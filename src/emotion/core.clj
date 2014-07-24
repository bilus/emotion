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

(defrecord Solution [inputs outputs rules])

(with-rand-seed 0
  (def solution (Solution.
                 (generate-placeholders input-templ)
                 (generate-placeholders output-templ)
                 (generate-placeholders rules-templ (conj output-terms :any)))))

(defn- ->ranges
  [input-params]
  (->> input-params
       (make-ranges)
       (flatten)
       (scale-ranges)))

(def make-estimator-1 (partial make-estimator input-templ output-templ rules-templ))

(def estimator (make-estimator-1 (->ranges (:inputs solution)) (->ranges (:outputs solution)) (:rules solution)))


(first inputs)

(estimator (aus-input->input-params input-vars (first inputs)) output-vars)

(estimator )

(clear-log!)
(fitness estimator input-vars inputs)
(show-log)

(first inputs)
(aus-input->input-params (first inputs))

((comp estimator aus-input->input-params) (first inputs))
(defn -main []
;;   (let [population generate-initial-population)
;;   Generate inital population.
;;   Create lazy seq using iterate.
;;   Partition and take pair until converges.
;;   Show the best rule set and detailed results. Save!
) ;; Being lazy doesn't pay here.

(clojure.test/run-all-tests #"^emotion.*")
