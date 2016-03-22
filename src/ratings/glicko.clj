(ns ratings.glicko)

;;Glicko2

;;Step 2
(defn convert-rating-to-glicko2 [rating]
  (/ (- rating 1500) 173.7178))

(defn convert-rd-to-glicko2 [rd]
  (/ rd 173.7178))


;;Step 3
(defn get-volatile-g [rd]
  (/ 1 (Math/sqrt (+ 1 (/ (* 3 (Math/pow rd 2)) (Math/pow (Math/PI) 2))))))

(defn get-volatile-e [rating1 rating2 g]
  (/ 1 (+ 1 (Math/exp (* (* -1 g) (- rating1 rating2))))))

(defn get-variance [e g]
  (/ 1 (* (Math/pow g 2) e (- 1 e))))


;;Step 4
(defn get-delta [e g v result]
  (* v g (- result e)))

;;Step 5
;;The F(x) function we got. Set up as partial most of the time
(defn get-f [a delta rd v tau x]
  (- (/ (* (Math/exp x) (- (Math/pow delta 2) (Math/pow rd 2) v (Math/exp x))) (* 2 (Math/pow (+ (Math/pow rd 2) v (Math/exp x)) 2))) (/ (- x a) (Math/pow tau 2))))

;;Helper Function for getting B
(defn get-b [k r a delta rd v f]
  (if (< (f (- a (* k r))) 0)
    (recur (inc k) r a delta rd v f)
    (- a (* k r) delta rd v)))

;;Helper function for iterative part of Step 5
(defn get-ab [a b fa fb delta rd v r epsilon f]
  (if (> (Math/abs (- a b)) epsilon)
    (let [c (+ a (/ (* (- a b) fa) (- fb fa)))
          fc (f c)]
      (if (< (* fc fb) 0)        
        (recur b c fb fc delta rd v r epsilon f)
        (recur a c (/ fa 2) fc delta rd v r epsilon f)))
    (Math/exp (/ a 2))))

;;The setup for the iterative part of Step 5
(defn new-volatile [delta rd volatility v tau]
  (let [epsilon 0.000001
        a (Math/log (Math/pow volatility 2))
        f (partial get-f a delta rd v tau)
        b (if (> (Math/pow delta 2) (+ (Math/pow rd 2) v))
            (Math/log (- (Math/pow delta 2) (Math/pow rd 2) v))
            (get-b 1 tau a delta rd v f))]
    (get-ab a b (f a) (f b) delta rd v tau epsilon f)))

;;Step 6
(defn pre-rating-rd [rd rd-marked]
  (Math/sqrt (+ (Math/pow rd 2) (Math/pow rd-marked 2))))

;;Step 7
(defn get-rd-marked [rd-starred v]
  (/ 1 (Math/sqrt (+ (/ 1 (Math/pow rd-starred 2)) (/ 1 v)))))

(defn get-rating-marked [rating rd-marked g result e]
  (+ rating (* (Math/pow rd-marked 2) g (- result e))))


;;Only if no games in period
(defn update-rd-no-games [{volatility :volatility rd :rating-rd :as player}]
  (assoc player :rating-rd (* 173.7178 (pre-rating-rd (convert-rd-to-glicko2 rd) volatility))))


;;Actual maintainer
(defn get-glicko2 [{rating1 :rating rd1 :rating-rd volatility :volatility :as player} {rating2 :rating rd2 :rating-rd} result]
  (let [;;Step 2
        rating1 (convert-rating-to-glicko2 rating1)
        rd1 (convert-rd-to-glicko2 rd1)
        rating2 (convert-rating-to-glicko2 rating2)
        rd2 (convert-rd-to-glicko2 rd2)
        ;;Step 3
        g (get-volatile-g rd2)
        e (get-volatile-e rating1 rating2 g)
        variance (get-variance e g)
        ;;Step 4
        delta (get-delta e g variance result)
        ;;Step 5
        volatility-marked (new-volatile delta rd1 volatility variance 0.5)
        ;;Step 6
        rd-starred (pre-rating-rd rd1 volatility-marked)
        ;;Step 7 
        rd-marked (get-rd-marked rd-starred variance)
        rating-marked (get-rating-marked rating1 rd-marked g result e)]
    ;;Step 8
    (assoc player :rating (+ 1500 (* 173.7178 rating-marked)) :rating-rd (* 173.7178 rd-marked) :volatility volatility-marked :has-played "true")))
