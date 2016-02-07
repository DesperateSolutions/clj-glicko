(ns ratings.schemas
  (:require [schema.core :as s])
  (:import [org.bson.types ObjectId]))


(s/defschema player
  {:name s/Str
   :rating s/Num
   :rating-rd s/Num
   :volatility s/Num
   :_id s/Any})

(s/defschema league
  {:name s/Str
   :_id s/Any})

(s/defschema game
  {:white s/Str
   :black s/Str
   :result s/Str
   :added s/Str
   :_id s/Any})
