(ns ratings.glicko
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.tools.logging :as log]
            [monger.json :refer :all])
  (:import [org.bson.types ObjectId]))

;;Glicko2
(defn convert-rating-to-glicko2 [rating]
  (/ (- rating 1500) 173.7178))

(defn convert-rd-to-glicko2 [rd]
  (/ rd 173.7178))

(defn get-volatile-g [rd]
  (/ 1 (Math/sqrt (+ 1 (/ (* 3 (Math/pow rd 2)) (Math/pow (Math/PI) 2))))))

(defn get-volatile-e [rating1 rating2 g]
  (/ 1 (+ 1 (Math/exp (* (* -1 g) (- rating1 rating2))))))

(defn get-v [e g]
  (/ 1 (* (Math/pow g 2) e (- 1 e))))

(defn get-delta [e g v result]
  (* v g (- result e)))

(defn get-f [x delta rd v r a]
  (- (/ (* (Math/exp x) (- (Math/pow delta 2) (Math/pow rd 2) v (Math/exp x))) (* 2 (Math/pow (+ (Math/pow rd 2) v (Math/exp x)) 2))) (/ (- x a) (Math/pow r 2))))

(defn get-b [k r a delta rd v]
  (if (< (get-f (- a (* k r)) delta rd v r a) 0)
    (recur (inc k) r a delta rd v)
    (- a (* k r) delta rd v)))

(defn get-ab [a b fa fb delta rd v r epsilon]
  (if (> (Math/abs (- a b)) epsilon)
    (let [c (+ a (/ (* (- a b) fa) (- fb fa)))
          fc (get-f c delta rd v r a)]
      (if (< (* fc fb) 0)        
        (recur b c fb fc delta rd v r epsilon)
        (recur a c (/ fa 2) fc delta rd v r epsilon)))
    (Math/exp (/ a 2))))

(defn new-volatile [delta rd volatility v r]
  (let [epsilon 0.000001
        a (Math/log (Math/pow volatility 2))
        b (if (> (Math/pow delta 2) (+ (Math/pow rd 2) v))
            (Math/log (- (Math/pow delta 2) (Math/pow rd 2) v))
            (get-b 1 r a delta rd v))
        fa (get-f a delta rd v r a)
        fb (get-f b delta rd v r a)]
    (get-ab a b fa fb delta rd v r epsilon)))

(defn pre-rating-rd [rd rd-marked]
  (Math/sqrt (+ (Math/pow rd 2) (Math/pow rd-marked 2))))

(defn get-rd-marked [rd-starred v]
  (/ 1 (Math/sqrt (+ (/ 1 (Math/pow rd-starred 2)) (/ 1 v)))))

(defn get-rating-marked [rating rd-marked g result e]
  (+ rating (* (Math/pow rd-marked 2) g (- result e))))

(defn update-rd-no-games [{volatility :volatility rd :rating-rd :as player}]
  (assoc player :rating-rd (pre-rating-rd rd volatility)))

(defn- get-mongo-uri [{:keys [db addr port user pass]}]
  (if (and user pass)
    (format "mongodb://%s:%s@%s:%s/%s" user pass addr port db)
    (format "mongodb://%s:%s/%s" addr port db)))

(defn get-db []
  (let [uri (get-mongo-uri {:db (or (System/getenv "MONGODB_DB") "chess")
                            :addr (or (System/getenv "MONGODB_PORT_27017_TCP_ADDR") "127.0.0.1")
                            :port (or (System/getenv "MONGODB_PORT_27017_TCP_PORT") "27017")
                            :user (System/getenv "MONGODB_USER")
                            :pass (System/getenv "MONGODB_PASS")})]
    (log/info (format "mongo-uri: %s" uri))
    (:db (mg/connect-via-uri uri))))

(defn- update-player [player]
  (let [db (get-db)]
    (mc/update db "players" {:_id (:_id player)} player {:upsert true})))

