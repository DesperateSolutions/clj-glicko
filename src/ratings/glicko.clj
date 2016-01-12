(ns ratings.glicko
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.tools.logging :as log]
            [monger.json :refer :all])
  (:import [org.bson.types ObjectId]))


(defn convert-rating-to-glicko2 [rating]
  (/ (- rating 1500) 173.7178))

(defn convert-rd-to-glicko2 [rd]
  (/ rd 173.7178))

(defn update-rd [{rd :rating-rd} t]
  (if (= rd 0)
    350
    (min 350 (Math/sqrt (+ (* rd rd) (* (* 30 30) t))))))

(defn- get-q []
  0.0057565)

(defn get-volatile-g [rd]
  (/ 1 (Math/sqrt (+ 1 (/ (* 3 (Math/pow rd 2)) (Math/pow Math/PI 2))))))

(defn get-volatile-e [rating1 rating2 g]
  (/ 1 (1 + (Math/exp (* (* -1 g) (- rating1 rating2))))))

(defn get-v [e g]
  (* (Math/pow g 2) e (- 1 e)))

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

(defn new-rd [delta rd v r]
  (let [epsilon 0.000001
        a (Math/log (Math/pow rd 2))
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

(defn get-glicko2 [rating1 rating2 volatility rd result r]
  (let [g (get-volatile-g volatility)
        e (get-volatile-e rating1 rating2 g)
        v (get-v e g)
        delta (get-delta e g v result)
        volatility-marked (new-rd delta rd v r)
        rd-starred (pre-rating-rd rd volatility-marked)
        rd-marked (get-rd-marked rd-starred v)
        rating-marked (get-rating-marked rating1 rd-marked g result e)]
    (assoc nil :rating (+ 1500 (* 173.7178 rating-marked)) :rd (* 173.7178 rd-marked) :volatility volatility-marked)))

(defn- get-g [rd]
  (/ 1 (Math/sqrt (+ 1 (/ (* 3 (Math/pow (get-q) 2) (Math/pow rd 2)) (Math/pow Math/PI 2))))))

(defn- get-e [rating1 rating2 rd]
  (/ 1 (+ 1 (Math/pow 10 (/ (* (- (get-g rd)) (- rating1 rating2)) 400)))))

(defn get-d [g e]
  (Math/pow (* (Math/pow (get-q) 2) (Math/pow g 2) e (- 1 e)) -1))

(defn- new-rd [rd d]
  (Math/sqrt (Math/pow (+ (/ 1 (Math/pow rd 2)) (/ 1 d)) -1)))


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

(defn- update-rating [{rating1 :rating rd1 :rating-rd :as player1} {rating2 :rating rd2 :rating-rd :as player2} result]
  (let [q (get-q)
        g (get-g rd2)
        e (get-e rating1 rating2 rd2)
        d (get-d g e)
        new-rating (+ rating1 (* (/ q (+ (/ 1 (Math/pow rd1 2)) (/ 1 d))) g (- result e)))
        new-rd (new-rd rd1 d)]
    (update-player (assoc player1 :rating new-rating :rating-rd new-rd))))

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

(defn add-game [{rating1 :rating rd1 :rating-rd id1 :_id} {rating2 :rating rd2 :rating-rd id2 :_id} result]
  (let [game (assoc nil 
               :_id (ObjectId.)
               :white (str id1)
               :black (str id2)
               :result result
               :white-old-rating rating1
               :white-old-rd rd1
               :black-old-rating rating2
               :black-old-rd rd2
               :added (c/to-string (t/now)))]
    (mc/insert (get-db) "games" game)
    game))

(defn score-game [white-id black-id result]
  (let [player1 (get-player-from-id white-id)
        player2 (get-player-from-id black-id)]
    (cond (= 1 result)
          (do (update-rating player1 player2 1)
              (update-rating player2 player1 0))
          (= -1 result)
          (do (update-rating player2 player1 1)
              (update-rating player1 player2 0))
          :else
          (do (update-rating player1 player2 0.5)
              (update-rating player2 player1 0.5)))
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
