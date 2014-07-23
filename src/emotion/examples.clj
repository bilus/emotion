(ns emotion.examples) ;; TODO: Put into /lib

(defmacro examples [& expressions]
  (list 'fn []
    (concat (list 'do)
      (for [[actual _ expected] (partition 3 expressions)]
        (list 'clojure.test/is (list '= actual expected))))))
