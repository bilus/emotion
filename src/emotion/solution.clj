(ns emotion.solution
  (:use emotion.examples)
  (:use emotion.ranges)
  (:use emotion.fitness)
  (:use emotion.rand)
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

(defn- fitness-1
  "A helper function to facilitate memoization of fitness."
  [solution-params inputs outputs rules]
  (let [estimator (make-estimator
                  (:input-templ solution-params)
                  (:output-templ solution-params)
                  (:rules-templ solution-params)
                  (->ranges (count (:input-vars solution-params)) inputs)
                  (->ranges (count (:output-vars solution-params)) outputs)
                  rules)]
    (calc-fitness estimator (:input-vars solution-params) (:output-vars solution-params) (:aus-inputs solution-params))))

(def fitness-memo (memoize fitness-1))

(defn- mutate-vars 
  [which solution]
  (let [mutation-prob 0.05] ; TODO: Make it configurable using dynamic vars.
    (letfn [(keep-positive [v] (max 0 v))
            (mutate [v] (keep-positive (+ v (rand-between -0.1 0.1))))  ; TODO: Make the range configurable using dynamic vars.
            (maybe-mutate [v] (rand-if mutation-prob (mutate v) v))]
    (update-in solution [which] #(map maybe-mutate %)))))

(defn mutate-rules
  [solution]
  (let [rules (:rules solution)
        input-terms (get-in solution [:solution-params :input-terms])
        output-terms (get-in solution [:solution-params :output-terms])
        mutation-prob 0.3] ; TODO: Make it configurable using dynamic vars.    
    (letfn [(mutate [term possible-terms] (rand-nth (remove #{term} possible-terms)))
            (maybe-mutate [possible-terms term] (rand-if mutation-prob (mutate term possible-terms) term))]
      (->> rules
           (t/map-rules 
             #(map (partial maybe-mutate input-terms) %) 
             #(map (partial maybe-mutate output-terms) %))
           (assoc-in solution [:rules])))))

(defrecord Solution [solution-params inputs outputs rules]
  Evolvable
  (fitness [solution]
           (fitness-memo solution-params inputs outputs rules))

  (mutate [solution]
      (letfn [(mutate-1 [] (mutate-vars (rand-nth [:inputs :outputs]) solution))
              (mutate-2 [] (mutate-rules solution))]
        ((rand-nth [mutate-1 mutate-2])))))

(defn generate-solution
  "Generates a solution wih random values for placoholders in templates of input/output variables and fuzzy rules."
  [solution-params]
    (Solution.
       solution-params
       (t/rand-values (:input-templ solution-params)) ;; TODO: Move to templates to keep the knowledge of the shape of the template in one place.
       (t/rand-values (:output-templ solution-params)) ;; TODO: Ditto.
       (t/generate-rules-template-vals (:rules-templ solution-params) (conj (:input-terms solution-params) :any) (:output-terms solution-params)))) ;; TODO: Hard-coded :any.

