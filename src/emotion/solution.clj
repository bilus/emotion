(ns emotion.solution
  (:use emotion.examples)
  (:use emotion.ranges)
  (:use emotion.fitness)
  (:use emotion.rand)
  (:use emotion.input)
  (:require [emotion.templates :as t]))

; TODO: Move protocols to lib/.

(defn- ->ranges
  [num-vars params]
  (->> params
       (make-ranges)
       (flatten)
       (partition (/ (count params) num-vars))
       (map fit-triangle-ranges)
       (flatten)))

(defprotocol SolutionFactory
  (generate-solution [solution-params]))

(defprotocol Evolvable
  (fitness [solution])
  (mutate [solution])
  (crossover [solution1 solution2]))

(defprotocol EmotionCapture
  (capture-emotions [solution aus-input]))

(defn- make-estimator-1 
  [solution-params inputs outputs rules]
  (make-estimator (:input-templ solution-params)
                  (:output-templ solution-params)
                  (:rules-templ solution-params)
                  (->ranges (count (:input-vars solution-params)) inputs)
                  (->ranges (count (:output-vars solution-params)) outputs)
                  rules))

(defn- fitness-1
  "A helper function to facilitate memoization of fitness."
  [solution-params inputs outputs rules]
  (let [estimator (make-estimator-1 solution-params inputs outputs rules)]
    (calc-fitness estimator (:input-vars solution-params) (:output-vars solution-params) (:aus-inputs solution-params))))

(def fitness-memo (memoize fitness-1))

(defn- mutate-vars 
  [which solution]
  (let [mutation-prob 0.05] ; TODO: Make it configurable using dynamic vars.
    (letfn [(keep-in-range [[min-val max-val] v] (min max-val (max min-val v)))
            (mutate [v] (keep-in-range [0.1 0.9] (+ v (rand-between -0.1 0.1))))  ; TODO: Make the range configurable using dynamic vars.
            (maybe-mutate [v] (rand-if mutation-prob (mutate v) v))]
    (update-in solution [which] #(map maybe-mutate %)))))

(defn mutate-rules
  [solution]
  (let [rules (:rules solution)
        input-terms (get-in solution [:solution-params :input-terms])
        output-terms (get-in solution [:solution-params :output-terms])
        mutation-prob 0.1] ; TODO: Make it configurable using dynamic vars.    
    (letfn [(mutate [term possible-terms] (rand-nth (remove #{term} possible-terms)))
            (maybe-mutate [possible-terms term] (rand-if mutation-prob (mutate term possible-terms) term))]
      (->> rules
           (t/map-rules 
             #(map (partial maybe-mutate input-terms) %) 
             #(map (partial maybe-mutate output-terms) %))
           (assoc-in solution [:rules])))))

(defn swap-halves 
  "Splits sequences in halves and exchanges the halves."
  {:test (examples
           (swap-halves [1 2 3 4] [10 20 30 40]) => [[1 2 30 40] [10 20 3 4]])}
  [lhs rhs]
  {:pre [(= (mod (count lhs) 2) (mod (count rhs) 2) 0)    ; Both lhs & rhs are divisible in half.
         (= (count lhs) (count rhs))]}
  (let [[lhs-1 lhs-2] (partition (/ (count lhs) 2) lhs)
        [rhs-1 rhs-2] (partition (/ (count rhs) 2) rhs)]
    [(concat lhs-1 rhs-2) (concat rhs-1 lhs-2)]))

(defn crossover-vars 
  [lhs rhs] 
  (first (swap-halves lhs rhs))) 

(defn crossover-rules 
  [lhs rhs] 
  (first (swap-halves lhs rhs))) 

(defrecord Solution [solution-params inputs outputs rules]
  Evolvable
  (fitness [solution]
           (fitness-memo solution-params inputs outputs rules))
  (mutate [solution]
      (letfn [(mutate-1 [] (mutate-vars (rand-nth [:inputs :outputs]) solution))
              (mutate-2 [] (mutate-rules solution))]
        ((rand-nth [mutate-1 mutate-2]))))
  (crossover [solution1 solution2]
    (let [[lhs rhs] (shuffle [solution1 solution2])]
      (-> lhs
          (assoc-in [:inputs] (crossover-vars (:inputs lhs) (:inputs rhs)))
          (assoc-in [:outputs] (crossover-vars (:outputs lhs) (:outputs rhs)))
          (assoc-in [:rules] (crossover-rules (:rules lhs) (:rules rhs))))))
  EmotionCapture
  (capture-emotions [solution aus-input]
    (let [estimator (make-estimator-1 solution-params inputs outputs rules)]
      (estimator (aus-input->input-params (:input-vars solution-params) aus-input) (:output-vars solution-params)))))



(defrecord SolutionParams [aus-inputs input-templ output-templ rules-templ input-vars output-vars input-terms output-terms]
  SolutionFactory
  (generate-solution
  [solution-params]
  "Generates a solution wih random values for placoholders in templates of input/output variables and fuzzy rules."
  (Solution.
     solution-params
     ;; TODO: Move to templates to keep the knowledge of the shape of the template in one place.
     (t/rand-values (:input-templ solution-params)) 
     (t/rand-values (:output-templ solution-params)) ;; TODO: Ditto.
     (t/generate-rules-template-vals (:rules-templ solution-params) (conj (:input-terms solution-params) :any) (:output-terms solution-params))))) ;; TODO: Hard-coded :any.



(defn load-population 
  [file-name]
  (when file-name (read-string (slurp file-name))))

(defn save-population
  [file-name population]
  (spit file-name (with-out-str (pr population))))
