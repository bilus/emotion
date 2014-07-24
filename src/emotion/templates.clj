(ns emotion.templates
  (:use emotion.examples)
  (:require [clojure.template :as t])
  (:require [clojure.walk :as w]))

(defn resolve-template
  "Replaces numbered placeholders with successive values."
  {:test (examples
          (resolve-template [:dark 0 1 :medium 2 3 :bright 4 5] [0.0 0.5 0.25 0.75 0.5 1.0]) => [:dark 0.0 0.5 :medium 0.25 0.75 :bright 0.5 1.0])}
  [expr values]
  (w/prewalk-replace (zipmap (iterate inc 0) values) expr))

(defn count-placeholders
  "Counts numbered placeholders in a template."
  [template]
  (->> template
       (flatten)
       (filter integer?)
       (count)))

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
