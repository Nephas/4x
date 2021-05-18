(ns four-x.state.reducers
  (:require [four-x.draw :refer [draw redraw-empires!]]
            [four-x.state.selectors :refer [get-action-points get-owning-empire]]))

(def reducers {:expand      (fn [state empire-id sector-id]
                              (-> state
                                  (update-in [:empires empire-id :sectors] conj sector-id)
                                  (assoc-in [:sectors sector-id :exploit] 1)))

               :exploit     (fn [state empire-id sector-id]
                              (update-in state [:sectors sector-id :exploit] inc))

               :exterminate (fn [state empire-id sector-id]
                              (if (> (get-in state [:sectors sector-id :exploit]) 1)
                                (update-in state [:sectors sector-id :exploit] dec)
                                (-> state
                                    (update-in [:empires (get-owning-empire state sector-id) :sectors]
                                               (fn [s] (filter #(not= % sector-id) s)))
                                    (update-in [:empires empire-id :sectors] conj sector-id))))

               :explore     (fn [state empire-id sector-id] state)

               :pass        (fn [state empire-id sector-id] state)})

(defn resolve-turn [state]
  (if (> (get-in state [:active :actions]) 1)
    (update-in state [:active :actions] dec)
    (-> state
        (update-in [:active] (fn [{id :empire actions :actions}]
                               (let [next (mod (inc id) (count (:empires state)))]
                                 {:empire next :actions (get-action-points state next)})))
        (update :turn inc))))

(defn reduce-action [state action]
  (println "resolving action: " action)
  (let [[kind empire-id sector-id] action
        reducer (kind reducers)
        next-state (-> state
                       (reducer empire-id sector-id)
                       (resolve-turn))]
    (do (redraw-empires! next-state)
        (.log js/console next-state)
        next-state)))