(ns four-x.draw
  (:require [quil.core :as q]))

(defn draw-stars [state]
  (q/stroke-weight 5)
  (q/stroke 255)
  (doseq [[id [x y]] (:stars state)]
    (q/point x y))

  ;LABELS
  (q/fill 255)
  (q/stroke 122)
  (q/stroke-weight 1)
  (doseq [[id [x y]] (:stars state)]
    (q/text id x y)))

(defn draw-geometry [state]
  (q/stroke 0 122 122)
  (doseq [[p1 p2] (:lanes state)]
    (q/line p1 p2))

  (q/stroke 122 122 255)
  (doseq [[p1 p2] (:borders state)]
    (q/line p1 p2)))

(defn draw-highlight [state]
  (q/stroke-weight 3)
  (q/stroke 64 128 255)
  (q/fill 0 0)
  (let [[x y] (get (:stars state) (:selection state))
        cell (four-x.voronoi/voronoi-cell (:sectors state) [x y])]
    (doseq [[p1 p2] cell]
      (q/line p1 p2))

    (q/stroke 0 0)
    (q/fill 64 128 64 128)
    (doseq [[[x1 y1] [x2 y2]] cell]
      (q/triangle x1 y1 x2 y2 x y))))

(defn draw [state]
  (q/background 10)
  (draw-geometry state)
  (draw-highlight state)
  (draw-stars state))
