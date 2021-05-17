(ns four-x.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [four-x.starmap :refer [star-positions generate-stars]]
            [four-x.draw :refer [draw redraw-geometry! redraw-stars! redraw-empires! init-buffers!]]
            [four-x.state.records :as r]
            [four-x.ai.deciders :refer [decide]]
            [four-x.state.actions :refer [queue-action! has-actions? next-action!]]
            [four-x.state.reducers :refer [reduce-action]]
            [four-x.state.selectors :refer [get-closest-star get-expandable-neighborhood get-exploitable-sectors]]
            [four-x.voronoi :refer [voronoi-map generate-sectors]]))

(def SCREENSIZE [1200 1000])
(def MAPSIZE [1000 1000])
(def TILESIZE (/ (first MAPSIZE) 10))

(def debug (atom nil))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (q/text-size 16)
  (let [positions (star-positions MAPSIZE TILESIZE)
        stars (generate-stars positions)
        sectors (generate-sectors positions)
        {borders :borders
         lanes   :edges} (voronoi-map positions)
        state {:selection 10
               :turn      0
               :acting    0
               :actions   2
               :empires   {0 (r/->Empire "the good"
                                         [0 128 255]
                                         [(int (q/random (count sectors)))])
                           1 (r/->Empire "the bad"
                                         [64 128 255]
                                         [(int (q/random (count sectors)))])
                           2 (r/->Empire "the neutral"
                                         [128 128 255]
                                         [(int (q/random (count sectors)))])
                           3 (r/->Empire "the ugly"
                                         [192 128 255]
                                         [(int (q/random (count sectors)))])}
               :stars     stars
               :sectors   sectors
               :lanes     lanes
               :borders   borders}]
    (init-buffers! state MAPSIZE)
    state))

(defn update-state [state]
  (reset! debug state)
  (do (when (and (not= 0 (:acting state)) (not (has-actions?)))
        (queue-action! (decide state (:acting state))))
      (if (has-actions?) (reduce-action state (next-action!))
                         state)))

(defn handle-key [state event]
  (let [key (:key event)]
    (do (when (= :b key) (init-buffers! state MAPSIZE))
        (println key)
        state)))

(defn handle-move [state event]
  (let [{x :x y :y} event
        id (get-closest-star state [x y])]
    (assoc state :selection id)))

(defn handle-click [state event]
  (let [{x :x y :y} event
        id (get-closest-star state [x y])
        expandable-sector-ids (set (get-expandable-neighborhood state 0))
        exploitable-sector-ids (set (get-exploitable-sectors state 0))]
    (do (when (contains? expandable-sector-ids id) (queue-action! [:expand 0 id]))
        (when (contains? exploitable-sector-ids id) (queue-action! [:exploit 0 id]))
        state)))

(defn ^:export run-sketch []
  (q/defsketch four-x
               :host "four-x"
               :size SCREENSIZE
               :setup setup
               :key-pressed handle-key
               :mouse-moved handle-move
               :mouse-clicked handle-click
               :update update-state
               :draw draw
               :middleware [m/fun-mode]))

; uncomment this line to reset the sketch:
; (run-sketch)
