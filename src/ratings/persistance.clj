(ns ratings.persistance
  (:require [ratings.glicko :as glicko]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [clojure.tools.logging :as log]
            [monger.json :refer :all])
  (:import [org.bson.types ObjectId]))


(defn- get-mongo-uri [{:keys [db addr port user pass]}]
  (if (and user pass)
    (format "mongodb://%s:%s@%s:%s/%s" user pass addr port db)
    (format "mongodb://%s:%s/%s" addr port db)))

(defn get-db [name]
  (let [uri (get-mongo-uri {:db name
                            :addr (or (System/getenv "MONGODB_PORT_27017_TCP_ADDR") "127.0.0.1")
                            :port (or (System/getenv "MONGODB_PORT_27017_TCP_PORT") "27017")
                            :user (System/getenv "MONGODB_USER")
                            :pass (System/getenv "MONGODB_PASS")})]
    (log/info (format "mongo-uri: %s" uri))
    (:db (mg/connect-via-uri uri))))



(defn- update-player [player league]
  (let [db (get-db league)]
    (log/info (mc/update db "players" {:_id (:_id player)} player {:upsert true}))))


(defn get-players [league]
  (doall (map (fn [player]
                (assoc player :rating (Math/round (double (:rating player)))))
              (mc/find-maps (get-db league) "players"))))

(defn update-rd [league]
  (doseq [player (get-players league)]
    (if (= "true" (:has-played player))
      (update-player (assoc player :has-played "false") league)
      (update-player (assoc (glicko/update-rd-no-games player) :has-played "false") league))))

(defn update-timed []
  "This function should run the update on a timed basis for squash until the dashboard is sorted")

(defn get-games [league]
  (let [db (get-db league)]
    (doall (map (fn [{white :white black :black result :result id :_id added :added}]
                    (let [white-name (:name (mc/find-map-by-id db "players" (ObjectId. white)))
                          black-name (:name (mc/find-map-by-id db "players" (ObjectId. black)))]
                      (assoc nil :white white-name :black black-name :result (str result) :_id id :added added)))
                (mc/find-maps db "games")))))

(defn get-data []
  (assoc nil :players (get-players) :games (get-games)))

(defn get-player-from-id [id league]
  (mc/find-map-by-id (get-db league) "players" (ObjectId. id)))

(defn add-game [{rating1 :rating rd1 :rating-rd id1 :_id volatility1 :volatility white-name :name} {rating2 :rating rd2 :rating-rd id2 :_id volatility2 :volatility black-name :name} result league]
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
    (mc/insert (get-db league) "games" game)
    (assoc nil 
      :white white-name 
      :black black-name 
      :result result 
      :added (:added game) 
      :_id (str (:_id game)))))


(defn score-game [white-id black-id result league]
  (let [player1 (get-player-from-id white-id league)
        player2 (get-player-from-id black-id league)
        score (- (Integer. (first (clojure.string/split result #"-"))) (Integer. (last (clojure.string/split result #"-"))))]
    (cond (< 0 score)
          (do (update-player (glicko/get-glicko2 player1 player2 1) league)
              (update-player (glicko/get-glicko2 player2 player1 0) league))
          (> 0 score)
          (do (update-player (glicko/get-glicko2 player2 player1 1) league)
              (update-player (glicko/get-glicko2 player1 player2 0) league))
          :else
          (do (update-player (glicko/get-glicko2 player1 player2 0.5) league)
              (update-player (glicko/get-glicko2 player2 player1 0.5) league)))
    (add-game player1 player2 result league)))

(defn find-first [f users]
  (first (filter f users)))

(defn add-new-player [name league]
  (let [player (assoc nil :_id (ObjectId.) :name name :rating 1200 :rating-rd 350 :volatility 0.06 :has-played "false")]
    (log/info (mc/insert (get-db league) "players" player))
    player))

(defn add-games-bulk [league games]
  (let [current (atom nil)]
    (doseq [game games]
      (cond
       (not @current) (reset! current 
                              (f/unparse (f/formatters :date) (f/parse (f/formatters :date-time (:added game)))))
       (= @current  (f/unparse (f/formatters :date) (f/parse (f/formatters :date-time (:added game))))) 
       (do (update-rd league)
           (reset! current 
                   (f/unparse (f/formatters :date) (f/parse (f/formatters :date-time (:added game)))))))
      (let [players (mc/find-maps (get-db league) "players")
            white (or (find-first #(= (:name %) (:white game)) players)
                      (add-new-player (:white game) league))
            black (or (find-first #(= (:name %) (:black game)) players)
                      (add-new-player (:black game) league))]
        (score-game (str (:_id white)) 
                    (str (:_id black)) 
                    (cond 
                     (= (:result game) (str (:white game) " won!")) "1-0"
                     (= (:result game) (str (:black game) " won!")) "0-1"
                     :else "0-0")
                    league)))))

(defn get-latest-game-between-players [white black games latest]
  (if (first games)
    (if (and (= white (:white (first games))) (= black (:black (first games))))
      (if (or (not latest) (t/after? (c/from-string (:added (first games))) (c/from-string (:added latest))))
        (get-latest-game-between-players white black (rest games) (first games))
        (get-latest-game-between-players white black (rest games) latest))
      (get-latest-game-between-players white black (rest games) latest))
    latest))

;;Delete game will only allow deletion of the latest game played by both players
(defn delete-game [id league]
  (let [db (get-db league)
        game (mc/find-map-by-id db "games" (ObjectId. id))]
    (if (= game (get-latest-game-between-players (:white game) (:black game) (mc/find-maps db "games") nil))
      (do
        (update-player (assoc (get-player-from-id (:white game) league) :rating (:white-old-rating game) :rating-rd (:white-old-rd game)) league)
        (update-player (assoc (get-player-from-id (:black game) league) :rating (:black-old-rating game) :rating-rd (:black-old-rd game)) league)
        (mc/remove-by-id db "games" (ObjectId. id))
        game)
      (throw (IllegalArgumentException. "To old")))))

;;Deleting players will not change any ratings
(defn delete-player [id league]
  (let [db (get-db league)
        id (ObjectId. id)
        player (mc/find-by-id db "players" id)]
    (mc/remove-by-id db "players" id)
    player))

(defn create-league [league-name settings]
  (let [league (assoc nil :_id (ObjectId.) :name league-name :settings settings)]
    (log/info (mc/insert (get-db "leagues") "settings" league))
    league))

(defn get-league [id]
  (mc/find-map-by-id (get-db "leagues") "settings" (ObjectId. id)))

(defn get-leagues []
  (mc/find-maps (get-db "leagues") "settings"))
