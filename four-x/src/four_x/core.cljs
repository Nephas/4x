(ns four-x.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [four-x.starmap :refer [star-positions generate-stars get-closest]]
            [four-x.draw :refer [draw]]
            [four-x.state.records :as r]
            [four-x.state.selectors :refer [get-closest]]
            [four-x.voronoi :refer [voronoi-map generate-sectors]]))

(def SCREENSIZE 1000)
(def TILESIZE (/ SCREENSIZE 10))


(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (q/text-size 16)
  (let [positions (star-positions SCREENSIZE TILESIZE)
        stars (generate-stars positions)
        sectors (generate-sectors positions)
        {borders :borders
         lanes   :edges} (voronoi-map positions)]
    {:selection 10
     :turn      0
     :empires   {0 (r/->Empire "the good"
                               [0 128 255]
                               (range 5 8))
                 1 (r/->Empire "the bad"
                               [128 128 255]
                               (range 30 33))}
     :stars     stars
     :sectors   sectors
     :lanes     lanes
     :borders   borders}))

(defn update-state [state]
  (if (zero? (mod (q/frame-count) 30)) (update state :turn inc)
                                       state))

(defn handle-key [state event]
  (let [key (:key event)]
    (do (println key)
        state)))

(defn handle-move [state event]
  (let [{x :x y :y} event
        id (get-closest (:stars state) [x y])]
    (assoc state :selection id)))

(defn handle-click [state event]
  (let [{x :x y :y} event
        id (get-closest (:stars state) [x y])]
    (println event)
    (update-in state [:empires 0 :sectors] conj id)))

(defn ^:export run-sketch []
  (q/defsketch four-x
               :host "four-x"
               :size [SCREENSIZE SCREENSIZE]
               :setup setup
               :key-pressed handle-key
               :mouse-moved handle-move
               :mouse-clicked handle-click
               :update update-state
               :draw draw
               :middleware [m/fun-mode]))

; uncomment this line to reset the sketch:
; (run-sketch)
