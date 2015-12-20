(ns ratings.api
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clostache.parser :as tpl]
            [ratings.glicko :as glicko]
            [cheshire.core :as json]
            [ring.util.response :refer [redirect]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defroutes ratings-routes
  (route/resources "/")
  (GET "/" {session :session
            headers :headers
            params  :query-params}
       {:status 200
        :body (tpl/render-resource "index.html" (glicko/get-data))
        :headers {"Content-Type" "text/html"}})

  (GET "/players" {session :session
                   headers :headers
                   params :query-params}
       {:status 200
        :body (json/generate-string (glicko/get-players))
        :headers {"Content-Type" "application/json"}})
  (GET "/games" {session :session
                   headers :headers
                   params :query-params}
       {:status 200
        :body (json/generate-string (glicko/get-games))
        :headers {"Content-Type" "application/json"}})
  (POST "/addgame" {{:strs [white-id black-id result] :as params} :form-params session :session headers :headers}
         (try
           (json/generate-string (glicko/score-game white-id black-id (Integer. result)))
           (redirect (get headers "referer"))
           (catch com.fasterxml.jackson.core.JsonParseException e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:error (str "Unable JSON.")})})
           (catch IllegalArgumentException e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:error (.getMessage e)})})
           (catch clojure.lang.ExceptionInfo e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:error (-> e ex-data :causes)})})
           (catch Exception e
             (.printStackTrace e)
             {:status 500
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:error (str "An unexpected error occurred! ")})})))
  
  (POST "/addplayer" {{:strs [name] :as params} :form-params session :session headers :headers}
         (try
           (json/generate-string (glicko/add-new-player name))
           (redirect (get headers "referer"))
           (catch com.fasterxml.jackson.core.JsonParseException e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:error (str "Unable JSON.")})})
           (catch IllegalArgumentException e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:error (.getMessage e)})})
           (catch clojure.lang.ExceptionInfo e
             (.printStackTrace e)
             {:status 406
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:error (-> e ex-data :causes)})})
           (catch Exception e
             (.printStackTrace e)
             {:status 500
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:error (str "An unexpected error occurred! ")})})))
  (DELETE "/delete-player" {{:strs [_id] :as params} :form-params session :seesion headers :headers}
          (try
            (str (glicko/delete-player _id))
            (redirect (get headers "referer"))
            (catch com.fasterxml.jackson.core.JsonParseException e
              (.printStackTrace e)
              {:status 406
               :headers {"Content-Type" "application/json"}
               :body (json/generate-string {:error (str "Unable JSON.")})})
            (catch IllegalArgumentException e
              (.printStackTrace e)
              {:status 406
               :headers {"Content-Type" "application/json"}
               :body (json/generate-string {:error (.getMessage e)})})
            (catch clojure.lang.ExceptionInfo e
              (.printStackTrace e)
              {:status 406
               :headers {"Content-Type" "application/json"}
               :body (json/generate-string {:error (-> e ex-data :causes)})})
           (catch Exception e
             (.printStackTrace e)
             {:status 500
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:error (str "An unexpected error occurred! ")})})))          
  (DELETE "/delete-game" {{:strs [_id] :as params} :form-params session :seesion headers :headers}
          (try
            (str (glicko/delete-game _id))
            (redirect (get headers "referer"))
            (catch com.fasterxml.jackson.core.JsonParseException e
              (.printStackTrace e)
              {:status 406
               :headers {"Content-Type" "application/json"}
               :body (json/generate-string {:error (str "Unable JSON.")})})
            (catch IllegalArgumentException e
              (.printStackTrace e)
              {:status 406
               :headers {"Content-Type" "application/json"}
               :body (json/generate-string {:error (.getMessage e)})})
            (catch clojure.lang.ExceptionInfo e
              (.printStackTrace e)
              {:status 406
               :headers {"Content-Type" "application/json"}
               :body (json/generate-string {:error (-> e ex-data :causes)})})
            (catch Exception e
              (.printStackTrace e)
              {:status 500
               :headers {"Content-Type" "application/json"}
               :body (json/generate-string {:error (str "An unexpected error occurred! ")})})))          
  
  
  
  (route/not-found "<h1>Page not found</h1>"))


(def app
  (wrap-defaults ratings-routes api-defaults))
