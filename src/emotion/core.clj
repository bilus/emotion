(ns emotion.core
  (:gen-class)
  (:use emotion.debug)
  (:use emotion.rand)
  (:use emotion.input)
  (:use emotion.fitness)
  (:use emotion.solution)
  (:use emotion.evolution)
  (:require [emotion.templates :as t])
  (:require [clojure.tools.cli :refer [cli]]))

(def images-dir "/Users/martinb/dev/Avatar/CKDB/CK+/cohn-kanade-images")

(def inputs (doall (take 400 (aus-inputs images-dir))))
; (frequencies (map :emotion inputs))

(def input-vars
  (collect-input-vars inputs))

(def output-vars
  (keys (emotions)))

(def input-terms [:low :medium :high])
(def output-terms input-terms)

(def input-templ
  (t/variables-template input-vars input-terms))

(def output-templ
  (t/variables-template output-vars output-terms))

(def rules-templ
  (t/rules-template input-vars output-vars 3))

input-vars
output-vars
input-templ
output-templ
rules-templ

(defn- load-population 
  [file-name]
  (when file-name (read-string (slurp file-name))))

(defn- save-population
  [file-name population]
  (spit file-name (with-out-str (pr population))))

(defn -main [& args]
  (let [default-seed (System/currentTimeMillis) 
        [opts args banner] (cli args
                                ["-h" "--help" "Print this help" 
                                  :default false 
                                  :flag true]
                                 ["-l" "--load" "Load solutions from an .edn file and continue evolution"]
                                 ["-s" "--seed" "Set the random number generator seed" 
                                  :default default-seed
                                  :parse-fn #(Integer. %)]
                                 ["-i" "--iterations" "Number of iterations" 
                                  :default 10
                                  :parse-fn #(Integer. %)])]
        (let [seed (:seed opts)
              num-iterations (:iterations opts)] 
          (println "seed = " seed)
          (with-rand-seed seed
            (let [solution-params (->SolutionParams inputs input-templ output-templ rules-templ input-vars output-vars input-terms output-terms)
                  population (or (load-population (:load opts)) (initial-population solution-params 50))]
                  (doseq [population (take num-iterations (iterate evolve population))]
                    (save-population "intermediate.edn" population)
                    (println [(->> population (map fitness) (reduce min)) 
                              (count population)])))))))      
      
(clojure.test/run-all-tests #"^emotion.*")