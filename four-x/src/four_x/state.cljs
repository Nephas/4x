(ns four-x.state
  (:require [quil.core :as q]))

(defn bounded [lo val hi]
  (cond (< val lo) lo
        (> val hi) hi
        true val))

(defn bounded-point [[x y] mapsize]
  [(bounded 0 x mapsize)
   (bounded 0 y mapsize)])

(defn noisy [n med]
  (+ n (* med (q/random-gaussian))))

(defn star-positions [mapsize tilesize]
  (for [x (range 0 mapsize tilesize)
        y (range 0 mapsize tilesize)]
    (bounded-point [(noisy x (* 0.33 tilesize)) (noisy y (* 0.33 tilesize))] mapsize)))

(defn generate-stars [mapsize tilesize]
  (let [pos (star-positions mapsize tilesize)]
    (zipmap (range (count pos)) pos)))