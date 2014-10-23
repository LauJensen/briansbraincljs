(ns brianscript.core
  (:use-macros [brianscript.macros :only [forloop]])
  (:require [domina :as domina]))

(enable-console-print!)

(def canvas (domina/by-id "mycanvas"))
(def primaryCtx (.getContext canvas "2d"))
(def dim-board   [ 90   90])
(def dim-screen  [(.-width canvas) (.-height canvas)])
(def dim-scale   (vec (map / dim-screen dim-board)))

(def off   0)
(def dying 1)
(def on    2)

(defn fmap [f coll] (doall (map f coll)))

(defn render-cell [ctx x y col]
  (let [sx  (inc (* x (dim-scale 0)))
        sy  (inc (* y (dim-scale 1)))]
    (set! (.-fillStyle ctx) col)
    (.fillRect ctx sx sy (dec (dim-scale 0)) (dec (dim-scale 1)))))

(defn render [ctx stage]
  (set! (.-fillStyle ctx) "#000")
  (.fillRect ctx 0 0 (dim-screen 0) (dim-screen 1))
  (forloop [(y 0) (< y (dim-board 1)) (inc y)]
     (forloop [(x 0) (< x (dim-board 0)) (inc x)]
         (condp = (get-state x y stage)
           on      (render-cell ctx x y "#fff")
           dying   (render-cell ctx x y "#bbb")
           nil))))

(defn make-board
  [w h]
  (let [board (int-array h)]
    (forloop [(y 0) (< y h) (inc y)]
       (let [col   (int-array w)]
         (dotimes [x w]
           (aset col x
                 (if (< 50 (rand-int 100))
                   on
                   off)))
         (aset board y col)))
    board))

(defn get-state
  [x y board]
  (let [dimy (count board)
        dimx (count (aget board 0))]
    (aget board (mod y dimy)
                (mod x dimx))))

(defn active-neighbors
  [x y blocal]
  (let [coords [[-1 -1] [0 -1] [1 -1]
                [-1 0]         [1  0]
                [-1 1]  [0 1]  [1  1]]]
    (->> (map (fn [[xd yd]] ;delta x, deltay
                (let [nx (+ x xd)  ;new x
                      ny (+ y yd)] ; new y
                  (get-state nx ny blocal)))
              coords)
         (filter #{on})
         count)))

(defn stepfn
  [b]
  (let [board @b
        dimy (count board)
        dimx (count (aget board 0))
        newb (make-board dimx dimy)]
    (forloop [(y 0) (< y dimy) (inc y)]
       (forloop [(x 0) (< x dimx) (inc x)]
          (let [current-state (get-state x y board)]
            (aset newb y x
                  (cond
                   (= on current-state)               dying
                   (= dying current-state)            off
                   (= 2 (active-neighbors x y board)) on
                   :else off)))))
    (reset! b newb)))
; does not set b

(def board (atom (make-board 90 90)))

(defn main-loop
  []
  (render primaryCtx (stepfn board)))

;(def thread (js/setInterval main-loop 150))
;(js/clearInterval thread)

(time (render primaryCtx (stepfn board)))

;rules not working
