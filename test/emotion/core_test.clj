(ns emotion.core-test
  (:require [clojure.test :refer :all]
            [emotion.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (def-engine e "test" [])
    (is (not (nil? e)))))
