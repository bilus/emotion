(defproject emotion "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main emotion.core
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.fuzzylite "1.0"]
                 [org.clojure/data.json "0.2.5"]
;;                  [com.taoensso/timbre "3.2.0"]
                 ]        ;; Profiling.
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}}   ;; Testing.
  :plugins [[lein-localrepo "0.5.3"]
            [quickie "0.2.5"]]


;;   :dev-dependencies [[com.stuartsierra/lazytest "1.1.2"]
;;                    [lein-autotest "1.1.0"]]
;; :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"}
  )
