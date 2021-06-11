(ns four-x.state.reducers
  (:require [four-x.draw :refer [draw redraw-empires!]]
            [four-x.state.selectors :refer [get-action-points get-owning-empire is-capital?]]))

(def reducers {:expand      (fn [state empire-id sector-id]
                              (-> state
                                  (update-in [:empires empire-id :sectors] conj sector-id)
                                  (assoc-in [:sectors sector-id :exploit] 1)))

               :exploit     (fn [state empire-id sector-id]
                              (update-in state [:sectors sector-id :exploit] inc))

               :exterminate (fn [state empire-id sector-id]
                              (let [target-empire (get-owning-empire state sector-id)]
                                (if (> (get-in state [:sectors sector-id :exploit]) 1)
                                  (update-in state [:sectors sector-id :exploit] (fn [e] (max 1 (- e 2))))
                                  (-> state
                                      (update-in [:empires target-empire :sectors] (fn [s] (filter #(not= % sector-id) s)))
                                      (update-in [:empires empire-id :sectors] conj sector-id)
                                      (update-in [:empires empire-id :conquests] (if (is-capital? state target-empire sector-id) inc identity))))))

               :explore     (fn [state empire-id sector-id] state)

               :pass        (fn [state empire-id sector-id] state)})

(defn cleanup [state]
  (loop [current-state state
         [[id empire] & remaining] (:empires state)]

    (let [next-state (if (not (contains? (set (:sectors empire)) (:capital empire)))
                       (-> current-state
                           (assoc-in [:empires id :sectors] [])
                           (assoc-in [:empires id :actions] 0))
                       current-state)]
      (if (empty? remaining) next-state
                             (recur next-state remaining)))))

(defn resolve-turn [state]
  (let [empire-id (get-in state [:active :empire])
        next-state (update-in state [:empires empire-id :actions] dec)]
    (if (> (get-in state [:empires empire-id :actions]) 1)
      next-state
      (do (redraw-empires! next-state)
          (-> next-state
              (update-in [:empires empire-id :actions] (fn [actions] (+ actions (get-action-points state empire-id))))
              (update-in [:active :empire] (fn [id] (mod (inc id) (count (:empires state)))))
              (update :turn (if (zero? empire-id) inc identity)))))))

(defn reduce-action [state action]
  (.log js/console "Resolving Action: " action)
  (let [[kind empire-id sector-id] action
        reducer (kind reducers)
        next-state (-> state
                       (reducer empire-id sector-id)
                       (cleanup)
                       (resolve-turn))]
    (do (when (zero? empire-id) (redraw-empires! next-state))
        (.log js/console next-state)
        next-state)))