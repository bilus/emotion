(defproject emotion "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main emotion.core
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.fuzzylite "1.0"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/math.combinatorics "0.0.8"] 
                 [bigml/sampling "2.1.1"]         ; weighted random sampling
                 [org.clojure/tools.cli "0.2.4"]  ; command-line args
;;                  [com.taoensso/timbre "3.2.0"]
                 ]        ;; Profiling.
  :profiles {
             :dev {:dependencies [[midje "1.5.1"]]}   ;; Testing.
             :train {:main emotion.core}
             :capture {:main emotion.capture}}
  
  :aliases {"run-train" ["with-profile" "train" "run"]
            "run-capture" ["with-profile" "capture" "run"]}
  
  :plugins [[lein-localrepo "0.5.3"]
            [quickie "0.2.5"]])
