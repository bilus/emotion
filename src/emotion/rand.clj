(ns emotion.rand
  (:use emotion.examples)
  (:use clojure.test))

(def ^:dynamic *rand* clojure.core/rand)

(defn rand-1
 ([]
   (*rand* 1))
 ([n]
   (*rand* n)))

(defmacro with-rand-seed
 "Sets seed for calls to random in body. Beware of lazy seqs!"

 [seed & body]
  `(let [g# (java.util.Random. ~seed)]
     (binding [*rand* #(* % (.nextFloat g#))]
      (with-redefs [rand rand-1]
        ~@body))))


;; TODO: Move to a separate file or make work inline.
(clojure.test/deftest test-with-rand-seed ((examples
       (with-rand-seed 4
         (rand)) ~=> 0.7306094)))
