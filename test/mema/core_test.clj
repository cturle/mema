(ns mema.core-test
  (:require [clojure.test :refer :all]
            [mema.core :as m])
  (:import  [clojure.lang ExceptionInfo]) )


(def M-test
  {:L1 {:isa :Link, :atoms [:N1 :N2]}
   :L2 {:isa :Link, :atoms [:N2 :N3]}
   :L3 {:isa :Link, :atoms [:L1 :L2]}
   :N1  {:isa :Node}
   :N2  {:isa :Node}
   :N3  {:isa :Node, :lex "N3"}
   :lex=>node {"N3" :N3}
   :active-nodes #{:N1}
   :active-links #{:L1} })


; (pp-obj-dispatcher-test)
(deftest pp-obj-dispatcher-tests
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
(deftest pp-type-dispatcher-tests
  (testing "PP = :is-active"
    (is (= [:is-active :Link]
           (m/pp-type-dispatcher M-test :is-active :Link) ))
    (is (= [:is-active :Node]
           (m/pp-type-dispatcher M-test :is-active :Node) ))
    ))

; (get-test)
(deftest get-tests
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
(deftest get-all-tests
  (testing "PP = :is-active"
    (is (= #{:N1}
           (m/get-all M-test :is-active :Node) ))
    (is (= #{:L1}
           (m/get-all M-test :is-active :Link) ))
    ))

; (create-lex=>node-tests)
(deftest create-lex=>node-tests
  (testing "Nominal cases"
    (let [M1 (m/create-lex=>node {} "3")
          N  (get-in M1 [:lex=>node "3"]) ]
      (is (some? N))
      (is (= {:isa :Node :lex "3"}
             (get M1 N) ))))
  (testing "Checks : bad types for lex"
    (is (thrown? AssertionError (m/create-lex=>node {} 3)))
    (is (thrown? AssertionError (m/create-lex=>node {} :x)))
    (is (thrown? AssertionError (m/create-lex=>node {} 'x))) ))

; (activate-node-tests)
(deftest activate-node-tests
  (testing "Nominal cases"
    (is (m/get (m/activate-node {:M M-test :N :N1})  :is-active :N1))
    (is (m/get (m/activate-node {:M M-test :N :N2})  :is-active :N2))
    (is (m/get (m/activate-node {:M M-test :N "N3"}) :is-active :N3)) )
  (testing "Check bad cases"
    (is (thrown? ExceptionInfo (m/activate-node {:M M-test :N :L1})) ))
  )


;;; (equality-slides) cf doc/equality-states.pptx

(deftest equality-slides
  (let [M0 {:L1 {:isa :Link, :atoms [:L3 :L4]}
            :L3 {:isa :Link, :atoms [:R :N1]}
            :L4 {:isa :Link, :atoms [:X :Y]}
            :X  {:isa :Node}
            :R  {:isa :Node}
            :Y  {:isa :Node}
            :N1 {:isa :Node, :io "="}
            :io=>node {"=" :N1}
            :active-nodes #{}
            :active-links #{:L1} }
        M  (atom nil)
        N3 (atom nil)
        L2 (atom nil) ]
    (testing "SLIDE 1 : init"
      (reset! M M0)
      (is (= #{}
             (m/get-all @M :is-active :Node) ))
      (is (= #{:L1}
             (m/get-all @M :is-active :Link) ))
      )
    (testing "SLIDE 2 : '*3* = ?'"
      (swap! M m/desactivate-nodes)
      (swap! M m/activate-node :X)
      (is (m/get @M :is-active :X))
      (swap! M m/activate-node "3")
      (reset! N3 (m/get-lex=>node @M "3"))
      (is (some? @N3))
      (is (m/get @M :is-active @N3))
      (is (= #{:X @N3}
             (m/get-all @M :is-active :Node) ))
      (is (= #{:L1}
             (m/get-all @M :is-active :Link) )) )
    (testing "SLIDE 3 : '*3* = ?'"
      (swap! M m/propagate)
      (reset! L2 (m/get-link @M :X @N3))
      (is (some? @L2))
      (is (m/get @M :is-active @L2))
      (is (= #{:X @N3}
             (m/get-all @M :is-active :Node) ))
      (is (= #{:L1 @L2}
             (m/get-all @M :is-active :Link) ))
      )))


;;; (activeA (activeA ?M X) 3)
;; (def M1 (desactive-nodes M0))

; (run-tests)








