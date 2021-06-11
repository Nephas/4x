(ns four-x.state.init
  (:require [quil.core :as q]
            [four-x.starmap :refer [star-positions generate-stars]]
            [four-x.voronoi :refer [voronoi-map voronoi-sectors]]
            [four-x.generator.names :refer [generate-empire-name]]
            [four-x.state.selectors :refer [get-closest-star get-sector-value]]
            [four-x.state.records :as r]))

(defn get-start-sectors [stars mapsize n]
  (->> (range 0 q/TWO-PI (/ q/TWO-PI n))
       (map (fn [angle] (vector (+ 500 (* 330 (q/cos angle))) (+ 500 (* 330 (q/sin angle))))))
       (map (fn [pos] (get-closest-star {:stars stars} pos)))))

(defn boost-sectors [ids sectors]
  (loop [current-sectors sectors
         [id & remaining] ids]
    (let [next-state (if (contains? (set ids) id)
                       (-> current-sectors
                           (assoc-in [id :exploit-limit] 4)
                           (assoc-in [id :exploit] 2))
                       current-sectors)]
      (if (empty? remaining) next-state (recur next-state remaining)))))

(defn generate-empires [start-sectors n]
  (let [ids (range n)
        empires (map (fn [id] (let [start-sector (nth start-sectors id)]
                                (r/->Empire 1 (generate-empire-name) [(* 32 id) 128 255] [start-sector] start-sector 1))) ids)]
    (zipmap ids empires)))

(defn generate-sectors [positions start-sectors]
  (->> positions
       (voronoi-sectors)
       (boost-sectors start-sectors)))

(defn init-state [mapsize tilesize n]
  (let [positions (star-positions mapsize tilesize)
        stars (generate-stars positions)
        start-sectors (get-start-sectors stars mapsize n)
        sectors (generate-sectors positions start-sectors)
        empires (generate-empires start-sectors n)
        {borders :borders
         lanes   :edges} (voronoi-map positions)
        state {:selection 10
               :turn      0
               :active    {:empire 0}
               :empires   empires
               :stars     stars
               :sectors   sectors
               :lanes     lanes
               :borders   borders}]
    state))