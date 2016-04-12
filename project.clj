(defproject com.chess-rating/rating "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "A simple glicko rater with built in api support. "
  :url "https://github.com/Molyna/clj-glicko"
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.8"]]
                   :source-paths ["dev"]}}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.novemberain/monger "3.0.0-rc2"]
                 [cheshire "5.5.0"]
                 [metosin/compojure-api "0.24.5" :exclusions [commons-codec]]
                 [compojure "1.4.0"]
                 [clj-time "0.11.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-cors "0.1.7"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler ratings.api/app})
