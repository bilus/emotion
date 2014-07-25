(ns emotion.solution
  (:use emotion.examples)
  (:use emotion.ranges)
  (:use emotion.evolution)
  (:require [emotion.templates :as t]))


(defn- ->ranges
  [num-vars params]
  (->> params
       (make-ranges)
       (flatten)
       (partition (/ (count params) num-vars))
       (map scale-ranges)
       (flatten)))

(defrecord SolutionParams [aus-inputs input-templ output-templ rules-templ input-vars output-vars input-terms output-terms])

(defprotocol Evolvable
  (fitness [solution])
  (mutate [solution]))

(defrecord Solution [solution-params inputs outputs rules]
  Evolvable
  (fitness [solution]
     (let [estimator (make-estimator
                        (:input-templ solution-params)
                        (:output-templ solution-params)
                        (:rules-templ solution-params)
                        (->ranges (count (:input-vars solution-params)) inputs)
                        (->ranges (count (:output-vars solution-params)) outputs)
                        rules)]
       (calc-fitness estimator (:input-vars solution-params) (:output-vars solution-params) (:aus-inputs solution-params))))

  (mutate [solution]
      solution))

(defn generate-solution
  "Generates a solution wih random values for placoholders in templates of input/output variables and fuzzy rules."
  [solution-params]
    (Solution.
       solution-params
       (t/rand-values (:input-templ solution-params)) ;; TODO: Move to templates to keep the knowledge of the shape of the template in one place.
       (t/rand-values (:output-templ solution-params)) ;; TODO: Ditto.
       (t/generate-rules-template-vals (:rules-templ solution-params) (conj (:input-terms solution-params) :any) (:output-terms solution-params)))) ;; TODO: Hard-coded :any.

