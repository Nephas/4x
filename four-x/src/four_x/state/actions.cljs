(ns four-x.state.actions)

;; empire X expand into sector Y
;(defn expand #(vector :expand %1 %2))
;
;; empire X explore sector Y
;(defn explore #(vector :explore %1 %2))
;
;; empire X exploit sector Y
;(defn exploit #(vector :exploit %1 %2))
;
;; empire X exterminate sector Y
;(defn exterminate #(vector :exterminate %1 %2))

(def actions (atom []))

(defn queue-action! "should be [:type empire-id sector-id] where type can be one of #{:expand :explore :exploit :exterminate}" [action]
  (swap! actions conj action))

(defn has-actions? []
  (not (empty? @actions)))

(defn next-action! []
  (let [next (first @actions)]
    (swap! actions rest)
    next))