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
