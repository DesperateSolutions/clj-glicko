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
                  :path-params [league :- String]
                  :return [player]
                  (ok (persistance/get-leagues league)))))


;; (defroutes ratings-routes
;;   (route/resources "/")
;;   (GET "/:league/players" [league]
;;        {:status 200
;;         :body (json/generate-string (persistance/get-players league))
;;         :headers {"Content-Type" "application/json"}})
;;   (GET "/:league/games" [league]
;;        {:status 200
;;         :body (json/generate-string (persistance/get-games league))
;;         :headers {"Content-Type" "application/json"}})
;;   (GET "/league" {session :session
;;                    headers :headers
;;                    params :query-params
;;                    {:strs [id] :as params} :form-params}
;;        {:status 200
;;         :body (json/generate-string (persistance/get-league id))
;;         :headers {"Content-Type" "application/json"}})
;;   ;; (GET "/leagues" {session :session
;;   ;;                  headers :headers
;;   ;;                  params :query-params}
;;   ;;      {:status 200
;;   ;;       :body (json/generate-string (persistance/get-leagues))
;;   ;;       :headers {"Content-Type" "application/json"}})
;;   (POST "/leagues" {{:strs [league-name settings] :as params} :form-params session :session headers :headers}
;;         (try
;;           (json/generate-string (persistance/create-league league-name (json/parse-string settings true)))
;;           (catch com.fasterxml.jackson.core.JsonParseException e
;;             (.printStackTrace e)
;;             {:status 406
;;              :headers {"Content-Type" "application/json"}
;;              :body (json/generate-string {:error (str "Unable JSON.")})})
;;           (catch IllegalArgumentException e
;;             (.printStackTrace e)
;;             {:status 406
;;              :headers {"Content-Type" "application/json"}
;;              :body (json/generate-string {:error (.getMessage e)})})
;;           (catch clojure.lang.ExceptionInfo e
;;             (.printStackTrace e)
;;             {:status 406
;;              :headers {"Content-Type" "application/json"}
;;              :body (json/generate-string {:error (-> e ex-data :causes)})})
;;           (catch Exception e
;;             (.printStackTrace e)
;;             {:status 500
;;              :headers {"Content-Type" "application/json"}
;;              :body (json/generate-string {:error (str "An unexpected error occurred! ")})})))
;;   (POST "/:league/games" {{:strs [whiteId blackId result] league :league :as params} :form-params session :session headers :headers}
;;          (try
;;            (json/generate-string (persistance/score-game whiteId blackId (Integer. result) league))
;;            (catch com.fasterxml.jackson.core.JsonParseException e
;;              (.printStackTrace e)
;;              {:status 406
;;               :headers {"Content-Type" "application/json"}
;;               :body (json/generate-string {:error (str "Unable JSON.")})})
;;            (catch IllegalArgumentException e
;;              (.printStackTrace e)
;;              {:status 406
;;               :headers {"Content-Type" "application/json"}
;;               :body (json/generate-string {:error (.getMessage e)})})
;;            (catch clojure.lang.ExceptionInfo e
;;              (.printStackTrace e)
;;              {:status 406
;;               :headers {"Content-Type" "application/json"}
;;               :body (json/generate-string {:error (-> e ex-data :causes)})})
;;            (catch Exception e
;;              (.printStackTrace e)
;;              {:status 500
;;               :headers {"Content-Type" "application/json"}
;;               :body (json/generate-string {:error (str "An unexpected error occurred! ")})})))
  
;;   (POST "/:league/players" {{:strs [name] league :league :as params} :form-params session :session headers :headers}
;;          (try
;;            (json/generate-string (persistance/add-new-player name league))
;;            (catch com.fasterxml.jackson.core.JsonParseException e
;;              (.printStackTrace e)
;;              {:status 406
;;               :headers {"Content-Type" "application/json"}
;;               :body (json/generate-string {:error (str "Unable JSON.")})})
;;            (catch IllegalArgumentException e
;;              (.printStackTrace e)
;;              {:status 406
;;               :headers {"Content-Type" "application/json"}
;;               :body (json/generate-string {:error (.getMessage e)})})
;;            (catch clojure.lang.ExceptionInfo e
;;              (.printStackTrace e)
;;              {:status 406
;;               :headers {"Content-Type" "application/json"}
;;               :body (json/generate-string {:error (-> e ex-data :causes)})})
;;            (catch Exception e
;;              (.printStackTrace e)
;;              {:status 500
;;               :headers {"Content-Type" "application/json"}
;;               :body (json/generate-string {:error (str "An unexpected error occurred! ")})})))
;;   (DELETE "/:league/player" {{:strs [_id] league :league :as params} :form-params session :seesion headers :headers}
;;           (try
;;             (str (persistance/delete-player _id league))
;;             (catch com.fasterxml.jackson.core.JsonParseException e
;;               (.printStackTrace e)
;;               {:status 406
;;                :headers {"Content-Type" "application/json"}
;;                :body (json/generate-string {:error (str "Unable JSON.")})})
;;             (catch IllegalArgumentException e
;;               (.printStackTrace e)
;;               {:status 406
;;                :headers {"Content-Type" "application/json"}
;;                :body (json/generate-string {:error (.getMessage e)})})
;;             (catch clojure.lang.ExceptionInfo e
;;               (.printStackTrace e)
;;               {:status 406
;;                :headers {"Content-Type" "application/json"}
;;                :body (json/generate-string {:error (-> e ex-data :causes)})})
;;            (catch Exception e
;;              (.printStackTrace e)
;;              {:status 500
;;               :headers {"Content-Type" "application/json"}
;;               :body (json/generate-string {:error (str "An unexpected error occurred! ")})})))          
;;   (DELETE "/:league/game" {{:strs [_id] league :league :as params} :form-params session :seesion headers :headers}
;;           (try
;;             (str (persistance/delete-game _id league))
;;             (catch com.fasterxml.jackson.core.JsonParseException e
;;               (.printStackTrace e)
;;               {:status 406
;;                :headers {"Content-Type" "application/json"}
;;                :body (json/generate-string {:error (str "Unable JSON.")})})
;;             (catch IllegalArgumentException e
;;               (.printStackTrace e)
;;               {:status 406
;;                :headers {"Content-Type" "application/json"}
;;                :body (json/generate-string {:error (.getMessage e)})})
;;             (catch clojure.lang.ExceptionInfo e
;;               (.printStackTrace e)
;;               {:status 406
;;                :headers {"Content-Type" "application/json"}
;;                :body (json/generate-string {:error (-> e ex-data :causes)})})
;;             (catch Exception e
;;               (.printStackTrace e)
;;               {:status 500
;;                :headers {"Content-Type" "application/json"}
;;                :body (json/generate-string {:error (str "An unexpected error occurred! ")})})))          
  
  
  
;;   (route/not-found "<h1>Page not found</h1>"))


(def app
  (-> ratings-routes
      (wrap-defaults api-defaults)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))
