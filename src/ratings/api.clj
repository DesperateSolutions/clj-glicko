(ns ratings.api
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ratings.glicko :as glicko]
            [cheshire.core :as json]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defroutes ratings-routes
  (route/resources "/")
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
  (POST "/games" {{:strs [whiteId blackId result] :as params} :form-params session :session headers :headers}
         (try
           (json/generate-string (glicko/score-game whiteId blackId (Integer. result)))
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
  
  (POST "/players" {{:strs [name] :as params} :form-params session :session headers :headers}
         (try
           (json/generate-string (glicko/add-new-player name))
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
  (DELETE "/player" {{:strs [_id] :as params} :form-params session :seesion headers :headers}
          (try
            (str (glicko/delete-player _id))
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
  (DELETE "/game" {{:strs [_id] :as params} :form-params session :seesion headers :headers}
          (try
            (str (glicko/delete-game _id))
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
  (-> ratings-routes
      (wrap-defaults api-defaults)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))
