(defproject com.chess-rating/rating "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "A simple glicko rater with built in api support. "
  :url "https://github.com/Molyna/clj-glicko"
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.8"]]
                   :source-paths ["dev"]}}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.novemberain/monger "3.0.1"]
                 [cheshire "5.6.3"]
                 [metosin/compojure-api "1.1.6" :exclusions [commons-codec]]
                 [compojure "1.5.1"]
                 [clj-time "0.12.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring-cors "0.1.8"]
                 [com.cemerick/friend "0.2.3" :exclusions [slingshot org.clojure/core.cache commons-logging org.clojure/data.priority-map]]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler ratings.api.api/app})
