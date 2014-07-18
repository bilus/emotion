(ns emotion.core
  (:import (com.fuzzylite Engine))
  (:import (com.fuzzylite.variable InputVariable
                                   OutputVariable))
  
  (:import (com.fuzzylite.rule RuleBlock
                               Rule))
  (:import (com.fuzzylite.term Triangle))
  (:import (java.lang StringBuilder)))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))


(def engine (Engine. "simple-dimmer"))

(def ambient (InputVariable.))

(doto ambient 
    (.setName "Ambient")
    (.setRange 0.0 1.0)
    (.addTerm (Triangle. "DARK" 0.0 0.5))
    (.addTerm (Triangle. "MEDIUM" 0.25 0.75))
    (.addTerm (Triangle. "BRIGHT" 0.5 1.0)))
(.addInputVariable engine ambient)

(def power (OutputVariable.))

(doto power
  (.setName "Power")
  (.setRange 0.0 1.0)
  (.setDefaultValue Double/NaN)
  (.addTerm (Triangle. "LOW" 0.0 0.5))
  (.addTerm (Triangle. "MEDIUM" 0.25 0.75))
  (.addTerm (Triangle. "HIGH" 0.5 1.0)))

(.addOutputVariable engine power)

(def rule-block RuleBlock.)

(doto rule-block
  (.addRule (Rule/parse "if Ambient is DARK then Power is HIGH" engine))
  (.addRule (Rule/parse "if Ambient is MEDIUM then Power is MEDIUM" engine))
  (.addRule (Rule/parse "if Ambient is BRIGHT then Power is LOW" engine)))

(.addRuleBlock engine rule-block)

(.configure engine "" "" "AlgebraicProduct" "AlgebraicSum" "Centroid")

(def status StringBuilder.)
(.isReady engine status)

(.setInputValue ambient 0.5)

(.process engine)

(.defuzzify power)

5
