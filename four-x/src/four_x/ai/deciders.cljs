(ns four-x.ai.deciders
  (:require [four-x.state.selectors :refer [get-expandable-neighborhood get-exploitable-sectors get-attackable-sectors]]
            [quil.core :as q]))

(defn decide "returns: an action for the empire" [state empire-id]
  (let [possible-actions (concat (map #(vector :expand empire-id %) (get-expandable-neighborhood state empire-id))
                                 (map #(vector :exploit empire-id %) (get-exploitable-sectors state empire-id))
                                 (map #(vector :exterminate empire-id %) (get-attackable-sectors state empire-id)))]
    (if (empty? possible-actions) [:pass empire-id nil]
                                  (nth possible-actions (int (q/random (count possible-actions)))))))