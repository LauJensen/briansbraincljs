(ns brianscript.core
  (:require [domina :as domina]))

(enable-console-print!)

(def canvas (domina/by-id "mycanvas"))
(def primaryCtx (.getContext canvas "2d"))
(def dim-board   [ 90   90])
(def dim-screen  [(.-width canvas) (.-height canvas)])
(def dim-scale   (vec (map / dim-screen dim-board)))

(defn fmap [f coll] (doall (map f coll)))

(defn render-cell [ctx cell]
  (let [[state x y] cell
        x  (inc (* x (dim-scale 0)))
        y  (inc (* y (dim-scale 1)))]
    (set! (.-fillStyle ctx) (if (= state :dying) "#bbb" "#fff"))
    (.fillRect ctx x y (dec (dim-scale 0)) (dec (dim-scale 1)))))

(defn render [ctx stage]
  (set! (.-fillStyle ctx) "#000")
  (.fillRect ctx 0 0 (dim-screen 0) (dim-screen 1))
  (fmap (fn [col]
          (fmap #(when (not= :off (% 0))
                   (render-cell ctx %)) col)) stage))

(def board
     (for [x (range (dim-board 0))]
       (for [y (range (dim-board 1))]
         [(if (< 50 (rand-int 100)) :on :off) x y])))

(defn active-neighbors [above [left _ right] below]
  (count
   (filter #(= :on (% 0))
           (concat above [left right] below))))

(defn torus-window [coll]
  (partition 3 1 (concat [(last coll)] coll [(first coll)])))

(defn rules [above current below]
  (let [[self x y]  (second current)]
    (cond
      (= :on    self)                              [:dying x y]
      (= :dying self)                              [:off   x y]
      (= 2 (active-neighbors above current below)) [:on    x y]
      :else                                        [:off   x y])))

(defn step [board]
  (doall
   (map (fn [window]
          (apply #(doall (apply map rules %&))
                 (doall (map torus-window window))))
        (torus-window board))))

(def stage (atom board))

(defn main-loop
  []
  (render primaryCtx (swap! stage step)))

(def thread (js/setInterval main-loop 500))

;(js/clearInterval thread)
