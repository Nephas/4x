(ns four-x.state.selectors)


(defn sqr-dist [[x y] [x' y']]
  (let [sqr (fn [x] (* x x))]
    (+ (sqr (- x' x)) (sqr (- y' y)))))

(defn get-closest [starmap p]
  (first (apply min-key (fn [[id {p' :pos}]] (sqr-dist p p')) starmap)))

(defn is-neighbor [sector-a sector-b]
  (= 1 (count (clojure.set/intersection (set (:border sector-a)) (set (:border sector-b))))))

(defn get-neighborhood [sectors id]
  (let [sector-a (get sectors id)]
    (->> sectors
         (filter (fn [[id sector-b]] (is-neighbor sector-a sector-b)))
         (map first))))

(defn expandable-sectors [sectors empire]
  (let [neighbor-ids (apply concat (for [id (:sectors empire)] (get-neighborhood sectors id)))]
    ))