(ns brianscript.macros)

(defmacro forloop
  [[init pred step] & body]
  `(loop [~@init]
     (when ~pred
       ~@body
       (recur ~step))))
