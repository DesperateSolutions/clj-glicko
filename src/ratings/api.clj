(ns ratings.api
  (:require [compojure.api.sweet :refer :all]
            [ratings.persistance :as persistance]
            [ratings.schemas :refer :all]
            [cheshire.core :as json]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.util.response :refer [redirect]]
            [ring.util.http-response :refer [ok]]
            [schema.core :as s]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]])
  (:import [org.bson.types ObjectId]))

(s/defschema Total {:total Long})

(defapi ratings-routes
  (swagger-ui)
  (swagger-docs
   {:info {:title "clj-glicko"
           :description "REST api for clj-glicko"}})
  (context* "" []
            :tags ["api"]
            (GET* "/leagues" []
                  :summary "Returns all leagues"
                  :return [league]
                 (ok (persistance/get-leagues)))
            (GET* "/league" []
                  :summary "Return a specific league"
                  :return league
                  :query-params [league-id :- String]
                  :description "Return a specific league. Requires BSON id as a form-param"
                  (ok (persistance/get-league league-id)))
            (GET* "/:league/players" [league]
                  :summary "Return all players"
                  :path-params [league :- String]
                  :return [player]
                  (ok (persistance/get-players league)))
            (GET* "/:league/games" [league]
                  :summary "Return all games"
                  :path-params [league :- String]
                  :return [game]
                  (ok (persistance/get-games league)))
            (POST* "/:league/games" [league]
                   :summary "Add a game to the system - Result should be in the form of X-Y, for example 1-0"
                   :path-params [league :- String]
                   :form-params [whiteId :- String blackId :- String result :- String]
                   :return game
                   (ok (persistance/score-game whiteId blackId result league)))
            (POST* "/:league/players" [league]
                   :summary "Add a player to the system"
                   :path-params [league :- String]
                   :form-params [name :- String]
                   :return player
                   (ok (persistance/add-new-player name league)))
            (POST* "/leagues" []
                   :summary "Add a league to the system"
                   :form-params [league-name :- String settings :- String]
                   :return league
                   (ok (persistance/create-league league-name settings)))
            (DELETE* "/:league/players" [league]
                     :summary "Delete a player from the system"
                     :form-params [player-id :- String]
                     (ok (persistance/delete-player player-id league)))
            (DELETE* "/:league/games" [league]
                     :summary "Delete a game from the stystem"
                     :form-params [game-id :- String]
                     (ok (persistance/delete-game game-id league)))))

(def app
  (-> ratings-routes
      (wrap-defaults api-defaults)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))
