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



(defn- update-player 
  ([player league]
   (update-player (get-db league) player league))
  ([db player {old_rating :rating} league]
    (log/info (mc/update db 
                         "players" 
                         {:_id (:_id player)} 
                         (assoc player :old_ratings 
                                (if (:old_ratings player)
                                  (conj (:old_ratings player) old_rating)
                                  (conj [] old_rating))) 
                         {:upsert true}))))


(defn get-players 
  ([league]
   (get-players (get-db league) league))
  ([db league]
   (doall (map (fn [player]
                 (assoc player :rating (Math/round (double (:rating player)))))
               (mc/find-maps db "players")))))

(defn update-rd 
  ([league]
   (update-rd (get-db league) league))
  ([db league]
   (doseq [player (get-players db league)]
     (if (= "true" (:has-played player))
       (update-player db (assoc player :has-played "false") league)
       (update-player db (assoc (glicko/update-rd-no-games player) :has-played "false") league)))))

(defn update-timed []
  "This function should run the update on a timed basis for squash until the dashboard is sorted")

(defn get-games 
  ([league]
   (get-games (get-db league) league))
  ([db league]
   (doall (map (fn [{white :white black :black result :result id :_id added :added}]
                 (let [white-name (:name (mc/find-map-by-id db "players" (ObjectId. white)))
                       black-name (:name (mc/find-map-by-id db "players" (ObjectId. black)))]
                   (assoc nil :white white-name :black black-name :result (str result) :_id id :added added)))
               (mc/find-maps db "games")))))

(defn get-player-from-id 
  ([id league]
   (get-player-from-id (get-db league) id league))
  ([db id league]
   (mc/find-map-by-id db "players" (ObjectId. id))))

(defn add-game
  ([player1 player2 result league added]
   (add-game (get-db league) player1 player2 result league added))
  ([db {rating1 :rating rd1 :rating-rd id1 :_id volatility1 :volatility white-name :name} {rating2 :rating rd2 :rating-rd id2 :_id volatility2 :volatility black-name :name} result league added]
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
                :added (or added (c/to-string (t/now))))]
     (mc/insert db "games" game)
     (assoc nil 
       :white white-name 
       :black black-name 
       :result result 
       :added (:added game) 
       :_id (str (:_id game))))))

