(ns four-x.state.reducers
  (:require [four-x.draw :refer [draw redraw-empires!]]))

(def reducers {:expand      (fn [state empire-id sector-id]
                              (update-in state [:empires empire-id :sectors] conj sector-id))

               :explore     (fn [state empire-id sector-id] state)

               :exploit     (fn [state empire-id sector-id]
                              (update-in state [:sectors sector-id :exploit] inc))

               :exterminate (fn [state empire-id sector-id] state)

               :pass        (fn [state empire-id sector-id] state)})

(defn reduce-action [state action]
  (println "resolving action: " action)
  (let [[kind empire-id sector-id] action
        reducer (kind reducers)
        next-state (-> state
                       (reducer empire-id sector-id)
                       (update :turn inc)
                       (update :acting (fn [id] (mod (inc id) (count (:empires state))))))]
    (do (redraw-empires! next-state)
        next-state)))