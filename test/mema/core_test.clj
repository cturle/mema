(ns mema.core-test
  (:require [clojure.test :refer :all]
            [mema.core :as m]))


(def M-test
  {:L1 {:isa :Link, :atoms [:N1 :N2]}
   :L2 {:isa :Link, :atoms [:N2 :N3]}
   :L3 {:isa :Link, :atoms [:L1 :L2]}
   :N1  {:isa :Node}
   :N2  {:isa :Node}
   :N3  {:isa :Node}
   :active-nodes #{:N1}
   :active-links #{:L1} })


; (pp-obj-dispatcher-test)
(deftest pp-obj-dispatcher-test
  (testing "PP = :isa"
    (is (= [:isa :Link]
           (m/pp-obj-dispatcher M-test :isa :L1) ))
    (is (= [:isa :Node]
           (m/pp-obj-dispatcher M-test :isa :N1) ))
    )
  (testing "PP = :active"
    (is (= [:is-active :Link]
           (m/pp-obj-dispatcher M-test :is-active :L1) ))
    (is (= [:is-active :Node]
           (m/pp-obj-dispatcher M-test :is-active :N1) ))
    ))

; (p-type-dispatcher-test)
(deftest pp-type-dispatcher-test
  (testing "PP = :is-active"
    (is (= [:is-active :Link]
           (m/pp-type-dispatcher M-test :is-active :Link) ))
    (is (= [:is-active :Node]
           (m/pp-type-dispatcher M-test :is-active :Node) ))
    ))

; (get-test)
(deftest get-test
  (testing "PP = :isa"
    (is (= :Link
           (m/get M-test :isa :L1) ))
    (is (= :Node
           (m/get M-test :isa :N1) ))
    )
  (testing "PP = :is-active"
    (is (= true
           (m/get M-test :is-active :L1) ))
    (is (= false
           (m/get M-test :is-active :L2) ))
    (is (= true
           (m/get M-test :is-active :N1) ))
    (is (= false
           (m/get M-test :is-active :N2) ))
    ))

; (get-all-test)
(deftest get-all-test
  (testing "PP = :is-active"
    (is (= #{:N1}
           (m/get-all M-test :is-active :Node) ))
    (is (= #{:L1}
           (m/get-all M-test :is-active :Link) ))
    ))



;;; (equality-slides) cf doc/equality-states.pptx

(deftest equality-slides
  (let [M (atom {})]
    (testing "SLIDE 1 : init"
      (reset! M  {:L1 {:isa :Link, :atoms [:L3 :L4]}
                  :L3 {:isa :Link, :atoms [:R :N1]}
                  :L4 {:isa :Link, :atoms [:X :Y]}
                  :X  {:isa :Node}
                  :R  {:isa :Node}
                  :Y  {:isa :Node}
                  :N1 {:isa :Node, :io "="}
                  :io=>node {"=" :N1}
                  :active-nodes #{}
                  :active-links #{:L1} })
      (is (= #{}
             (m/get-all @M :is-active :Node) ))
      (is (= #{:L1}
             (m/get-all @M :is-active :Link) ))
      )))


;;; (activeA (activeA ?M X) 3)
;; (def M1 (desactive-nodes M0))

; (run-tests)








