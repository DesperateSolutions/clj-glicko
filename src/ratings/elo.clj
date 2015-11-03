(ns ratings.elo)


(defn- update-player-score [{rating1 :rating :as player} {rating2 :rating} s]
  (let [r1 (Math/pow 10 (/ rating1 400))
        r2 (Math/pow 10 (/ rating2 400))
        e1 (/ r1 (+ r1 r2))
        r1 (+ rating1 (* 32 (- s e1)))]
    (assoc player :rating r1)))

(defn score-game [player1 player2 result]
  (cond (= 1 result)
        (do (update-player-score player1 player2 1)
            (update-player-score player2 player1 0))
        (= -1 result)
        (do (update-player-score player2 player1 1)
            (update-player-score player1 player2 0))
        :else
        (do (update-player-score player1 player2 0.5)
            (update-player-score player2 player1 0.5))))
