(ns ratings.glicko)

;;Glicko2
(defn convert-rating-to-glicko2 [rating]
  (/ (- rating 1500) 173.7178))

(defn convert-rd-to-glicko2 [rd]
  (/ rd 173.7178))

(defn get-volatile-g [rd]
  (/ 1 (Math/sqrt (+ 1 (/ (* 3 (Math/pow rd 2)) (Math/pow (Math/PI) 2))))))

(defn get-volatile-e [rating1 rating2 g]
  (/ 1 (+ 1 (Math/exp (* (* -1 g) (- rating1 rating2))))))

(defn get-v [e g]
  (/ 1 (* (Math/pow g 2) e (- 1 e))))

(defn get-delta [e g v result]
  (* v g (- result e)))

(defn get-f [x delta rd v r a]
  (- (/ (* (Math/exp x) (- (Math/pow delta 2) (Math/pow rd 2) v (Math/exp x))) (* 2 (Math/pow (+ (Math/pow rd 2) v (Math/exp x)) 2))) (/ (- x a) (Math/pow r 2))))

(defn get-b [k r a delta rd v]
  (if (< (get-f (- a (* k r)) delta rd v r a) 0)
    (recur (inc k) r a delta rd v)
    (- a (* k r) delta rd v)))

(defn get-ab [a b fa fb delta rd v r epsilon]
  (if (> (Math/abs (- a b)) epsilon)
    (let [c (+ a (/ (* (- a b) fa) (- fb fa)))
          fc (get-f c delta rd v r a)]
      (if (< (* fc fb) 0)        
        (recur b c fb fc delta rd v r epsilon)
        (recur a c (/ fa 2) fc delta rd v r epsilon)))
    (Math/exp (/ a 2))))

(defn new-volatile [delta rd volatility v r]
  (let [epsilon 0.000001
        a (Math/log (Math/pow volatility 2))
        b (if (> (Math/pow delta 2) (+ (Math/pow rd 2) v))
            (Math/log (- (Math/pow delta 2) (Math/pow rd 2) v))
            (get-b 1 r a delta rd v))
        fa (get-f a delta rd v r a)
        fb (get-f b delta rd v r a)]
    (get-ab a b fa fb delta rd v r epsilon)))

(defn pre-rating-rd [rd rd-marked]
  (Math/sqrt (+ (Math/pow rd 2) (Math/pow rd-marked 2))))

(defn get-rd-marked [rd-starred v]
  (/ 1 (Math/sqrt (+ (/ 1 (Math/pow rd-starred 2)) (/ 1 v)))))

(defn get-rating-marked [rating rd-marked g result e]
  (+ rating (* (Math/pow rd-marked 2) g (- result e))))

(defn update-rd-no-games [{volatility :volatility rd :rating-rd :as player}]
  (assoc player :rating-rd (* 173.7178 (pre-rating-rd (convert-rd-to-glicko2 rd) volatility))))

(defn get-glicko2 [{rating1 :rating rd1 :rating-rd volatility :volatility :as player} {rating2 :rating rd2 :rating-rd} result]
  (let [rating1 (convert-rating-to-glicko2 rating1)
        rd1 (convert-rd-to-glicko2 rd1)
        rating2 (convert-rating-to-glicko2 rating2)
        rd2 (convert-rd-to-glicko2 rd1)
        g (get-volatile-g rd2)
        e (get-volatile-e rating1 rating2 g)
        v (get-v e g)
        delta (get-delta e g v result)
        volatility-marked (new-volatile delta rd1 volatility v 0.5)
        rd-starred (pre-rating-rd rd1 volatility-marked)
        rd-marked (get-rd-marked rd-starred v)
        rating-marked (get-rating-marked rating1 rd-marked g result e)]
    (assoc player :rating (+ 1500 (* 173.7178 rating-marked)) :rating-rd (* 173.7178 rd-marked) :volatility volatility-marked)))
