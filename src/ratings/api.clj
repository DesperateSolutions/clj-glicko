(ns ratings.api
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clostache.parser :as tpl]
            [ratings.glicko :as glicko]
            [clojure.data.json :as json]
            [ring.util.response :refer [redirect]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defroutes ratings-routes
  (route/resources "public")  
  (GET "/" {session :session
                   headers :headers
                   params  :query-params}
       {:status 200
        :body (tpl/render-resource "players.html" (glicko/get-data))
        :headers {"Content-Type" "text/html"}})

  (POST "/addgame" {{:strs [white-id black-id result] :as params} :form-params session :session headers :headers}
         (try
           (str (glicko/score-game white-id black-id (Integer. result)))
           (redirect (get headers "referer"))
           (catch com.fasterxml.jackson.core.JsonParseException e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/write-str {:error (str "Unable JSON.")})})
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
  
  (POST "/addplayer" {{:strs [name] :as params} :form-params session :session headers :headers}
         (try
           (str (glicko/add-new-player name))
           (redirect (get headers "referer"))
           (catch com.fasterxml.jackson.core.JsonParseException e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/write-str {:error (str "Unable JSON.")})})
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
  (wrap-defaults ratings-routes api-defaults))
