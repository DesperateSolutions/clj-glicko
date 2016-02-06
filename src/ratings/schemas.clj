(ns ratings.schemas
  (:require [schema.core :as s])
  (:import [org.bson.types ObjectId]))

(s/defschema player
  {:name s/Str
   :rating s/Num
   :rating-rd s/Num
   :volatility s/Num
   :_id s/Str})

(s/defschema League
  {:name s/Str
   :_id s/Str})

(s/defschema games
  {:white-id s/Str
   :black-id s/Num
   :result s/Num
   :score s/Num
   :_id s/Str})
