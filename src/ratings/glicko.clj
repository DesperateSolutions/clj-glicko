(ns ratings.glicko
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [monger.json :refer :all])
  (:import [org.bson.types ObjectId]))

;;We also want to just calculate it all in the main functions and send on - This is a lot of double shit
(defn update-rd [{rd :rating-rd :as player}]
  (if (= rd 0)
    350
    (min 350 (Math/sqrt (+ (* rd rd) (* (* 30 30) 1))))))

(defn- get-q []
  0.0057565)

(defn- get-g [rd]
  (/ 1 (Math/sqrt (+ 1 (/ (* 3 (Math/pow (get-q) 2) (Math/pow rd 2)) (Math/pow Math/PI 2))))))

(defn- get-e [rating1 rating2 rd]
  (/ 1 (+ 1 (Math/pow 10 (/ (* (- (get-g rd)) (- rating1 rating2)) 400)))))

(defn get-d [g e]
  (Math/pow (* (Math/pow (get-q) 2) (Math/pow g 2) e (- 1 e)) -1))

(defn- new-rd [rd d]
  (Math/sqrt (Math/pow (+ (/ 1 (Math/pow rd 2)) (/ 1 d)) -1)))

(defn- get-db []
  (mg/get-db (mg/connect) "chess"))

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
    (doall (map (fn [{white :white black :black result :result}]
                  (let [white-name (:name (mc/find-map-by-id db "players" (ObjectId. white)))
                        black-name (:name (mc/find-map-by-id db "players" (ObjectId. black)))
                        result-string (cond (= result 1) 
                                            (str white-name " won!")
                                            (= result -1)
                                            (str black-name " won!")
                                            :else
                                            "Drawn!")]
                    (assoc nil :white white-name :black black-name :result result-string)))
                (mc/find-maps db "games")))))

(defn get-data []
  (assoc nil :players (get-players) :games (get-games)))

(defn get-player-from-id [id]
  (mc/find-map-by-id (get-db) "players" (ObjectId. id)))

(defn add-game [{rating1 :rating rd1 :rating-rd id1 :_id} {rating2 :rating rd2 :rating-rd id2 :_id} result]
  (mc/insert (get-db) "games" (assoc nil 
                                :_id (ObjectId.) 
                                :white (str id1)
                                :black (str id2) 
                                :result result 
                                :white-old-rating rating1 
                                :white-old-rd rd1
                                :black-old-rating rating2
                                :black-old-rd rd2
                                :added (c/to-string (t/now)))))

(defn score-game [white-id black-id result]
  (let [player1 (get-player-from-id white-id)
        player2 (get-player-from-id black-id)]
    (add-game player1 player2 result)
    (cond (= 1 result)
          (do (update-rating player1 player2 1)
              (update-rating player2 player1 0))
          (= -1 result)
          (do (update-rating player2 player1 1)
              (update-rating player1 player2 0))
          :else
          (do (update-rating player1 player2 0.5)
              (update-rating player2 player1 0.5)))))

(defn add-new-player [name] 
  (mc/insert (get-db) "players" (assoc nil :_id (ObjectId.) :name name :rating 1200 :rating-rd 350)))


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
