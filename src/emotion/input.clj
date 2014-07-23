(ns emotion.input
  (:require [clojure.java.io :as io])
  (:require [clojure.data.json :as json])
  (:require [clojure.walk :only [keywordize-keys] :as walk])
  (:import  [java.io FileReader]))

(defn- read-json
  "Reads json from a reader into a map. It's there to facilitate profiling using defnp."
  [in]
  (json/read in))

(defn aus-inputs [images-dir]
  "Creates a lazy stream of maps created from aus.txt files read recursively from images-dir. Files are created using BatchTracker/main_seq.rb."
  (->> images-dir
       (io/file)
       (file-seq)
       (filter #(= (.getName %) "aus.txt"))
       (map #(FileReader. %))
       (map (comp walk/keywordize-keys read-json))
       (filter #(= (:status %) "ok"))
))

(defn collect-input-vars [inputs]
  (->> (first inputs)
       (keys)
       (filter #(re-matches #"^au_.*" (name %)))
       (into [])))

