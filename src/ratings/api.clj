(ns ratings.api
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clostache.parser :as tpl]
            [ratings.glicko :as glicko]
            [clojure.data.json :as json]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes ratings-routes
  (route/resources "public")  
  (GET "/" {session :session
                   headers :headers
                   params  :query-params}
       {:status 200
        :body (tpl/render-resource "players.html" (glicko/get-players))
        :headers {"Content-Type" "text/html"}})

  (POST "/add-game" {{:strs [white black result] :as params} :form-params session :session}
         (try
           (glicko/score-game white black result)
           (catch com.fasterxml.jackson.core.JsonParseException e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/write-str {:error (str "Unable to parse swagger endpoint.")})})
           (catch IllegalArgumentException e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/write-str {:error (.getMessage e)})})
           (catch clojure.lang.ExceptionInfo e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/write-str {:error (-> e ex-data :causes)})})
           (catch Exception e
             (.printStackTrace e)
             {:status 500
              :headers {"Content-Type" "application/json"}
              :body (json/write-str {:error (str "An unexpected error occurred! ")})})))
  
  (route/not-found "<h1>Page not found</h1>"))


(def app
  (wrap-defaults ratings-routes site-defaults))
