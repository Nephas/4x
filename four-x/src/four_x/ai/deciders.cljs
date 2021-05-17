(ns four-x.ai.deciders
  (:require [four-x.state.selectors :refer [get-expandable-neighborhood]]))

(defn decide "returns: an action for the empire" [state empire-id]
  (let [expandable-sector-ids (set (get-expandable-neighborhood state empire-id))]
    [:expand empire-id (first expandable-sector-ids)]))