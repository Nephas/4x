(ns four-x.state.selectors)

(defn flat-map [f coll]
  (apply concat (map f coll)))

(defn sqr-dist [[x y] [x' y']]
  (let [sqr (fn [x] (* x x))]
    (+ (sqr (- x' x)) (sqr (- y' y)))))

(defn is-neighbor [sector-a sector-b]
  (= 1 (count (clojure.set/intersection (set (:border sector-a)) (set (:border sector-b))))))

; ===== SELECTORS =====

(defn fully-exploited? [state sector-id]
  (let [{exploit :exploit exploit-limit :exploit-limit} (get-in state [:sectors sector-id])]
    (>= exploit exploit-limit)))

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
    (->> (clojure.set/difference neighbor-ids occupied-ids)
         (filter (fn [id] (< 0 (get-in state [:sectors id :exploit-limit])))))))

(defn get-exploitable-sectors "returns: list of sector-ids" [state empire-id]
  (filter (fn [id] (not (fully-exploited? state id)))
          (get-in state [:empires empire-id :sectors])))

(defn get-attackable-sectors "returns: list of sector-ids" [state empire-id]
  (let [neighbor-ids (set (get-empire-neighborhood state empire-id))
        other-empires (dissoc (:empires state) empire-id)
        occupied-ids (set (flat-map (fn [[id empire]] (:sectors empire)) other-empires))]
    (clojure.set/intersection neighbor-ids occupied-ids)))

(defn get-sector-count [state empire-id]
  (count (get-in state [:empires empire-id :sectors])))

(defn get-total-exploit [state empire-id]
  (let [sector-ids (get-in state [:empires empire-id :sectors])
        sectors (vals (select-keys (:sectors state) sector-ids))]
    (apply + (map #(:exploit %) sectors))))

(defn get-action-points [state empire-id]
  (let [sectors (get-sector-count state empire-id)
        exploit (get-total-exploit state empire-id)]
    (max 1 (Math/round (Math/pow (max 1 (- exploit (* 2 sectors))) 0.75)))))

(defn get-owning-empire "returns: empire-id" [state sector-id]
  (first (first (filter (fn [[id empire]] (contains? (set (:sectors empire)) sector-id)) (:empires state)))))

(defn get-sector-value [state sector-id]
  (get-in state [:sectors sector-id :exploit-limit]))

  (defn is-capital? [state empire-id sector-id]
  (= sector-id (get-in state [:empires empire-id :capital])))