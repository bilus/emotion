(ns emotion.floats)

(defn- scale [x y]
  (if (or (zero? x) (zero? y))
    1
    (Math/abs x)))

(defn float=
  "Float (near-)equality test."
  ([x y]
   (float= x y 0.00001))
  ([x y epsilon]
   (let [eq #(<= (Math/abs (- %1 %2))
                  (* (scale %1 %2) epsilon))]
    (if (every? sequential? [x y])
      (every? (partial apply eq) (zipmap x y))
      (eq x y)))))

(defn float<
  ([x y] (float< x y 0.00001))
  ([x y epsilon] (< x
                    (- y (* (scale x y) epsilon)))))

(defn float>
  ([x y] (float< y x))
  ([x y epsilon] (float< y x epsilon)))

(defn float<=
  ([x y] (not (float> x y)))
  ([x y epsilon] (not (float> x y epsilon))))

(defn float>=
  ([x y] (not (float< x y)))
  ([x y epsilon] (not (float< x y epsilon))))