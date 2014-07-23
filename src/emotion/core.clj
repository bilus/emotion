(ns emotion.core
  (:gen-class)
  (:use emotion.input)
  (:use emotion.evolution))

(def images-dir "/Users/martinb/dev/Avatar/CKDB/CK+/cohn-kanade-images")

(def inputs (doall (aus-inputs images-dir)))

(def input-vars
  (collect-input-vars inputs))

(def output-vars
  (keys (emotions)))

(def input-terms [:low :medium :high])
(def output-terms input-terms)

(def input-templ
  (variables-template input-vars input-terms))

(def output-templ
  (variables-template output-vars output-terms))

(def rules-templ
  (rules-template input-vars output-vars 1))

input-vars
output-vars
input-templ
output-templ
rules-templ
(def make-estimator-1 (partial make-estimator input-templ output-templ rules-templ inputs))



(defn -main []
;;   (let [population generate-initial-population)
;;   Generate inital population.
;;   Create lazy seq using iterate.
;;   Partition and take pair until converges.
;;   Show the best rule set and detailed results. Save!
) ;; Being lazy doesn't pay here.

