 (ns emotion.core
  (:require [clojure.string :as s])
  (:import (com.fuzzylite Engine))
  (:import (com.fuzzylite.variable InputVariable
                                   OutputVariable))

  (:import (com.fuzzylite.rule RuleBlock
                               Rule))
  (:import (com.fuzzylite.term Triangle))
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
      (println (name label))
      (.addTerm iv (Triangle. (name label) from to))) ;; TODO: Allow for dynamic generation using clojure.lang.Reflector.
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
      (println (name label))
      (.addTerm iv (Triangle. (name label) from to))) ;; TODO: Allow for dynamic generation using clojure.lang.Reflector.
    (.addOutputVariable engine iv)
    iv))

(defmacro with-engine [engine engine-name input-vars output-vars & body]
  `(let [~engine (Engine. ~engine-name)]
     (let ~(into [] (mapcat #(list (first %) `(add-input-var ~engine [~(keyword (first %)) ~(second %)])) (partition 2 input-vars)))
       (let ~(into [] (mapcat #(list (first %) `(add-output-var ~engine [~(keyword (first %)) ~(second %)])) (partition 2 output-vars)));; TODO: Refactor this.
       ~@body))))

(defn- def-fuzzy-var [add engine [var-name var-def]]
  `(def ~var-name (~add ~engine [~(keyword var-name) ~var-def])))



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

(defn- add-rules [engine rules]
  (let [rule-block (RuleBlock.)]
    (doseq [rule rules]
      (println (rule->str rule))
      (.addRule rule-block (Rule/parse (rule->str rule) engine)))
    (.addRuleBlock engine rule-block)))

(defmacro def-engine [engine engine-name input-vars output-vars rules]
  `(do
     (def ~engine (Engine. ~engine-name))
     ~@(map (partial def-fuzzy-var `add-input-var engine) (partition 2 input-vars))
     ~@(map (partial def-fuzzy-var `add-output-var engine) (partition 2 output-vars))
     (add-rules ~engine (partition 2 ~rules))))


(def-engine engine "simple-dimmer"
  [ambient [:dark 0.0 0.5 :medium 0.25 0.75 :bright 0.5 1.0]]
  [power [:low 0.0 0.5 :medium 0.25 0.75 :high 0.5 1.0]]
  [[:ambient :dark]   [:power :high]
   [:ambient :medium] [:power :medium]
   [:ambient :bright] [:power :low]])

(clojure.pprint/pprint (macroexpand-1 '(def-engine engine "simple-dimmer"
  [ambient [:dark 0.0 0.5 :medium 0.25 0.75 :bright 0.5 1.0]]
  [power [:low 0.0 0.5 :medium 0.25 0.75 :high 0.5 1.0]]
  [[:ambient :dark]   [:power :high]
   [:ambient :medium] [:power :medium]
   [:ambient :bright] [:power :bright]])))

engine

ambient

power



;(def engine (Engine. "simple-dimmer"))

;(def ambient (add-input-var engine [:ambient [:dark 0.0 0.5 :medium 0.25 0.75 :bright 0.5 1.0]]))


;(def ambient (InputVariable.))

;(doto ambient
;    (.setName "Ambient")
;    (.setRange 0.0 1.0)
;    (.addTerm (Triangle. "DARK" 0.0 0.5))
;    (.addTerm (Triangle. "MEDIUM" 0.25 0.75))
;    (.addTerm (Triangle. "BRIGHT" 0.5 1.0)))
;(.addInputVariable engine ambient)

;; (def power (OutputVariable.))

;; (doto power
;;   (.setName "Power")
;;   (.setRange 0.0 1.0)
;;   (.setDefaultValue Double/NaN)
;;   (.addTerm (Triangle. "LOW" 0.0 0.5))
;;   (.addTerm (Triangle. "MEDIUM" 0.25 0.75))
;;   (.addTerm (Triangle. "HIGH" 0.5 1.0)))

;; (.addOutputVariable engine power)

;; (def rule-block (RuleBlock.))

;; (doto rule-block
;;   (.addRule (Rule/parse "if ambient is dark then power is high" engine))
;;   (.addRule (Rule/parse "if ambient is medium then power is medium" engine))
;;   (.addRule (Rule/parse "if ambient is bright then power is low" engine)))
;; ambient

;; (.addRuleBlock engine rule-block)

(.configure engine "" "" "AlgebraicProduct" "AlgebraicSum" "Centroid")

(def status (StringBuilder.))
(.isReady engine status)
status

(.setInputValue ambient 0.3)

(.process engine)

(.defuzzify power)
