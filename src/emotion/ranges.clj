(ns emotion.ranges
  (:use emotion.examples))

(defn- make-range
  [[prev-from prev-to] [from-offset to-offset]]
  (let [prev-width (- prev-to prev-from)
        from (+ prev-from (* from-offset prev-width))
        to (+ prev-to to-offset)]
    [from to]))


(defn make-ranges
  "Converts a vector of relative triangular terms widths and offsets (a representation facilitating simpler mutation and cross-over) into pairs of ranges for each term's triangle."
  {:test (examples
          (make-ranges [0.0 0.3 0.5
                        1.0 0.3 0.7]) => (list [0.0 2.0] [0.6 2.3] [1.4499999999999997 3.0])
          (make-ranges [0
                        1]) => (list [0 2]))}
  [offset-representation]
  (let [[from-offsets to-offsets] (partition (/ (count offset-representation) 2) offset-representation)]
    (->> (map vector from-offsets to-offsets)
         (reduce #(conj %1 (make-range (last %1) %2)) [[0 1]])
         (next))))

;; TODO: I think it should scale beyond 1 because the peek of the last triangle (high) should be at 1.
(defn scale-ranges
  "Keeps ranges in [0..1] uhm.. range."
  {:test (examples
          (scale-ranges [0 1 0.5 2]) => [0.0 0.5 0.25 1.0]
          (scale-ranges [0.5 1.5 1 2.5]) ~=> [0 0.5 0.25 1])}
  [ranges]
  (let [min-from (reduce min ranges)
        max-to (reduce max ranges)
        scale #(/ (- % min-from) (- max-to min-from))]
    (map (comp float scale) ranges)))

