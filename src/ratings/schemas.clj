(ns ratings.schemas
  (:require [schema.core :as s])
  (:import [org.bson.types ObjectId]))

(s/defschema settings
  {:draw s/Str ;Boolean
   :period-length s/Str ;Date
   :scoreable s/Str ;Boolean
   })

(s/defschema player
  {:name s/Str
   :rating s/Num
   :rating-rd s/Num
   :volatility s/Num
   :has-played s/Str ;Boolean
   :_id s/Any})

(s/defschema league
  {:name s/Str
   :_id s/Any
   :settings settings
   })

(s/defschema game
  {:white s/Str
   :black s/Str
   :result s/Str
   :added s/Str
   :_id s/Any})

(s/defschema bulkgames
  {:white s/Str
   :black s/Str
   :result s/Str})

(s/defschema reseed
  {:white s/Str
   :black s/Str
   :result s/Str
   :added s/Str
   :_id s/Str})
