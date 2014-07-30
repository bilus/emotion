(ns emotion.mama-demo
    (:use emotion.solution)
    (:use emotion.evolution))

(defn- levenshtein [str1 str2]
  (let [len1 (count str1)
        len2 (count str2)]
    (cond (zero? len1) len2
          (zero? len2) len1
          :else
          (let [cost (if (= (first str1) (first str2)) 0 1)]
            (min (inc (levenshtein (rest str1) str2))
                 (inc (levenshtein str1 (rest str2)))
                 (+ cost
                    (levenshtein (rest str1) (rest str2)))))))) 

(defrecord MamaSolution [ss]
  emotion.solution/Evolvable
  (mutate [solution]
	(let [letters ["A" "B" "C" "D" "E" "F" "G" "H" "I" 
				   "J" "K" "L" "M" "N" "O"]]
   		(letfn [(maybe-mutate-letter [l]
                              (if (< (rand) 0.1) 
                                (first (rand-nth letters)) 
                                l))]
        (update-in solution [:ss]
            (partial map maybe-mutate-letter)))))
  (fitness [solution]
  	(levenshtein (:ss solution) "MAMA"))
  (crossover [s1 s2]
	(assoc-in s1 [:ss] (first (swap-halves (:ss s1) (:ss s2))))))

(defrecord MamaParams []
  SolutionFactory
  (generate-solution [solution-params]
    (->MamaSolution 
      (rand-nth ["ABCD" "MBCD" "LICA" "DADA" "FIFI" 
             "HEDC" "NMLK" "DIAB" "MONA" "FEHJ" 
             "CINO"])))) 

; (def p (initial-population (MamaParams.) 10))
; ; p
; (def iterations (iterate evolve p))
; (doseq [population (take 30 iterations)]
;   (println [(->> population (map fitness) (reduce +)) 
;             (count population)]))
; (def result (last (take 30 iterations)))
; (println result)
; ; (map #(reduce + (map fitness %)) (take 500 iterations))
; ; (map #(vector % (fitness %)) result)