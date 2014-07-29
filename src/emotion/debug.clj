(ns emotion.debug)

(def debug-log (atom []))

(swap! debug-log #(conj % 1))

(defn dbg-log [expr v]
  (swap! debug-log #(concat % ['-- '| expr '=> v '|])))

(defn dbg-print [expr v]
  (println "dbg:" expr " = " v))

(defn show-log
  []
  @debug-log)

(defn clear-log!
  []
  (reset! debug-log []))

(defmacro dbg
  [x]
  `(let [x# ~x]
     (do
       (dbg-log '~x x#)
       (dbg-print '~x x#)
     x#)))

(defmacro dbg-
  [x]
  x)