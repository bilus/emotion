(ns emotion.examples
  (:use emotion.floats)) ;; TODO: Put into /lib

;; TODO: Support ~=> with arbitrarily-nested seqs and maps.


(defmacro examples [& expressions] ;; TODO: Rewrite using syntax quote.
  (list 'fn []
    (concat (list 'do)
      (for [[actual op expected] (partition 3 expressions)]
        (cond
         (= op '=>) (list 'clojure.test/is (list '= actual expected))
         (= op '~=>) (list 'clojure.test/is (list `float= actual expected)))))))
