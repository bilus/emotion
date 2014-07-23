(ns emotion.scratchpad
  (:import (com.fuzzylite.defuzzifier WeightedAverage
                                      Centroid))
    (:import (com.fuzzylite.norm.s Maximum)))


;; (defn simple-dimmer [inputs outputs rules]
;;   (let [engine (make-engine "simple-dimmer"
;;     (t/resolve-template [:ambient [:dark 0 1 :medium 2 3 :bright 4 5]
;;                          ] inputs)
;;     (t/resolve-template [:power [:low 0 1 :medium 2 3 :high 4 5]] outputs)
;;     rules)
;;         status (StringBuilder.)
;;         power (.getOutputVariable engine "power")]
;;       (set-input engine :ambient 1.09)
;;       (.configure engine "" "" "AlgebraicProduct" "AlgebraicSum" "Centroid")
;;       (.setDefuzzifier power (Centroid. 200))
;;       (.setAccumulation (.fuzzyOutput power) (Maximum.))
;;       (.setLockValidOutput power false)
;;       (.setLockOutputRange power false)
;;       (.setDefaultValue power Double/NaN)
;;       (.setDefuzzifier (.getOutputVariable engine "power") (Centroid.))
;;       (.isReady engine status)
;;       (.process engine)
;;       (get-output engine :power)))


;; ;; ;; TODO: Write clojure functions


;; (simple-dimmer [0.0 0.5 0.25 0.75 0.5 1.1]
;;                [0.0 0.5 0.25 0.75 0.5 1.0]
;;                [[:ambient :dark] [:power :high]
;;                 [:ambient :medium] [:power :medium]
;;                 [:ambient :bright] [:power :low]])




;; (defn tipper [inputs outputs rules]
;;   (let [engine (make-engine "tipper"
;;     (t/resolve-template [:food [:cheap 0 1 :average 2 3 :good 4 5]
;;                          :service [:poor 6 7 :average 8 9 :good 10 11]
;;                          ] inputs)
;;     (t/resolve-template [:tip [:low 0 1 :medium 2 3 :high 4 5]] outputs)
;;     rules)
;;         status (StringBuilder.)
;;         tip (.getOutputVariable engine "tip")]
;;       (set-input engine :food 1.0)
;;       (set-input engine :service 1.0)
;;       (.configure engine "Minimum" "Maximum" "AlgebraicProduct" "AlgebraicSum" "Centroid")
;;       (.setDefuzzifier tip (Centroid. 200))
;;       (.setAccumulation (.fuzzyOutput tip) (Maximum.))
;;       (.setLockValidOutput tip false)
;;       (.setLockOutputRange tip false)
;;       (.setDefaultValue tip Double/NaN)
;;       (.isReady engine status)
;;       (.process engine)
;;       (get-output engine :tip)))


;; ;; ;; TODO: Write clojure functions


;; (tipper [-0.1 0.5 0.25 0.75 0.5 1.1
;;                 -0.1 0.5 0.25 0.75 0.5 1.1]
;; ;;                 0.0 1.0  0.5 1.5  1.0 2.0]
;;                [0.0 7.0 6.0 15.0 12.0 30.0]
;;                [[:food :cheap :service :poor] [:tip :low]
;;                 [:food :cheap :service :good] [:tip :medium]
;;                 [:food :average :service :good] [:tip :high],
;;                 [:food :good :service :average] [:tip :high]
;;                 [:food :good :service :good] [:tip :high]])



