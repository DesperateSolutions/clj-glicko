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
                  :return [League]
                 (ok (persistance/get-leagues)))
            (GET* "/league" [id]
                  :summary "Return a specific league"
                  :return League
                  :form-params [id :- String]
                  :description "Return a specific league. Requires BSON id as a form-param"
                  (ok (persistance/get-league id)))
            (GET* "/:league/players" [league]
                  :summary "Return all players"
                  :path-params [league :- String]
                  :return [player]
                  (ok (persistance/get-leagues league)))
            (GET* "/:league/games" [league]
                  :summary "Return all games"
                  :path-params [league :- String]
                  :return [games]
                  (ok (persistance/get-games league)))
            (POST* "/:league/games" [league]
                   :summary "Add a game to the system"
                   :path-params [league :- String]
                   :form-params [whiteId :- String blackId :- String result :- String]
                   (ok (persistance/score-game whiteId blackId result league)))
            (POST* "/:league/player" [league]
                   :summary "Add a player to the system"
                   :path-params [league :- String]
                   :form-params [name :- String]
                   :return player
                   (ok (persistance/add-new-player name league)))
            (POST* "/leagues" []
                   :summary "Add a league to the system"
                   :form-params [league-name :- String settings :- s/Any]
                   :return League
                   (ok (persistance/create-league league-name settings)))
            (DELETE* "/:league/player" [league]
                     :summary "Delete a player from the system"
                     :form-params [player-id :- String]
                     :return player
                     (ok (persistance/delete-player player-id league)))
            (DELETE* "/:league/game" [league]
                     :summary "Delete a game from the stystem"
                     :form-params [game-id :- String]
                     :return game
                     (ok (persistance/delete-game game-id league)))))

(def app
  (-> ratings-routes
      (wrap-defaults api-defaults)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))
