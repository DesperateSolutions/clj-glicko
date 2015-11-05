(ns ratings.elo
  )

(def player1 {:name "Newbie" :rating 1200 :rating-rd 350})
(def player2 {:name "Medium" :rating 1500 :rating-rd 70})
(def player3 {:name "Oldie" :rating 1800 :rating-rd 70})

(defn- update-player-score-elo [{rating1 :rating :as player} {rating2 :rating} s]
  (let [r1 (Math/pow 10 (/ rating1 400))
        r2 (Math/pow 10 (/ rating2 400))
        e1 (/ r1 (+ r1 r2))
        r1 (+ rating1 (* 32 (- s e1)))]
    (assoc player :rating r1)))

(defn score-game [player1 player2 result]
  (cond (= 1 result)
        (do (update-player-score-elo player1 player2 1)
            (update-player-score-elo player2 player1 0))
        (= -1 result)
        (do (update-player-score-elo player2 player1 1)
            (update-player-score-elo player1 player2 0))
        :else
        (do (update-player-score-elo player1 player2 0.5)
            (update-player-score-elo player2 player1 0.5))))


;;This should probably go into a file of its own for Glicko
;;We also want to just calculate it all in the main functions and send on - This is a lot of double shit
(defn update-rd [{rd :rating-rd :as player}]
  (if (= rd 0)
    350
    (min 350 (Math/sqrt (+ (* rd rd) (* (* 30 30) 1))))))

(defn- get-q []
  0.0057565)

(defn- get-g [rd]
  (/ 1 (Math/sqrt (+ 1 (/ (* 3 (Math/pow (get-q) 2) (Math/pow rd 2)) (Math/pow Math/PI 2))))))

(defn- get-e [rating1 rating2 rd]
  (/ 1 (+ 1 (Math/pow 10 (/ (* (- (get-g rd)) (- rating1 rating2)) 400)))))

(defn- get-d [{rating1 :rating :as player1} {rating2 :rating rd :rating-rd :as player2} g e]
  (Math/pow (* (Math/pow (get-q) 2) (Math/pow g 2) e (- 1 e)) -1))

(defn- new-rd [rd d]
  (Math/sqrt (Math/pow (+ (/ 1 (Math/pow rd 2)) (/ 1 (Math/pow d 2))) -1))
  )

(defn new-rating [{rating1 :rating rd1 :rating-rd :as player1} {rating2 :rating rd2 :rating-rd :as player2} result]
  (let [q (get-q)
        g (get-g rd2)
        e (get-e rating1 rating2 rd2)
        d (get-d player1 player2 g e)
        new-rating (+ rating1 (* (/ q (+ (/ 1 (Math/pow rd1 2)) (/ 1 (Math/pow d 2)))) g (- result e)))
        new-rd (new-rd rd1 d)]
    (assoc player1 :rating new-rating :rating-rd new-rd)))
