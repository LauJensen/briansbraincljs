(ns brianscript.core
  (:use-macros [brianscript.macros :only [forloop]])
  (:require [domina :as domina]))

(enable-console-print!)

(def canvas (domina/by-id "mycanvas"))
(def primaryCtx (.getContext canvas "2d"))
(def dim-board   [90 90])
(def dim-screen  [(.-width canvas) (.-height canvas)])
(def dim-scale   (vec (map / dim-screen dim-board)))

(def off   0)
(def dying 1)
(def on    2)

(defn render-cell [ctx x y col]
  (let [sx  (inc (* x (dim-scale 0)))
        sy  (inc (* y (dim-scale 1)))]
    (set! (.-fillStyle ctx) col)
    (.fillRect ctx sx sy (dec (dim-scale 0)) (dec (dim-scale 1)))))

; 8ms
(defn render [ctx [stage _]]
  (set! (.-fillStyle ctx) "#000")
  (.fillRect ctx 0 0 (dim-screen 0) (dim-screen 1))
  (forloop [(y 0) (< y (dim-board 1)) (inc y)]
     (forloop [(x 0) (< x (dim-board 0)) (inc x)]
         (condp = (aget stage y x)
           on      (render-cell ctx x y "#fff")
           dying   (render-cell ctx x y "#bbb")
           nil))))

(defn mod [x m]
  (js* "((x%m)+m)%m"))
; 40 ms per 1000000 runs
(defn get-cell
  [b x y]
  (js* "b[brianscript.core.mod(y,90)][brianscript.core.mod(x,90)]"))

; 55ms per 1.000.000 runs
;(defn get-cell
;  [b x y]
;  (aget b (mod y 120) (mod x 120)))

(defn make-board
  [w h]
  (let [board  (make-array h)
        buffer (make-array h)]
    (forloop [(y 0) (< y h) (inc y)]
       (let [row  (int-array w)]
         (forloop [(x 0) (< x w) (inc x)]
             (aset row x (rand-nth [on off on])))
         (aset board y (int-array row))
         (aset buffer y (int-array row))))
    (atom [board buffer])))

(defn active-neighbors
  [blocal x y]
  (let [[height width] dim-board
        coords   [[(dec x) (dec y)] [x (dec y)] [(inc x) (dec y)]
                  [(dec x) y]                   [(inc x) y]
                  [(dec x) (inc y)] [x (inc y)] [(inc x) (inc y)]]]
    (loop [[c & cs] coords cnt 0]
      (if (and c (< cnt 3))
        (let [state   (get-cell blocal (c 0) (c 1))]
          (cond
           (and (= state on) (= cnt 2))
           3

           (= state on)
           (recur cs (inc cnt))

           :else (recur cs cnt)))
        cnt))))

(defn stepfn
  [stage]
  (let [[dimx dimy]       dim-board
        [board buffer]    @stage]
    (forloop [(y 0) (< y dimy) (inc y)]
       (forloop [(x 0) (< x dimx) (inc x)]
                (let [current-state (get-cell board x y)]
                  (aset buffer y x
                        (cond
                         (= on current-state)                dying
                         (= dying current-state)             off
                         (= 2 (active-neighbors board x y))  on
                         :else off)))))
    (reset! stage [buffer board])))

(def board (make-board (dim-board 0) (dim-board 1)))

(println "Timing 3 runs of 10 iterations:")
(dotimes [i 3] (time (dotimes [i 10] (stepfn board))))
(println "Timing 10 runs of the renderer:")
(let [b @board] (time (dotimes [i 10] (render primaryCtx b))))

;(def thread (js/setInterval (fn [] (render primaryCtx (stepfn board))) 60))

;(time (dotimes [i 1] (stepfn board)))
;(time (dotimes [i 10] (render primaryCtx (stepfn board))))
;(time (render primaryCtx @board))

;(def thread (js/setInterval
 ;            (fn [] (render primaryCtx (stepfn board))) 80))
;(js/clearInterval thread)
