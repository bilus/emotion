(ns emotion.capture
  (:use emotion.input)
  (:use emotion.solution)
  (:require [clojure.tools.cli :refer [cli]]))

(defn format-emotion
  [[k v]]
  [k (format "%.2f" v)])

(defn format-emotions 
  [emotions]
  (into {} (map format-emotion emotions)))

(defn -main [& args]
  (let [[opts args banner] (cli args
                                ["-h" "--help" "Print this help" 
                                  :default false 
                                  :flag true] ; TODO: Show help.
                                 ["-a" "--aus" "Path to an aus file"]
                                 ["-i" "--input" "Path to an .edn file with a vector containing one or more solutions"]
                                 ["-o" "--output" "Optional path to save the best performing solution"])]
    (let [aus (load-aus-input (:aus opts))
          input-path (:input opts)
          output-path (:output opts)
          population (load-population input-path)]
      (let [solution (first population)]
        ; (println (:input-vars (:solution-params solution)))
        ; (println aus)
        ; (println (aus-input->input-params (:input-vars (:solution-params solution)) aus))
        (println (format-emotions (capture-emotions solution aus)))))))

(-main "-a" "/Users/martinb/dev/Avatar/CKDB/CK+/cohn-kanade-images/S113/008/aus.txt"
       ; "-i" "tournament-intermediate-r3-0.0597.edn"
       "-i" "intermediate.edn"
       ; "-i" "tournament-intermediate-r3.edn"
       "-o" "winner.edn")