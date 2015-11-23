(ns ratings.glicko
  (:require [monger.core :as mg]
            [monger.collection :as mc])
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
  (let [db (get-db)
        games (mc/find-maps db "games")]
    (println games)
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
                games))))

(defn get-data []
  (assoc nil :players (get-players) :games (get-games)))

(defn get-player-from-id [id]
  (mc/find-map-by-id (get-db) "players" (ObjectId. id)))

(defn add-game [white black result]
  (mc/insert (get-db) "games" (assoc nil :_id (ObjectId.) :white white :black black :result result)))

(defn score-game [white-id black-id result]
  (let [player1 (get-player-from-id white-id)
        player2 (get-player-from-id black-id)]
    (add-game white-id black-id result)
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


(defn delete-game [id]
  (mc/remove-by-id (get-db) "games" (ObjectId. id)))

(defn delete-player [id]
  (mc/remove-by-id (get-db) "players" (ObjectId. id)))
