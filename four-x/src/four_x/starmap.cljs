(ns four-x.starmap
  (:require [quil.core :as q]
            [four-x.state.records :as r]))

(defn bounded [lo val hi]
  (cond (< val lo) lo
        (> val hi) hi
        true val))

(defn bounded-point [[x y] [x-max y-max]]
  [(bounded 0 x x-max)
   (bounded 0 y y-max)])

(defn noisy [n med]
  (+ n (* med (q/random-gaussian))))

(defn star-positions [[map-x map-y] tilesize]
  (for [x (range 0 map-x tilesize)
        y (range 0 map-y tilesize)]
    (bounded-point [(noisy x (* 0.33 tilesize)) (noisy y (* 0.33 tilesize))] [map-x map-y])))

(defn rand-name []
  (clojure.string/join (take (int (q/random 3 6))
                             (repeatedly (fn [] (char (int (q/random 97 120))))))))

(defn rand-color []
  [(int (q/random 0 255)) 64 255])

(defn generate-stars [pos]
  (zipmap (range (count pos)) (map #(r/->Star % (rand-name) (rand-color)) pos)))
