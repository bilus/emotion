(ns emotion.examples) ;; TODO: Put into /lib

(defn- scale [x y]
  (if (or (zero? x) (zero? y))
    1
    (Math/abs x)))

(defn float=
  ([x y] (float= x y 0.00001))
  ([x y epsilon] (<= (Math/abs (- x y))
                     (* (scale x y) epsilon))))

(defmacro examples [& expressions] ;; TODO: Rewrite using syntax quote.
  (list 'fn []
    (concat (list 'do)
      (for [[actual op expected] (partition 3 expressions)]
        (cond
         (= op '=>) (list 'clojure.test/is (list '= actual expected))
         (= op '~=>) (list 'clojure.test/is (list `float= actual expected)))))))
