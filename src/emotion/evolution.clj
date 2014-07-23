(ns emotion.evolution
  (:use emotion.fuzzy)
  (:use emotion.examples)
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
          (set-input engine k v)
          (doto (.getOutputVariable engine (name k))
            (.setAccumulation (Maximum.))
            (.setLockValidOutput false)
            (.setLockOutputRange false)
            (.setDefaultValue Double/NaN)
            (.setDefuzzifier (Centroid. 200))))

        (.configure engine "Minimum" "Maximum" "AlgebraicProduct" "AlgebraicSum" "Centroid")
        (.isReady engine status)
        (.process engine)
        (into {} (map #(list % (get-output engine (name %))) output-vars)))))


(defn fitness
  [estimator aus-inputs solution]
  (let [valid-inputs (remove (comp nil? :emotion) aus-inputs)
        total-distance (->> valid-inputs
             (map (juxt estimator (comp emotion->map-memo :emotion)))
             (map #(apply emotion-dist %))
             (reduce +))]
    (/ total-distance (count valid-inputs))))

(defn make-range
  [[prev-from prev-to] [width offset]]
  (let [prev-width (- prev-to prev-from)
        from (+ prev-from (* offset prev-width))
        to (+ from width)]
    [from to]))


(defn make-ranges
  "Converts a vector of relative triangular terms widths and offsets (a representation facilitating simpler mutation and cross-over) into pairs of ranges for each term's triangle."
  {:test (examples
          (make-ranges [1.0 0.3 0.7
                       0.0 0.3 0.5]) => (list [0.0 1.0] [0.3 0.6] [0.44999999999999996 1.15]))}
  [genotype-representation]
  (let [[widths offsets] (partition (/ (count genotype-representation) 2) genotype-representation)]
    (next (reduce #(conj %1 (make-range (last %1) %2)) [[0 1]] (map vector widths offsets)))))


(defn scale-ranges
  "Keeps ranges in [0..1] uhm.. range."
  {:test (examples
          (scale-ranges [0 1 0.5 2]) => [0.0 0.5 0.25 1.0]
          (scale-ranges [0.5 1.5 1 2.5] => [0 0.5 0.25 1]))}
  [ranges]
  (let [r (partition 2 ranges)
        min-from (first ranges)
        max-to (reduce #(max %1 (second %2)) 0 r)
        scale (/ 1.0 (- max-to min-from))]
    (map (partial * scale) ranges)))


(defn terms-template ;; TODO: More generic name; it's not exclusively for terms.
  "Converts a list of terms to a parametrized template."
  {:test (examples
    (terms-template [] 0)       => []
    (terms-template [:x :y] 1)  => [:x 1 2 :y 3 4])}
  ([terms start]
    (terms-template terms start 2))
  ([terms start num-params-per-term]
    (into [] (flatten (interleave terms (partition num-params-per-term (iterate inc start)))))))


(defn variables-template
  "Converts variables and terms to a template with placeholders for ranges for each term."
  {:test (examples
          (variables-template [] []) => []
          (variables-template [:lbrow_up :rbrow_up] [:low :medium :high]) => [:lbrow_up [:low 0 1 :medium 2 3 :high 4 5]
                                                                              :rbrow_up [:low 6 7 :medium 8 9 :high 10 11]])}
  [variables terms]
  (into [] (mapcat #(vector %1 (terms-template terms (* %2 (count terms) 2))) variables (iterate inc 0))))



(defn rules-template
  "Generates a template for rules."
  {:test (examples
          (rules-template [:in1 :in2] [:out1 :out2] 2) => [[:in1  0 :in2  1] [:out1  2]
                                                           [:in1  3 :in2  4] [:out1  5]
                                                           [:in1  6 :in2  7] [:out2  8]
                                                           [:in1  9 :in2 10] [:out2 11]])}
  [input-vars output-vars num-rules-per-output-var]
  (let [outputs (mapcat #(repeat num-rules-per-output-var %) output-vars)]
    (mapcat #(let [start (* %2 (inc (count input-vars)))]
               (vector
                  (terms-template input-vars start 1)
                  (vector %1 (+ start (count input-vars)))))
            outputs (iterate inc 0))))


(clojure.test/run-tests)
