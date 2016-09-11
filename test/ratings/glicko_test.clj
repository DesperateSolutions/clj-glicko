(ns ratings.glicko-test
  (:require [clojure.test :refer :all]
            [ratings.glicko :as glicko]))

(defn create-player []
  (assoc nil :rating 1200 :rating-rd 350 :volatility 0.06 :has-played "false"))

(defn test-one-game-winner [player]
  (testing "Testing if winner has correct rating"
    (is (= (:rating player) 1362.3108939062895))
    (is (= (:rating-rd player) 290.31896371797296))))

(defn test-one-game-loser [player])

(deftest single-game
  (let [player-a (create-player)
        player-b (create-player)]
    (test-one-game-winner (glicko/get-glicko2 player-a player-b 1))))
