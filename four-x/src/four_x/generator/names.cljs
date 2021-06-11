(ns four-x.generator.names
  (:require [quil.core :as q]))

(def CONSONANTS ["b" "c" "d" "f" "g" "h" "j" "k" "l" "m" "n" "p" "q" "r" "s" "t" "v" "w" "x" "y" "z" "'"])

(def VOWELS ["a" "e" "i" "o" "u"])

(defn rand-name [length]
  (clojure.string/join (for [i (range length)]
                         (if (even? i)
                           (rand-nth CONSONANTS)
                           (rand-nth VOWELS)))))

(defn generate-empire-name []
  (str (rand-name (int (q/random 5 8))) " "
       (rand-nth ["confederacy"
                  "union"
                  "empire"
                  "states"
                  "theocracy"
                  "republic"
                  "commonwealth"])))