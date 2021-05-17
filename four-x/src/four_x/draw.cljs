(ns four-x.draw
  (:require [quil.core :as q]
            [four-x.state.selectors :refer [get-neighborhood get-empire-neighborhood]]))

(def SCREENSIZE 1000)

(def layers (atom {:geometry nil
                   :empires  nil
                   :stars    nil}))

(defn init-buffers! []
  (let [stars (q/create-graphics SCREENSIZE SCREENSIZE)
        empires (q/create-graphics SCREENSIZE SCREENSIZE)
        geometry (q/create-graphics SCREENSIZE SCREENSIZE)]
    (do (q/with-graphics geometry (q/color-mode :hsb))
        (q/with-graphics empires (q/color-mode :hsb))
        (q/with-graphics stars (q/color-mode :hsb))
        (reset! layers {:geometry geometry
                        :empires  empires
                        :stars    stars}))))

(defn redraw-stars! [state]
  (q/with-graphics (:stars @layers)
                   (q/clear)
                   (q/stroke-weight 10)
                   (doseq [[id {[x y] :pos col :col}] (:stars state)]
                     (apply q/stroke col)
                     (q/point x y))

                   ;LABELS
                   (q/fill 255)
                   (q/stroke 122)
                   (q/stroke-weight 1)
                   (doseq [[id {name :name [x y] :pos}] (:stars state)]
                     (q/text name x y))))

(defn redraw-geometry! [state]
  (q/with-graphics (:geometry @layers)
                   (q/clear)
                   (q/stroke-weight 3)
                   (q/stroke 0 0 32)
                   (doseq [[p1 p2] (:lanes state)]
                     (q/line p1 p2))

                   (q/stroke 0 0 128)
                   (doseq [[p1 p2] (:borders state)]
                     (q/line p1 p2))))

(defn redraw-empires! [state]
  (q/with-graphics (:empires @layers)
                   (q/clear)
                   (q/color-mode :hsb)
                   (q/no-stroke)
                   (doseq [[empire-id empire] (:empires state)]
                     (apply q/fill (conj (:col empire) 128))
                     (doseq [id (:sectors empire)]
                       (let [[x1 y1] (get-in state [:sectors id :pos])]
                         (doseq [[[x2 y2] [x3 y3]] (get-in state [:sectors id :border])]
                           (q/triangle x1 y1 x2 y2 x3 y3)))))))

(defn draw-highlight [state]
  (q/fill 255 255)
  (q/text (str "turn: " (:turn state)) 30 30)
  (q/text (str "acting: " (:acting state)) 30 50)
  (q/text (str "fps: " (int (q/current-frame-rate))) 30 70)

  (q/fill 0 0)
  (q/stroke-weight 3)
  (q/stroke 64 128 255)
  (let [id (:selection state)
        cell (get-in state [:sectors id :border])]
    (doseq [[p1 p2] cell]
      (q/line p1 p2))))

(defn draw [state]
  (q/background 10)
  (q/image (:geometry @layers) 0 0)
  (q/image (:empires @layers) 0 0)
  (q/image (:stars @layers) 0 0)
  (draw-highlight state))
