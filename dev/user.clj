(ns user
  (:require 
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]
   [ratings.glicko :as glicko]
   [ratings.persistance.scoring :as p]))

(def system
  "A Var containing an object representing the application under
  development."
  (atom {}))

(defn start
  "Starts the system running, updates the Var #'system."
  [])

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  []
  (when-let [s (:server @system)]
    (s)))

(defn go
  "Initializes and starts the system running."
  []
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after 'user/go))

