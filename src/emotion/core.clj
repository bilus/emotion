(ns emotion.core
  (:gen-class)
  (:use emotion.debug)
  (:use emotion.rand)
  (:use emotion.input)
  (:use emotion.fitness)
  (:use emotion.solution)
  (:require [emotion.templates :as t]))

(def images-dir "/Users/martinb/dev/Avatar/CKDB/CK+/cohn-kanade-images")

(def inputs (doall (aus-inputs images-dir)))

(def input-vars
  (collect-input-vars inputs))

(def output-vars
  (keys (emotions)))

(def input-terms [:low :medium :high])
(def output-terms input-terms)

(def input-templ
  (t/variables-template input-vars input-terms))

(def output-templ
  (t/variables-template output-vars output-terms))

(def rules-templ
  (t/rules-template input-vars output-vars 1))

input-vars
output-vars
input-templ
output-templ
rules-templ

(let [n 64]
  (clear-log!)
  (with-rand-seed n
;;     (println n)
    (def solution-params (->SolutionParams inputs input-templ output-templ rules-templ input-vars output-vars input-terms output-terms))
    (:input-terms solution-params)
    (def solution (generate-solution solution-params))
;;     (def estimator (make-estimator (:input-templ solution-params) (:output-templ solution-params) (:rules-templ solution-params) (:inputs solution) (:outputs solution) (:rules solution)))
    (fitness solution))
  (show-log))
;; FIX ERRATIC NullPointerException
;; ADD MUTATION

;; (def solutions (take 100 (repeatedly #(generate-solution solution-params))))


;; solutions
;; (def fitnesses (doall (map fitness solutions)))

;; fitnesses
(def solution (generate-solution solution-params))
(dotimes [n 10] (println (fitness solution)))
(doseq [solution (doall (take 100 (iterate mutate solution)))]
  (println (fitness solution)))


;; WHY THERE ARE NaNS DUE TO IT BEING OUTSIDE THE RANGE?
;; RUN ESTIMATOR ON ONE INPUT
;; SEE THE FUZZY LOGIC INPUT/OUTPUT TRIANGLES

;; TODO: Generate more reasonable ranges.

;; (map #(estimator (aus-input->input-params input-vars %) output-vars)(remove (comp nil? :emotion) inputs))
;; (clear-log!)
;; (fitness estimator input-vars inputs)
(show-log)



(defn -main []
;;   (let [population generate-initial-population)
;;   Generate inital population.
;;   Create lazy seq using iterate.
;;   Partition and take pair until converges.
;;   Show the best rule set and detailed results. Save!
) ;; Being lazy doesn't pay here.

(clojure.test/run-all-tests #"^emotion.*")
