(ns emotion.ranges
  (:use emotion.debug)
  (:use emotion.examples)
  (:use emotion.floats))

(defn make-range
  [[prev-from prev-to] [from-offset to-offset]]
  (let [prev-width (- prev-to prev-from)
        from (+ prev-from (* from-offset prev-width))
        to (+ prev-to to-offset)]
    ; (println ["make-range" [prev-from prev-to] [from-offset to-offset] [from to]])
    ; (println ["prev-width" prev-width])
    [from to]))


(defn make-ranges
  "Converts a vector of relative triangular terms widths and offsets (a representation facilitating simpler mutation and cross-over) into pairs of ranges for each term's triangle."
  {:test (examples
          (make-ranges [0.0 0.3 0.5
                        1.0 0.3 0.7]) => (list [0.0 2.0] [0.6 2.3] [1.4499999999999997 3.0])
          (make-ranges [0
                        1]) => (list [0 2]))}
  [offset-representation]
  {:post [;(do (println "make-ranges") (println offset-representation) (println %) true)
          (not-any? #(float<= (reduce - (reverse %)) 0) %)]}  ; No range has 'to' equal to 'from'.
  (let [[from-offsets to-offsets] (partition (/ (count offset-representation) 2) offset-representation)]
    ; (println "make-ranges ")
    ; (println (drop 42 (map vector from-offsets to-offsets)))
    (->> (map vector from-offsets to-offsets)
         (reduce #(conj %1 (make-range (last %1) %2)) [[0 1]])
         (next))))

; (defn fit-ranges
;   "Scales and translates the ranges to fit [from..to]."
;   {:test (examples
;            (fit-ranges [0.0 1.0 0.5 2.0]  0.0 1.0)     ~=> [0.0 0.5 0.25 1.0]
;            (fit-ranges [0.5 1.5 1.0 2.5]  0.0 1.0)     ~=> [0.0 0.5 0.25 1.0]
;            (fit-ranges [0.0 1.0 0.5 2.0] -1.0 1.0)     ~=> [-1.0 0.0 -0.5 1.0]
;            (fit-ranges [-5.0 0.0 -2.0 5.0] -1.0 1.0)   ~=> [-1.0 0.0 -0.399999999 1.0])}
;   [ranges from to]
;   {:post [(not-any? #(float<= (reduce - %) 0) (partition 2 %))]}  ; No range has 'to' equal to or less than 'from'.
;   (let [min-val (reduce min ranges)
;         max-val (reduce max ranges)
;         scale (/ (- to from) (- max-val min-val))]
;     (map #(-> %
;               (- min-val)         ; Translate the range so it starts at 0 to make it possible to scale it.
;               (* scale)           ; Scale the value.
;               (+ from)) ranges))) ; Translate so the range starts at from.

(defn fit-triangle-ranges
  "It scales the ranges for Triangle terms so the peak of the first triangle is at 0 and the peak of the last one at 1."
  {:test (examples
           (fit-triangle-ranges [0.0 2.0 1.0 3.0]) ~=> [-1.0 1.0 0.0 2.0]
           (fit-triangle-ranges [0.0 10.0 5.0 15.0]) ~=> [-1.0 1.0 0.0 2.0])}
  [ranges]
  {:pre [;(do (println "fit-triangle-ranges") (println ranges) true)
         (> (count ranges) 2) ; More than one range. 
         (= (first ranges) (reduce min ranges))
         (= (last ranges) (reduce max ranges))]}  
  (let [ranges-1 (partition 2 ranges)
        first-range (first ranges-1)
        last-range (last ranges-1)
        first-middle (/ (+ (first first-range) (second first-range)) 2)
        last-middle (/ (+ (first last-range) (second last-range)) 2)
        max-to (reduce max ranges)
        scale #(/ (- % first-middle) (- last-middle first-middle))]
    (map (comp float scale) ranges)))



