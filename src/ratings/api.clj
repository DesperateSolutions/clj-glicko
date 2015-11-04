(ns ratings.api
  (:require [compojure.core :refer :all]
            [compojure.route :as route]))


;;Will be updated to call backend functions based on api calls. Needs a Post as well
(defroutes app
  (GET "/" [] "<h1>Hello World!</h1>")
  (route/not-found "<h1>Not Found</h1>"))
