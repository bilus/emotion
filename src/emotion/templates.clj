(ns emotion.templates
  (:require [clojure.template :as t])
  (:require [clojure.walk :as w]))

(defn resolve-template [expr values]
  (w/prewalk-replace (zipmap (iterate inc 0) values) expr))

;; Example:
;;
;; (resolve-template '[:dark 0 1 :medium 2 3 :bright 4 5] [0.0 0.5 0.25 0.75 0.5 1.0])

