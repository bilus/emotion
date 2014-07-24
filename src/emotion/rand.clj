(ns emotion.rand
  (:use emotion.examples)
  (:refer-clojure :exclude [rand]))

(def ^:dynamic *rand* clojure.core/rand)

(defn rand
 []
 (*rand*))

(defmacro with-rand-seed
 "Sets seed for calls to rand in body. Beware of lazy seqs!"

 [seed & body]
  `(let [g# (java.util.Random. ~seed)
        new-rand# #(.nextFloat g#)]
   (binding [*rand* new-rand#]
      ~@body)))

;; TODO: Move to a separate file or make work inline.
(clojure.test/deftest test-with-rand-seed ((examples
       (with-rand-seed 4
         (rand)) ~=> 0.7306094)))

(with-rand-seed 4
         (rand))
