(ns four-x.ai.deciders
  (:require [four-x.state.selectors :refer [get-expandable-neighborhood get-exploitable-sectors get-attackable-sectors get-sector-value get-owning-empire is-capital?]]
            [quil.core :as q]))

(def cache (atom nil))

(defn get-evaluator [state]
  (if (nil? @cache) (reset! cache (fn [[action empire-id sector-id]]
                                    (let [value ((memoize get-sector-value) state sector-id)
                                          capital? ((memoize is-capital?) state ((memoize get-owning-empire) state sector-id) sector-id)
                                          ]
                                      (+ (cond (= action :exploit) 16
                                               (= action :expand) 8
                                               true 0)
                                         (q/random 0 2)
                                         (if capital? 4 0)
                                         (if (> value 2) value -2)))))
                    @cache))

(defn decide "returns: an action for the empire" [state empire-id]
  (time (let [evaluator (get-evaluator state)
              possible-actions (concat (map #(vector :exploit empire-id %) (get-exploitable-sectors state empire-id))
                                       (map #(vector :expand empire-id %) (get-expandable-neighborhood state empire-id))
                                       (map #(vector :exterminate empire-id %) (get-attackable-sectors state empire-id)))]
          (if (empty? possible-actions) [:pass empire-id nil]
                                        (apply max-key evaluator possible-actions)))))