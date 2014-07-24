(ns emotion.evolution
  (:use emotion.fuzzy)
  (:use emotion.debug)
  (:use emotion.examples)
  (:use emotion.input)
  (:require [emotion.templates :as t])
    (:import (com.fuzzylite.defuzzifier WeightedAverage
                                      Centroid))
    (:import (com.fuzzylite.norm.s Maximum))
)

(defn emotions
  []
  {:happiness 1.0 :anger 0.0 :sadness 0.1 :surprise 0.5 :fear 0.0 :disgust 0.1 :contempt 0.0})

(defn estimate-emotion
  [aus-input]
  (emotions))

(defn emotion->map
  [emotion-scalar]
  (let [emotions {1 :anger 7 :surprise 6 :sadness 4 :fear 3 :disgust 5 :happiness 2 :contempt}
        zeroes (into {} (zipmap (vals emotions) (repeat 0.0)))]
    (merge zeroes {(emotions (int emotion-scalar)) 1.0})))

(def emotion->map-memo (memoize emotion->map))

(defn emotion-dist
  "Returns distance between two emotion maps. Provided values for each emotion is between [0..1] the result also stays in the same range."
  [emotion-map1 emotion-map2]
  {:pre [(> (count emotion-map1) 0) (> (count emotion-map2) 0) (= (count emotion-map1) (count emotion-map2))]}
  (let [total-error (->> (map - (vals emotion-map1) (vals emotion-map2))
                   (map #(Math/pow % 2))
                   (reduce +))
        num-items (count emotion-map1)]
    (/ total-error num-items)))


(defn make-estimator
  [input-template output-template rules-template inputs outputs rules]
  (let [engine (make-engine "tipper"
                  (t/resolve-template input-template inputs)
                  (t/resolve-template output-template outputs)
                  (t/resolve-template rules-template rules))
        status (StringBuilder.)]
      (fn [input-params output-vars]
        (doseq [[k v] input-params]
          (set-input engine k v))
        (doseq [k output-vars]
          (doto (.getOutputVariable engine (name k))
            (.. (fuzzyOutput) (setAccumulation (Maximum.)))
            (.setLockValidOutput false)
            (.setLockOutputRange false)
            (.setDefaultValue Double/NaN)
            (.setDefuzzifier (Centroid. 200))))
        (.configure engine "Minimum" "Maximum" "AlgebraicProduct" "AlgebraicSum" "Centroid")
        (.isReady engine status)
        (.process engine)
        (->> (map #(get-output engine (name %)) output-vars)
             (zipmap output-vars)))))


(defn fitness
  [estimator input-vars aus-inputs]
  (let [valid-inputs (remove (comp nil? :emotion) aus-inputs)
        total-distance (->> valid-inputs
             (map
              (juxt
               (partial (comp estimator aus-input->input-params) input-vars)
               (comp emotion->map-memo :emotion)))
             (dbg)
             (map #(apply emotion-dist %))
             (reduce +))]
    (/ total-distance (count valid-inputs))))

