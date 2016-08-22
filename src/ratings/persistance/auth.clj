(ns ratings.persistance.auth
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")}
            "jane" {:username "jane"
                    :password (creds/hash-bcrypt "user_password")}})
