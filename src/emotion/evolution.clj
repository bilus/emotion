(ns emotion.evolution
  (:use emotion.examples)
  (:use emotion.solution)
  (:use emotion.debug)
  (:require [clojure.math.combinatorics :as combo])
  (:require (bigml.sampling [simple :as simple])))

(defn- total-fitness
  [population]
  (reduce + (map fitness population)))

(defn- min-fitness
  [population]
  (reduce min (map fitness population)))

(defn- pick-candidates 
  [percent population]
  (let [max-count (* (count population) percent)
        total-fitness (total-fitness population)]
	 (take max-count (simple/sample population :weigh #(- 1 (/ (Math/pow (fitness %) 2) (+ total-fitness 1)))))))

(defn- mate 
  [solutions]
  {:pre [(> (count solutions) 1)]}
  (let [pairs (combo/combinations solutions 2)]
    (map #(apply crossover %) pairs)))


(defn initial-population [solution-params n]
  (take n (repeatedly #(generate-solution solution-params))))

(defn tournament-selection
  [tournament-size population]
  (let [child
        (fn []
          (let [selected (sort-by fitness 
                                  (repeatedly tournament-size
                                              #(rand-nth population)))]
            (crossover (first selected) (second selected))))]
    (repeatedly child)))

(defn- evolve-rank 
  [population]
  (let [crossover-candidates (pick-candidates 0.5 population)
        mutation-candidates (pick-candidates 0.3 population)
        survivors (pick-candidates 0.2 population) ; Somehow doesn't work.
        crossed-over (mate crossover-candidates)
        mutated (map mutate mutation-candidates)
        new-population (concat survivors crossed-over mutated)]
    (println ["if" (total-fitness survivors) (total-fitness crossed-over) (total-fitness mutated)])
    (println ["mf" (min-fitness survivors) (min-fitness crossed-over) (min-fitness mutated)])
    (println ["ic" (count survivors) (count crossed-over) (count mutated)])
    ; (println (total-fitness (pick-candidates 0.5 population)))
    (take (count population) (sort-by fitness new-population))))

(defn elitism 
  [best population]
  (conj (rest population) best))

(defn evolve-tournament
  [population]
  (let [best (first (sort-by fitness population))] ; TODO: It's lazy so it's probably efficent but benchmark it anyway.
    (->> population
       (tournament-selection 7) ; Tournament size.
       (take (count population))
       (map mutate)
       (elitism best))))

(defn evolve [population]
  (evolve-tournament population))



