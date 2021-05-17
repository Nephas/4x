(ns four-x.state.selectors)

(defn flat-map [f coll]
  (apply concat (map f coll)))

(defn sqr-dist [[x y] [x' y']]
  (let [sqr (fn [x] (* x x))]
    (+ (sqr (- x' x)) (sqr (- y' y)))))

(defn is-neighbor [sector-a sector-b]
  (= 1 (count (clojure.set/intersection (set (:border sector-a)) (set (:border sector-b))))))


; ===== SELECTORS =====

(defn get-closest-star "returns: id of the star closest to pos" [state pos]
  (first (apply min-key (fn [[id {p' :pos}]] (sqr-dist pos p')) (:stars state))))

(defn get-neighborhood "returns: list of sector-ids" [state sector-id]
  (let [sector-a (get-in state [:sectors sector-id])]
    (->> (:sectors state)
         (filter (fn [[id sector-b]] (is-neighbor sector-a sector-b)))
         (map first))))

(defn get-empire-neighborhood "returns: list of sector-ids" [state empire-id]
  (let [empire-sector-ids (set (get-in state [:empires empire-id :sectors]))
        all-neighbor-ids (flat-map #(get-neighborhood state %) empire-sector-ids)
        neighbor-ids (filter #(not (contains? empire-sector-ids %)) all-neighbor-ids)]
    (distinct neighbor-ids)))

(defn get-expandable-neighborhood "returns: list of sector-ids" [state empire-id]
  (let [neighbor-ids (set (get-empire-neighborhood state empire-id))
        occupied-ids (set (flat-map (fn [[id empire]] (:sectors empire)) (:empires state)))]
    (clojure.set/difference neighbor-ids occupied-ids)))