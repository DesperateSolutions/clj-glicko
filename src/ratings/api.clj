(ns ratings.api
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clostache.parser :as tpl]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))


;;Will be updated to call backend functions based on api calls. Needs a Post as well

(defroutes ratings-routes
  ;(route/resources "public")  
  (GET "/players" {session :session
                   headers :headers
                   params  :query-params}
       {:status 200
        :body (tpl/render-resource "players.html" {})
        :headers {"Content-Type" "text/html"}})

  (POST "/game" {session :session
                 headers :headers
                 params  :query-params}
       {:status 200
        :body (tpl/render-resource "players.html" {})
        :headers {"Content-Type" "text/html"}})
  
  (route/not-found "<h1>Page not found</h1>"))


(def app
  (wrap-defaults ratings-routes site-defaults))
