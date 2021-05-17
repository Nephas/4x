(ns four-x.draw
  (:require [quil.core :as q]
            [four-x.state.selectors :refer [get-neighborhood get-empire-neighborhood get-total-exploit]]))

(def layers (atom {:geometry nil
                   :empires  nil
                   :stars    nil}))

(defn redraw-stars! [state]
  (q/with-graphics (:stars @layers)
                   (q/clear)
                   (q/stroke-weight 10)
                   (doseq [[id {[x y] :pos col :col}] (:stars state)]
                     (apply q/stroke col)
                     (q/point x y))))

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
                     (doseq [id (:sectors empire)]
                       (let [{[x1 y1] :pos x :exploit} (get-in state [:sectors id])]
                         (apply q/fill (conj (:col empire) (* 64 (inc x))))
                         (doseq [[[x2 y2] [x3 y3]] (get-in state [:sectors id :border])]
                           (q/triangle x1 y1 x2 y2 x3 y3)))))))

(defn init-buffers! [state [x y]]
  (let [stars (q/create-graphics x y)
        empires (q/create-graphics x y)
        geometry (q/create-graphics x y)]
    (do (q/with-graphics geometry (q/color-mode :hsb))
        (q/with-graphics empires (q/color-mode :hsb))
        (q/with-graphics stars (q/color-mode :hsb))
        (reset! layers {:geometry geometry
                        :empires  empires
                        :stars    stars})
        (redraw-empires! state)
        (redraw-geometry! state)
        (redraw-stars! state))))

(defn draw-highlight [state]
  (q/fill 0 0)
  (q/stroke-weight 3)
  (q/stroke 64 128 255)
  (let [id (:selection state)
        cell (get-in state [:sectors id :border])
        {name :name [x y] :pos} (get-in state [:stars id])]
    (doseq [[p1 p2] cell]
      (q/line p1 p2))

    (q/no-stroke)
    (q/fill 255)
    (q/text name (+ 10 x) y)))


(defn draw-ranking-bar [[x y] ranking]
  (let [total (apply + (map :points ranking))
        norm (/ 150 total)]
    (loop [[current & remaining] (sort-by :points ranking)
           acc 0]
      (when (some? current) (let [points (:points current)]
                              (apply q/fill (:col current))
                              (q/rect (+ x (* norm acc)) y (* norm points) 20)
                              (recur remaining (+ acc points)))))))

(defn draw-ui [state]
  (q/with-translation [1000 0]
                      (q/fill 255 255)
                      (q/stroke-weight 0)
                      (q/text (str "turn: " (:turn state)) 30 30)
                      (q/text (str "acting: " (:acting state)) 30 50)
                      (q/text (str "fps: " (int (q/current-frame-rate))) 30 900)

                      (q/text (str "exploit: " (get-total-exploit state 0)) 30 90)
                      (q/text (str "size: " (count (get-in state [:empires 0 :sectors]))) 30 110)

                      (q/no-stroke)
                      (q/fill 255 255)
                      (q/text (str "empire size:") 30 130)
                      (->> (vals (:empires state))
                           (map (fn [empire] (hash-map :col (:col empire) :points (count (:sectors empire)))))
                           (draw-ranking-bar [30 140]))

                      (q/fill 255 255)
                      (q/text (str "empire exploit:") 30 180)
                      (->> (:empires state)
                           (map (fn [[id empire]] (hash-map :col (:col empire) :points (get-total-exploit state id))))
                           (draw-ranking-bar [30 190]))))

(defn draw [state]
  (try
    (do (q/background 10)
        (q/image (:geometry @layers) 0 0)
        (q/image (:empires @layers) 0 0)
        (q/image (:stars @layers) 0 0)
        (draw-highlight state)
        (draw-ui state))
    (catch js/Error e
      (init-buffers! state [1000 1000]))))