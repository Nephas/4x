(ns four-x.voronoi
  (:require [four-x.delauney :refer [triangulate]]
            [four-x.state.records :as r]
            ))

(def sqr-sum #(+ (* %1 %1) (* %2 %2)))

(defn neighbor? [triangle-a triangle-b]
  (->> (map #(contains? (set triangle-a) %) triangle-b)
       (filter true?)
       (count)
       (= 2)))

(defn get-neighbors [triangle coll]
  (filter #(neighbor? triangle %) coll))

(defn circumcenter [[x1 y1] [x2 y2] [x3 y3]]
  (let [denominator (* 2 (+ (* x1 (- y2 y3)) (* x2 (- y3 y1)) (* x3 (- y1 y2))))
        x (+ (* (sqr-sum x1 y1) (- y2 y3)) (* (sqr-sum x2 y2) (- y3 y1)) (* (sqr-sum x3 y3) (- y1 y2)))
        y (+ (* (sqr-sum x1 y1) (- x3 x2)) (* (sqr-sum x2 y2) (- x1 x3)) (* (sqr-sum x3 y3) (- x2 x1)))]
    [(/ x denominator) (/ y denominator)]))

(defn abs-sqr [[x y]] (+ (* x x) (* y y)))

(defn voronoi-borders [triangles]
  (let [graph (for [triangle triangles
                    neighbor (get-neighbors triangle triangles)]
                (vector (apply circumcenter neighbor)
                        (apply circumcenter triangle)))]
    (->> graph
         (map #(sort-by abs-sqr %))
         (distinct))))

(defn voronoi-map [points]
  (let [{:keys [edges triangles]} (triangulate points)]
    {:edges   edges
     :borders (voronoi-borders triangles)}))

(defn voronoi-sector [triangles point]
  (let [sector (filter (fn [t] (contains? (set t) point)) triangles)]
    (r/->Sector point (voronoi-borders sector) 0)))

(defn generate-sectors [points]
  (let [{:keys [points triangles]} (triangulate points)]
    (zipmap (range (count points))
            (map #(voronoi-sector triangles %) points))))
