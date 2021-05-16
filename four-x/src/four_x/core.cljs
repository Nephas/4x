(ns four-x.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [four-x.state :refer [generate-stars]]
            [four-x.draw :refer [draw]]
            [four-x.voronoi :refer [voronoi-borders]]))

(def SCREENSIZE 1000)
(def TILESIZE (/ SCREENSIZE 8))


(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (q/text-size 16)
  (let [stars (generate-stars SCREENSIZE TILESIZE)
        {borders :borders
         lanes   :edges
         sectors :triangles} (voronoi-borders (vals stars))]
    {:selection :10
     :stars     stars
     :lanes     lanes
     :sectors   sectors
     :borders   borders}))

(defn update-state [state]
  ; Update sketch state by changing circle color and position.
  state)

(defn handle-key [state event]
  (let [key (:key event)
        current (state :selection)
        all-ids (keys (state :stars))
        index (-indexOf all-ids current)
        next-id (nth all-ids (inc index))]
    (do (println key)
        (if (= key :space) (assoc state :selection next-id)
                         state))))

(defn ^:export run-sketch []
  (q/defsketch four-x
               :host "four-x"
               :size [SCREENSIZE SCREENSIZE]
               :setup setup
               :key-pressed handle-key
               :update update-state
               :draw draw
               :middleware [m/fun-mode]))

; uncomment this line to reset the sketch:
; (run-sketch)
