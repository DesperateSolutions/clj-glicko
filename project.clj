(defproject com.chess-rating/rating "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description ""
  :url ""
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.8"]]
                   :source-paths ["dev"]}}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.novemberain/monger "3.0.0-rc2"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [compojure "1.4.0"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [http-kit "2.1.19"]])