(defn get-glicko2 [{rating1 :rating rd1 :rating-rd volatility :volatility :as player} {rating2 :rating rd2 :rating-rd} result]
  (let [rating1 (convert-rating-to-glicko2 rating1)
        rd1 (convert-rd-to-glicko2 rd1)
        rating2 (convert-rating-to-glicko2 rating2)
        rd2 (convert-rd-to-glicko2 rd1)
        g (get-volatile-g rd2)
        e (get-volatile-e rating1 rating2 g)
        v (get-v e g)
        delta (get-delta e g v result)
        volatility-marked (new-volatile delta rd1 volatility v 0.5)
        rd-starred (pre-rating-rd rd1 volatility-marked)
        rd-marked (get-rd-marked rd-starred v)
        rating-marked (get-rating-marked rating1 rd-marked g result e)]
    (update-player (assoc player :rating (+ 1500 (* 173.7178 rating-marked)) :rd (* 173.7178 rd-marked) :volatility volatility-marked))))

(defn get-players []
  (mc/find-maps (get-db) "players"))

(defn get-games []
  (let [db (get-db)]
    (doall (map (fn [{white :white black :black result :result id :_id}]
                    (let [white-name (:name (mc/find-map-by-id db "players" (ObjectId. white)))
                          black-name (:name (mc/find-map-by-id db "players" (ObjectId. black)))
                          result-string (cond (= result 1)
                                              (str white-name " won!")
                                              (= result -1)
                                              (str black-name " won!")
                                              :else
                                              "Drawn!")]
                      (assoc nil :white white-name :black black-name :result result-string :_id id)))
                (mc/find-maps db "games")))))

(defn get-data []
  (assoc nil :players (get-players) :games (get-games)))

(defn get-player-from-id [id]
  (mc/find-map-by-id (get-db) "players" (ObjectId. id)))

(defn add-game [{rating1 :rating rd1 :rating-rd id1 :_id volatility1 :volatility} {rating2 :rating rd2 :rating-rd id2 :_id volatility2 :volatility} result]
  (let [game (assoc nil 
               :_id (ObjectId.)
               :white (str id1)
               :black (str id2)
               :result result
               :white-old-rating rating1
               :white-old-rd rd1
               :black-old-rating rating2
               :black-old-rd rd2
               :white-old-volatility volatility1
               :black-old-volatility volatility2
               :added (c/to-string (t/now)))]
    (mc/insert (get-db) "games" game)
    game))

(defn score-game [white-id black-id result]
  (let [player1 (get-player-from-id white-id)
        player2 (get-player-from-id black-id)]
    (cond (= 1 result)
          (do (get-glicko2 player1 player2 1)
              (get-glicko2 player2 player1 0))
          (= -1 result)
          (do (get-glicko2 player2 player1 1)
              (get-glicko2 player1 player2 0))
          :else
          (do (get-glicko2 player1 player2 0.5)
              (get-glicko2 player2 player1 0.5)))
    (add-game player1 player2 result)))

(defn add-new-player [name]
  (let [player (assoc nil :_id (ObjectId.) :name name :rating 1200 :rating-rd 350 :volatility 0.06)]
    (mc/insert (get-db) "players" player)
    player))


(defn get-latest-game-between-players [white black games latest]
  (if (first games)
    (if (and (= white (:white (first games))) (= black (:black (first games))))
      (if (or (not latest) (t/after? (c/from-string (:added (first games))) (c/from-string (:added latest))))
        (get-latest-game-between-players white black (rest games) (first games))
        (get-latest-game-between-players white black (rest games) latest))
      (get-latest-game-between-players white black (rest games) latest))
    latest))

;;Delete game will only allow deletion of the latest game played by both players
(defn delete-game [id]
  (let [game (mc/find-map-by-id (get-db) "games" (ObjectId. id))]
    (if (= game (get-latest-game-between-players (:white game) (:black game) (mc/find-maps (get-db) "games") nil))
      (do
        (update-player (assoc (get-player-from-id (:white game)) :rating (:white-old-rating game) :rating-rd (:white-old-rd game)))
        (update-player (assoc (get-player-from-id (:black game)) :rating (:black-old-rating game) :rating-rd (:black-old-rd game)))
        (mc/remove-by-id (get-db) "games" (ObjectId. id)))
      (throw (IllegalArgumentException. "To old")))))

;;Deleting players will not change any ratings
(defn delete-player [id]
  (mc/remove-by-id (get-db) "players" (ObjectId. id)))
