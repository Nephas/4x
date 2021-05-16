(ns four-x.state.actions)

; empire X expand into sector Y
(defn expand #(vector :expand %1 %2))

; empire X explore sector Y
(defn explore #(vector :explore %1 %2))

; empire X exploit sector Y
(defn exploit #(vector :exploit %1 %2))

; empire X exterminate sector Y
(defn exterminate #(vector :exterminate %1 %2))
