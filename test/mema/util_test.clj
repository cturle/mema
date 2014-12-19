(ns mema.util-test
  (:require [clojure.test :refer :all]
            [mema.util :refer :all]))

; (add-front-tests)
(deftest add-front-tests
  (is (= [:x1] (add-front [] :x1)))
  (is (= [:x2 :x1] (add-front  [:x1] :x2)))
  (is (= [:x2 :x1] (add-front '(:x1) :x2)))
  )

; (add-back-tests)
(deftest add-back-tests
  (is (= [:x1] (add-back [] :x1)))
  (is (= [:x1 :x2] (add-back  [:x1] :x2)))
  (is (= [:x1 :x2] (add-back '(:x1) :x2)))
  )

; (fassoc-in-tests)
(deftest fassoc-in-tests
  (let [M {:k1 {:k2 4}}]
    (is (= 2
           (get-in (fassoc-in M [:k1 :k2] / 2) [:k1 :k2]) ))))


; (run-tests)
