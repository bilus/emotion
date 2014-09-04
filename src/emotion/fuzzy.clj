 (ns emotion.fuzzy
  (:require [clojure.string :as s])
  (:use emotion.debug)
  (:import (com.fuzzylite Engine))
  (:import (com.fuzzylite.variable InputVariable
                                   OutputVariable))

  (:import (com.fuzzylite.rule RuleBlock
                               Rule))
  (:import (com.fuzzylite.term Triangle))
  (:import (com.fuzzylite.norm.t Minimum
                                 AlgebraicProduct))
  (:import (com.fuzzylite.norm.s Maximum))
  (:import (java.lang StringBuilder)))

(defn- transpose [l]
  (apply map list l))


(defn add-input-var [engine [var-name values]]
  (let [iv (InputVariable.)
        ranges (partition 3 values)
        [_ froms tos] (transpose ranges)]
    (doto iv
      (.setName (name var-name))
      (.setRange (reduce min froms) (reduce max tos)))
    (doseq [[label from to] ranges]
      (.addTerm iv (Triangle. (name label) from to))) ;; TODO: Allow for dynamic generation using clojure.lang.Reflector to support any term.
    (.addInputVariable engine iv)
    iv))


(defn add-output-var [engine [var-name values]] ;; TODO: Refactor by extracting the code common for add-input-var and add-output-var.
  (let [iv (OutputVariable.)
        ranges (partition 3 values)
        [_ froms tos] (transpose ranges)]
    (doto iv
      (.setName (name var-name))
      (.setRange (reduce min froms) (reduce max tos)))
    (doseq [[label from to] ranges]
      (.addTerm iv (Triangle. (name label) from to))) ;; TODO: Allow for dynamic generation using clojure.lang.Reflector.
    (.addOutputVariable engine iv)
    iv))

;; (defmacro with-engine [engine engine-name input-vars output-vars rules & body]
;;   `(let [~engine (Engine. ~engine-name)]
;;      (let ~(into [] (mapcat #(list (first %) `(add-input-var ~engine [~(keyword (first %)) ~(second %)])) (partition 2 input-vars)))
;;        (let ~(into [] (mapcat #(list (first %) `(add-output-var ~engine [~(keyword (first %)) ~(second %)])) (partition 2 output-vars)));; TODO: Refactor this.
;;          (add-rules ~engine (partition 2 ~rules))
;;          ~@body))))

;; (defn- def-fuzzy-var [add engine [var-name var-def]]
;;   `(def ~var-name (~add ~engine [~(keyword var-name) ~var-def])))



(defn rule-term->str [term]
  (->> term
       (map name)
       (partition 2)
       (map #(apply str (interpose " is " %)))
       (interpose " and ")
       (apply str)))

(defn- rule->str [[ifs thens]]
  (apply str (concat
              (list "if ")
              (interpose " and " (map rule-term->str (partition 2 ifs)))
              (list " then ")
              (map rule-term->str (partition 2 thens))
              )))

(defn add-rules [engine rules]
  ; (println "\n\n\nadd-rules")
  (let [rule-block (RuleBlock.)]
    (doseq [rule rules]
      ; (println (rule->str rule))
      (.addRule rule-block (Rule/parse (rule->str rule) engine)))
;;     (.setConjunction rule-block (Minimum.))
;;     (.setDisjunction rule-block (Maximum.))
;;     (.setActivation rule-block (Minimum.))
;;     (.setName rule-block "default")
    (.addRuleBlock engine rule-block)))

;; (defmacro def-engine [engine engine-name input-vars output-vars rules]
;;   `(do
;;      (def ~engine (Engine. ~engine-name))
;;      ~@(map (partial def-fuzzy-var `add-input-var engine) (partition 2 input-vars))
;;      ~@(map (partial def-fuzzy-var `add-output-var engine) (partition 2 output-vars))
;;      (add-rules ~engine (partition 2 ~rules))))



(defn make-engine [engine-name input-vars output-vars rules]
  (dbg- input-vars)
  (dbg- output-vars)
  (let [engine (Engine. engine-name)]
    (doseq [var (partition 2 input-vars)]
      (add-input-var engine var))
    (doseq [var (partition 2 output-vars)]
      (add-output-var engine var))
    (add-rules engine (partition 2 rules))
    engine))

(defn set-input [engine var-name value]
  (.setInputValue engine (name var-name) value))

(defn get-output [engine var-name]
  (.getOutputValue engine (name var-name)))