(defn score-game 
  ([white-id black-id result league]
   (score-game (get-db league) white-id black-id result league nil))
  ([db white-id black-id result league added]
   (let [player1 (get-player-from-id db white-id league)
         player2 (get-player-from-id db black-id league)
         score (- (Integer. (first (clojure.string/split result #"-"))) (Integer. (last (clojure.string/split result #"-"))))]
     (cond (< 0 score)
           (do (update-player db (glicko/get-glicko2 player1 player2 1) player1 league)
               (update-player db (glicko/get-glicko2 player2 player1 0) player2 league))
           (> 0 score)
           (do (update-player db (glicko/get-glicko2 player2 player1 1) player2 league)
               (update-player db (glicko/get-glicko2 player1 player2 0) player1 league))
           :else
           (do (update-player db (glicko/get-glicko2 player1 player2 0.5) player1 league)
               (update-player db (glicko/get-glicko2 player2 player1 0.5) player2 league)))
     (add-game db player1 player2 result league added))))

(defn add-games-bulk
  ([league games]
   (add-games-bulk (get-db league) league games))
  ([db league games]
   (doseq [game games]
     (score-game db
                 (str (:white game)) 
                 (str (:black game)) 
                 (:result game)
                 league
                 nil))))

(defn add-new-player 
  ([name league]
   (add-new-player (get-db league) name league))
  ([db name league]
   (let [player (assoc nil :_id (ObjectId.) :name name :rating 1200 :rating-rd 350 :volatility 0.06 :has-played "false")]
     (log/info (mc/insert db "players" player))
     player)))

(defn find-first [f users]
  (first (filter f users)))



(defn- delete-all-players 
  ([league]
   (delete-all-players (get-db league) league))
  ([db league]
    (doseq [player (mc/find-maps db "players")]
      (mc/remove-by-id db "players" (:_id player)))))

(defn- delete-all-games 
  ([league]
   (delete-all-games (get-db league) league))
  ([db league]
   (doseq [game (mc/find-maps db "games")]
     (mc/remove-by-id db "games" (:_id game)))))

(defn reseed-db-with-games-and-players 
  ([league games]
   (reseed-db-with-games-and-players (get-db league) league games))
  ([db league games]
   (let [current (atom nil)]
     (delete-all-games db league)
     (delete-all-players db league)
     (doseq [game games]
       (cond
        (not @current) (reset! current
                               (f/unparse (f/formatters :date) (f/parse (f/formatters :date-time) (:added game))))
        (not= @current (f/unparse (f/formatters :date) (f/parse (f/formatters :date-time) (:added game)))) 
        (do (update-rd db league)
            (reset! current 
                    (f/unparse (f/formatters :date) (f/parse (f/formatters :date-time) (:added game))))))
       (let [players (mc/find-maps db "players")
             white (or (find-first #(= (:name %) (:white game)) players)
                       (add-new-player db (:white game) league))
             black (or (find-first #(= (:name %) (:black game)) players)
                       (add-new-player db (:black game) league))]
         (score-game db 
                     (str (:_id white)) 
                     (str (:_id black)) 
                     (:result game)
                     league
                     (:added game)))))))

(defn delete-and-add-games-bulk [league game-id]
  (let [db (get-db league)
        games (get-games db league)
        current (atom nil)
        del-game (find-first #(= (str (:_id %)) game-id) games)]
    (if del-game
      (do
        (delete-all-games db league)
        (delete-all-players db league)
        (doseq [game games]
          (when-not (= game-id (str (:_id game)))
            (cond
             (not @current) (reset! current
                                    (f/unparse (f/formatters :date) (f/parse (f/formatters :date-time) (:added game))))
             (not= @current (f/unparse (f/formatters :date) (f/parse (f/formatters :date-time) (:added game)))) 
             (do (update-rd db league)
                 (reset! current 
                         (f/unparse (f/formatters :date) (f/parse (f/formatters :date-time) (:added game))))))
            (let [players (mc/find-maps db "players")
                  white (or (find-first #(= (:name %) (:white game)) players)
                            (add-new-player db (:white game) league))
                  black (or (find-first #(= (:name %) (:black game)) players)
                            (add-new-player db (:black game) league))]
              (score-game db 
                          (str (:_id white)) 
                          (str (:_id black)) 
                          (:result game)
                          league
                          (:added game))))))
      (throw (Exception. "Can't find game")))))

(defn create-league [league-name settings]
  (let [league (assoc nil :_id (ObjectId.) :name league-name :settings (assoc {} :draw (:draw settings) :period-length (:period settings) :scoreable (:scoreable settings)))]
    (log/info (mc/insert (get-db "leagues") "settings" league))
    league))

(defn get-league [id]
  (let [league (mc/find-map-by-id (get-db "leagues") "settings" (ObjectId. id))] 
    (assoc league :settings {:draw (or (:draw (:settings league)) "a") 
                             :period-length (or (:period-length (:settings league)) "a") 
                             :scoreable (or (:scoreable (:settings league)) "a")})))

(defn get-leagues []
  (->> (mc/find-maps (get-db "leagues") "settings")
       (reduce (fn [return league]
                 (log/info league)
                 (let [s (:settings league)]
                   (conj return 
                         (assoc league 
                           :settings (assoc nil 
                                       :draw (or (:draw s) "a") 
                                       :period-length (or (:period-length s) "a") 
                                       :scoreable (or (:scoreable s) "a"))))))
               [])))


;;Deleting players will not change any ratings
(defn delete-player [id league]
  (let [db (get-db league)
        _id (ObjectId. id)
        player (mc/find-by-id db "players" _id)
        games (mc/find-maps db "games")]
    (if (find-first (fn [game] (or (= (:black game) id) (= (:white game) id))) games)
      (throw (ex-info "Can't delete player" {:causes "Player has games"}))
      (do (mc/remove-by-id db "players" _id)
          "Player deleted"))))

