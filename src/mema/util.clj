(ns mema.util)

(defn add-front
"add X to the front of the sequential? S. Returns a seq?"
  [S X]
  (conj (seq S) X) )

(defn add-back
"add X to the back of the sequential? S. Returns a vector?"
  [S X]
  (conj (vec S) X) )

(defn fassoc-in [M vkeys F & args]
  (let [V (get-in M vkeys)
        V (apply F (add-front args V)) ]
  (assoc-in M vkeys V) ))



