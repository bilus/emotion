(ns emotion.core
  (:gen-class)
  (:use emotion.debug)
  (:use emotion.rand)
  (:use emotion.input)
  (:use emotion.evolution)
  (:use emotion.ranges)
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


(defn generate-nums
  [n]
  (take n (repeatedly rand)))

(defn random-sample
  [n values]
  (take n (repeatedly #(rand-nth values))))

(defn generate-placeholders
  ([template]
    (-> (t/count-placeholders template)
        (generate-nums)
        (doall))) ;; because of with-rand-seed binding
  ([template values]
    (-> (t/count-placeholders template)
        (random-sample values)
        (doall)))) ;; because of with-rand-seed binding


(defrecord SolutionParams [aus-inputs input-templ output-templ rules-templ input-vars output-vars input-terms output-terms])

(defprotocol EvolvableSolution
  (fitness [solution])
  (mutate [solution]))

(defrecord Solution [solution-params inputs outputs rules]
  EvolvableSolution
  (fitness [solution]
     (let [make-estimator-1 (partial make-estimator input-templ output-templ rules-templ)
           estimator (make-estimator-1 (->ranges (count (:input-vars solution-params)) inputs) (->ranges (count output-vars) outputs) rules)]
       (calc-fitness estimator (:input-vars solution-params) (:output-vars solution-params) (:aus-inputs solution-params))))

  (mutate [solution] solution))

(defn generate-solution
  [solution-params]
    (Solution.
       solution-params
       (generate-placeholders input-templ)
       (generate-placeholders output-templ)
       (generate-placeholders rules-templ (conj (:output-terms solution-params) :any))))

(defn- ->ranges
  [num-vars params]
  (->> params
       (make-ranges)
       (flatten)
       (partition (/ (count params) num-vars))
       (map scale-ranges)
       (flatten)))

(def solution-params (SolutionParams. inputs input-templ output-templ rules-templ input-vars output-vars input-terms output-terms))
(def solution (generate-solution solution-params))
(fitness solution)

;; FIX ERRATIC NullPointerException
(def solutions (take 5 (map fitness (repeatedly #(generate-solution solution-params)))))

solutions

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
