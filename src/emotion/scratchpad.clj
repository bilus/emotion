(ns emotion.scratchpad)

;(let [~@(flatten (map #(vector (first %) (+ foo (second %))) (partition 2 bindings)))]

;; (flatten (map #(vector (first %) (second %)) (partition 2 ['x 1 'y 2])))

;; (defn resolve-input-var [foo v]
;;   (second v))

;; (defmacro with-foo [foo bindings & body]
;;   `(let [~foo 5]
;;      (let [~@(flatten (map #(vector (first %) '('resolve-input-var %)) (partition 2 bindings)))]
;;        ~@body)))


;; (with-foo z [x 1 y 2]
;;   z)

;; (macroexpand-1 '(with-foo z [x 1 y 3]
;;   z))


;; (defmacro dbg-2 [& s]
;;   (list 'let ['a s] (list 'println (list 'quote s) "=" 'a) 'a))

;; (dbg-2 + 3 4)
(defmacro forloop [[v f t] & body]
  (let [to (gensym)]
    (list 'let [to t]
      (list 'loop [v f]
            (concat (list* 'when (list '<= v to)
                   body)
              (list (list 'recur (list 'inc v))))))))

(macroexpand-1 '(forloop [i 1 (rand 10)]
  (print i)
  (print (* i i))))


(forloop [i 1 10]
  (print i)
  (print (* i i)))

(defmacro forloop2 [[v f t] & body]
  `(let [to# ~t]
     (loop [~v ~f]
       (when (<= ~v to#)
         ~@body
         (recur (inc ~v))))))

(clojure.pprint/pprint (macroexpand-1 '(forloop2 [i 1 (rand 10)]
  (print i)
  (print (* i i)))))


(forloop2 [i 1 10]
  (print i)
  (print (* i i)))

(defn forloop-fn2 [] `(let [finish# end]
     (loop [i 0]
       (when (< i finish#)
         (print i)
         (recur (inc i))))))

(forloop-fn2)



;; (defmacro with-foo [foo bindings & body]
;;   `(let ~(into [] (concat [foo 5] (mapcat #(list (first %) (list 'str foo (second %))) (partition 2 bindings))))
;;     ~@body))

(defmacro with-foo [foo bindings & body]
  `(let ~(into [] (concat [foo 5] (mapcat #(list (first %) `(str ~foo ~(second %))) (partition 2 bindings))))
    ~@body))


(macroexpand-1 '(with-foo z [x 1 y 1] x))

(with-foo z [x 1 y 1] x)


(clojure.pprint/pprint (list* 'do (map #(list 'def %) ['i 'j])))
(clojure.pprint/pprint `(do ~@(map #(list `def %) [`i `j])))
