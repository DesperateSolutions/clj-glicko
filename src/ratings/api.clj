(ns ratings.api
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clostache.parser :as tpl]))


;;Will be updated to call backend functions based on api calls. Needs a Post as well

(defroutes ratings-api
 (route/resources "public")
 (GET "/players" {session :session
                  headers :headers
                  params  :query-params}
      {:status 200
       :body (tpl/render-resource "show-players.html")
       :headers {"Content Type" "text/html"}}))
  
