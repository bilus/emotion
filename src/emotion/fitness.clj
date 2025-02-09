(ns emotion.fitness
  (:use emotion.fuzzy)
  (:use emotion.debug)
  (:use alex-and-georges.debug-repl)
  (:use emotion.examples)
  (:use emotion.input)
  (:require [emotion.templates :as t])
    (:import (com.fuzzylite.defuzzifier WeightedAverage
                                      Centroid))
    (:import (com.fuzzylite.norm.s Maximum))
)

(defn emotions
  []
  {1 :anger 7 :surprise 6 :sadness 4 :fear 3 :disgust 5 :happiness 2 :contempt})


(defn emotion->map
  [emotion-scalar]
  (let [emotions (emotions)
        zeroes (into {} (zipmap (vals emotions) (repeat 0.0)))]
    (merge zeroes {(emotions (int emotion-scalar)) 1.0})))

(def emotion->map-memo (memoize emotion->map))


(defn emotion-dist
  "Returns distance between two emotion maps. Provided values for each emotion is between [0..1] the result also stays in the same range.
   A NaN = -1 and its presence deliberately results in an out-of-range result to punish estimators that return NaNs."
  {:test (examples
          (emotion-dist {:happy 0} {:happy 0})         ~=> 0.0
          (emotion-dist {:happy 0} {:happy 1})         ~=> 1.0
          (emotion-dist {:happy Float/NaN} {:happy 1}) ~=> 4.0)}
  [emotion-map1 emotion-map2]
  {:pre [(> (count emotion-map1) 0) (> (count emotion-map2) 0) (= (count emotion-map1) (count emotion-map2))]}
  (let [emotions (keys emotion-map1)
        get-val #(if (Double/isNaN %) -1 %)
        get-vals #(map get-val ((apply juxt emotions) %))
        total-error (->> (map - (get-vals emotion-map1) (get-vals emotion-map2))
                   (map #(Math/pow % 2))
                   (reduce +))
        num-items (count emotion-map1)]
    ; (println "----------------------------")    
    ; (println emotion-map1)
    ; (println emotion-map2)
    ; (println total-error)
    (/ total-error num-items)))

(defn make-estimator
  [input-template output-template rules-template inputs outputs rules]
  (let [engine (make-engine "tipper"
                  (dbg- (t/resolve-template input-template inputs))
                  (dbg- (t/resolve-template output-template outputs))
                  (dbg- (t/resolve-template rules-template rules)))
        status (StringBuilder.)]
      (fn [input-params output-vars]
        ; (println input-params)
        (doseq [[k v] (dbg- input-params)]
          (set-input engine k v))
        (doseq [k output-vars]
          (doto (.getOutputVariable engine (name k))
            (.. (fuzzyOutput) (setAccumulation (Maximum.)))
            ; (.setLockValidOutput false)
            ; (.setLockOutputRange false)
            (.setDefaultValue Double/NaN)
            (.setDefuzzifier (Centroid.))))
        (.configure engine "Minimum" "Maximum" "AlgebraicProduct" "AlgebraicSum" "Centroid")
        (.isReady engine status)
;;         (debug-repl)
        (.process engine)
        (dbg- (->> (map #(get-output engine (name %)) output-vars)
             (zipmap output-vars))))))

(defn calc-fitness
  [estimator input-vars output-vars aus-inputs]
  (let [valid-inputs (remove (comp nil? :emotion) aus-inputs)
        total-distance (->> valid-inputs
             (map
              (juxt
               :fname  
               #(estimator (aus-input->input-params input-vars %) output-vars) 
               (comp emotion->map-memo :emotion)))
             ; dbg
             (map #(apply emotion-dist (drop 1 %)))
             (reduce +))]
    ; (clojure.pprint/pprint (map :fname valid-inputs))
    (/ total-distance (count valid-inputs))))

