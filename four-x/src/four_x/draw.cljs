(ns four-x.draw
  (:require [quil.core :as q]
            [four-x.state.selectors :refer [get-neighborhood]]))

(defn draw-stars [state]
  (q/stroke-weight 10)
  (doseq [[id {[x y] :pos col :col}] (:stars state)]
    (apply q/stroke col)
    (q/point x y))

  ;LABELS
  (q/fill 255)
  (q/stroke 122)
  (q/stroke-weight 1)
  (doseq [[id {name :name [x y] :pos}] (:stars state)]
    (q/text name x y)))

(defn draw-geometry [state]
  (q/stroke-weight 3)
  (q/stroke 0 0 32)
  (doseq [[p1 p2] (:lanes state)]
    (q/line p1 p2))

  (q/stroke 0 0 128)
  (doseq [[p1 p2] (:borders state)]
    (q/line p1 p2)))

(defn draw-empires [state]
  (q/no-stroke)
  (doseq [empire (vals (:empires state))]
    (apply q/fill (conj (:col empire) 64))
    (doseq [id (:sectors empire)]
      (let [[x1 y1] (get-in state [:sectors id :pos])]
        (doseq [[[x2 y2] [x3 y3]] (get-in state [:sectors id :border])]
          (q/triangle x1 y1 x2 y2 x3 y3))))))

(defn draw-highlight [state]
  (q/fill 255 255)
  (q/text (str "turn: " (:turn state)) 30 30)

  (q/fill 0 0)
  (q/stroke-weight 3)

  (q/stroke 64 64 128)
  (let [neighbor-ids (get-neighborhood (:sectors state) (:selection state))
        neighbor-map (select-keys (:sectors state) neighbor-ids)]
    (doseq [sector (vals neighbor-map)]
      (doseq [[p1 p2] (:border sector)]
        (q/line p1 p2))))

  (q/stroke 64 128 255)
  (let [id (:selection state)
        cell (get-in state [:sectors id :border])]
    (doseq [[p1 p2] cell]
      (q/line p1 p2))))

(defn draw [state]
  (q/background 10)
  (draw-geometry state)
  (draw-empires state)
  (draw-highlight state)
  (draw-stars state))
