(ns emotion.core
  (:use emotion.fuzzy)
  (:require [emotion.templates :as t]))

(defn simple-dimmer [inputs outputs rules]
  (let [engine (make-engine "simple-dimmer"
    (t/resolve-template [:ambient [:dark 0 1 :medium 2 3 :bright 4 5]] inputs)
    (t/resolve-template [:power [:low 0 1 :medium 2 3 :high 4 5]] outputs)
    rules)]
    (doto engine
      (set-input :ambient 0.5)
      (.configure "" "" "AlgebraicProduct" "AlgebraicSum" "Centroid")
      (.process))
      (get-output engine :power)))


;; ;; TODO: Write clojure functions


(simple-dimmer [0.0 0.5 0.25 0.75 0.5 1.0]
               [0.0 0.5 0.25 0.75 0.5 1.0]
               [[:ambient :dark]   [:power :high]
                [:ambient :medium] [:power :medium]
                [:ambient :bright] [:power :low]])
