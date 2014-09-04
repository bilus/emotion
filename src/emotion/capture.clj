(ns emotion.capture
  (:use emotion.input)
  (:use emotion.solution)
  (:require [clojure.tools.cli :refer [cli]]))

(defn- format-emotion
  [[k v]]
  [k (format "%.2f" v)])

(defn format-emotions 
  [emotions]
  (into {} (map format-emotion emotions)))

(defn- capture 
  [aus-path solution]
  (let [aus (load-aus-input aus-path)]
    (format-emotions (capture-emotions solution aus))))

(defn -main [& args]
  (let [[opts args banner] (cli args
                                ["-h" "--help" "Print this help" 
                                  :default false 
                                  :flag true] ; TODO: Show help.
                                 ["-a" "--aus" "Path to an aus file"]
                                 ["-i" "--input" "Path to an .edn file with a vector containing one or more solutions"]
                                 ["-o" "--output" "Optional path to save the best performing solution"])]
    (let [aus-path (:aus opts)
          input-path (:input opts)
          population (load-population input-path)
          solution (first population)
          output-path (:output opts)]
        (println (capture aus-path input-path)))))

(let [population (load-population "intermediate.edn")
          solution (first population)]
  
  (println "anger")
  (println (capture "/Users/martinb/dev/Avatar/CKDB/CK+/cohn-kanade-images/S113/008/aus.txt" solution))

  (println "happiness")
  (println (capture "/Users/martinb/dev/Avatar/CKDB/CK+/cohn-kanade-images/S138/006/aus.txt" solution))

  (println "fear")
  (println (capture "/Users/martinb/dev/Avatar/CKDB/CK+/cohn-kanade-images/S501/004/aus.txt" solution))

  (println "surprise")
  (println (capture "/Users/martinb/dev/Avatar/CKDB/CK+/cohn-kanade-images/S132/007/aus.txt" solution)))