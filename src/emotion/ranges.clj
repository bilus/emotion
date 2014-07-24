(ns emotion.ranges
  (:use emotion.examples))

(defn- make-range
  [[prev-from prev-to] [width offset]]
  (let [prev-width (- prev-to prev-from)
        from (+ prev-from (* offset prev-width))
        to (+ from width)]
    [from to]))


(defn make-ranges
  "Converts a vector of relative triangular terms widths and offsets (a representation facilitating simpler mutation and cross-over) into pairs of ranges for each term's triangle."
  {:test (examples
          (make-ranges [1.0 0.3 0.7
                       0.0 0.3 0.5]) => (list [0.0 1.0] [0.3 0.6] [0.44999999999999996 1.15])
          (make-ranges [1
                        0]) => (list [0 1]))}
  [offset-representation]
  (let [[widths offsets] (partition (/ (count offset-representation) 2) offset-representation)]
    (->> (map vector widths offsets)
         (reduce #(conj %1 (make-range (last %1) %2)) [[0 1]])
         (next))))

;; TODO: I think it should scale beyond 1 because the peek of the last triangle (high) should be at 1.
(defn scale-ranges
  "Keeps ranges in [0..1] uhm.. range."
  {:test (examples
          (scale-ranges [0 1 0.5 2]) => [0.0 0.5 0.25 1.0]
          (scale-ranges [0.5 1.5 1 2.5] => [0 0.5 0.25 1]))}
  [ranges]
  (let [r (partition 2 ranges)
        min-from (first ranges)
        max-to (reduce #(max %1 (second %2)) 0 r)
        scale (/ 1.0 (- max-to min-from))]
    (map (partial * scale) ranges)))

