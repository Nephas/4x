(ns four-x.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [four-x.starmap :refer [star-positions generate-stars]]
            [four-x.draw :refer [draw redraw-geometry! redraw-stars! redraw-empires! init-buffers!]]
            [four-x.state.records :as r]
            [four-x.state.init :refer [init-state]]
            [four-x.ai.deciders :refer [decide]]
            [four-x.state.actions :refer [queue-action! has-actions? next-action!]]
            [four-x.state.reducers :refer [reduce-action]]
            [four-x.state.selectors :refer [get-closest-star get-expandable-neighborhood get-attackable-sectors get-exploitable-sectors]]
            [four-x.voronoi :refer [voronoi-map generate-sectors]]))

(def SCREENSIZE [1200 1000])
(def MAPSIZE [1000 1000])
(def TILESIZE (/ (first MAPSIZE) 10))

(def debug (atom nil))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (q/text-size 16)
  (let [state (init-state MAPSIZE TILESIZE)]
    (init-buffers! state MAPSIZE)
    state))

(defn update-state [state]
  (reset! debug state)
  (do (when (and (not= 0 (get-in state [:active :empire])) (not (has-actions?)))
        (queue-action! (decide state (get-in state [:active :empire]))))
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
        exploitable-sector-ids (set (get-exploitable-sectors state 0))
        attackable-sector-ids (set (get-attackable-sectors state 0))
        ]
    (do (when (contains? expandable-sector-ids id) (queue-action! [:expand 0 id]))
        (when (contains? exploitable-sector-ids id) (queue-action! [:exploit 0 id]))
        (when (contains? attackable-sector-ids id) (queue-action! [:exterminate 0 id]))
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
