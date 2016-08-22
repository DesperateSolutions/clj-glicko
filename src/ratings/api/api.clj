(ns ratings.api.api
  (:require [compojure.api.sweet :refer :all]
            [ratings.persistance.scoring :as scoring]
            [ratings.persistance.auth :as auth]
            [ratings.api.schemas :refer :all]
            [cheshire.core :as json]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.util.response :refer [redirect]]
            [ring.util.http-response :refer [ok]]
            [schema.core :as s]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]))
  (:import [org.bson.types ObjectId]))

(s/defschema Total {:total Long})

(defapi ratings-routes
  (swagger-routes {:data {:info {:title "clj-glicko"
                                 :description "REST api for clj-glicko"}}})
  (context "" []
           :tags ["api"]
           (GET "/leagues" []
                  :summary "Returns all leagues"
                  :return [league]
                 (ok (scoring/get-leagues)))
            (GET "/league" []
                  :summary "Return a specific league"
                  :return league
                  :query-params [league-id :- String]
                  :description "Return a specific league. Requires BSON id as a form-param"
                  (ok (scoring/get-league league-id)))
            (GET "/:league/players" [league]
                  :summary "Return all players"
                  :path-params [league :- String]
                  :return [player]
                  (ok (scoring/get-players league)))
            (GET "/:league/games" [league]
                  :summary "Return all games"
                  :path-params [league :- String]
                  :return [game]
                  (ok (scoring/get-games league)))
            (POST "/:league/games" [league]
                   :summary "Add a game to the system - Result should be in the form of X-Y, for example 1-0"
                   :path-params [league :- String]
                   :form-params [whiteId :- String blackId :- String result :- String]
                   :return game
                   (ok (scoring/score-game whiteId blackId result league)))
            (POST "/:league/bulkgames" [league]
                   :summary "Add a collection of games - Result must be in the format of X-Y, example 1-0"
                   :path-params [league :- String]
                   :form-params [bulkgames]
                   (ok (scoring/add-games-bulk league (json/parse-string bulkgames true))))
            (POST "/:league/players" [league]
                   :summary "Add a player to the system"
                   :path-params [league :- String]
                   :form-params [name :- String]
                   :return player
                   (ok (scoring/add-new-player name league)))
            (POST "/leagues" []
                   :summary "Add a league to the system"
                   :form-params [league-name :- String settings :- String]
                   :return league
                   (ok (scoring/create-league league-name settings)))
            (POST "/:league/update" [league]            
                   :summary "Updates RD of players that didn't play"
                   :path-params [league :- String]
                   (ok (scoring/update-rd league)))
            (POST "/:league/reseed" [league]
                  :summary "Reseeds the db with all the games from the bulk parameter. Deletes all games currently in db"
                  :path-params [league :- String]
                  :form-params [reseed]
                  (ok (scoring/reseed-db-with-games-and-players league (json/parse-string reseed true))))
            (DELETE "/:league/players" [league]
                     :summary "Delete a player from the system"
                     :form-params [player-id :- String]
                     (ok (scoring/delete-player player-id league)))
            (DELETE "/:league/games" [league]
                     :summary "Delete a game from the system"
                     :form-params [game-id :- String]
                     (ok (scoring/delete-game game-id league)))
            (DELETE "/:league/oldremove" [league]
                     :summary "Used to remove a single game of old. Should be used with extreme care"
                     :path-params [league :- String]
                     :form-params [game-id :- String]
                     (ok (scoring/delete-and-add-games-bulk league game-id)))))

(def app
  (-> ratings-routes
      (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn auth/users)
                            :workflows [(workflows/interactive-form)]})
      (wrap-defaults api-defaults)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))
