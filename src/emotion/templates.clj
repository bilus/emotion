(ns emotion.templates
  (:use emotion.examples)
  (:use emotion.rand)
  (:use emotion.debug)
  (:require [clojure.template :as t])
  (:require [clojure.walk :as w]))

(defn resolve-template
  "Replaces numbered placeholders with successive values."
  {:test (examples
          (resolve-template [:dark 0 1 :medium 2 3 :bright 4 5] [0.0 0.5 0.25 0.75 0.5 1.0]) => [:dark 0.0 0.5 :medium 0.25 0.75 :bright 0.5 1.0])}
  [expr values]
  (w/prewalk-replace (zipmap (iterate inc 0) (flatten values)) expr))

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


(defn- generate-nums
  [n]
  (take n (repeatedly rand)))

(defn- random-sample
  [n values]
  (take n (repeatedly #(rand-nth values))))

(defn rand-values
  "Generate values for placeholders in a template."
  ([template]
    (-> (count-placeholders template)
        (generate-nums)
        (doall)
        vec)) ;; because of with-rand-seed binding
  ([template values]
    (-> (count-placeholders template)
        (random-sample values)
        (doall)
        vec))) ;; because of with-rand-seed binding

(defn map-rules 
  "Maps rules template or rules params apply f-ifs to IF clauses (inputs) and f-thens to THEN clauses (outputs)."
  [f-ifs f-thens rules]
  (->> (partition 2 rules)
       (mapcat #(let [i %1 
                      [ifs thens] %2] 
                  (vector (f-ifs i ifs) (f-thens i thens))) (iterate inc 0))
       vec))

(defn generate-rules-template-vals
  "Generate randomly sampled terms for a rules template."
  {:test (examples (with-rand-seed 0 (generate-rules-template-vals [[:in1  0 :in2  1] [:out1  2]
                                                                    [:in1  3 :in2  4] [:out1  5]
                                                                    [:in1  6 :in2  7] [:out2  8]
                                                                    [:in1  9 :in2 10] [:out2 11]]
                                                                   [:yes :no]
                                                                   [:low :high])) 
                   => [[:no :no] [:low] [:yes :no] [:high] [:no :yes] [:low] [:no :yes] [:high]])}
  [rules-templ input-terms output-terms]
  (letfn [(ifs [i templ] 
               (rand-values templ input-terms))
          (thens [i templ] 
                 (vector (nth (cycle output-terms) i)))]
    (map-rules ifs thens rules-templ))) ; Assumes there's only 1 placeholder per then clause.
