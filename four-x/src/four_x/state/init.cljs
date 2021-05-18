(ns four-x.state.init
  (:require [quil.core :as q]
            [four-x.starmap :refer [star-positions generate-stars]]
            [four-x.voronoi :refer [voronoi-map generate-sectors]]
            [four-x.state.records :as r]))

(defn generate-empires [sectors n]
  (let [ids (range n)
        empires (map (fn [id] (r/->Empire (str "empire " id) [(* 32 id) 128 255] [(int (q/random (count sectors)))])) ids)]
    (zipmap ids empires)))

(defn init-state [mapsize tilesize]
  (let [positions (star-positions mapsize tilesize)
        stars (generate-stars positions)
        sectors (generate-sectors positions)
        empires (generate-empires sectors 8)
        {borders :borders
         lanes   :edges} (voronoi-map positions)
        state {:selection 10
               :turn      0
               :active    {:empire  0
                           :actions 1}
               :empires   empires
               :stars     stars
               :sectors   sectors
               :lanes     lanes
               :borders   borders}]
    state))