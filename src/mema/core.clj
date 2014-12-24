(ns mema.core
  (:refer-clojure :exclude [get])
  (:require [mema.util :refer :all]) )

;; let's create our own hierarchy.
(def mem-hierarchy (atom (-> (make-hierarchy)
                             (derive :Atom :Any)
                             (derive :Node :Atom)
                             (derive :Link :Atom)
                             (derive :Property :Any)
                             (derive :is-active :Property) )))

(defn pp-obj-dispatcher [M PP OBJ]
  [PP (get-in M [OBJ :isa] :Any)] )

(defmulti get #'pp-obj-dispatcher :default [:Any :Any] :hierarchy mem-hierarchy)

(defmethod get [:Any :Any] [M PP OBJ]
  (get-in M [OBJ PP]) )

(defn pp-type-dispatcher [M PP TYPE]
  [PP TYPE] )

(defmulti get-all #'pp-type-dispatcher :default [:Any :Any] :hierarchy mem-hierarchy)

; not defined by default so it raised an error if used

; Nodes are Active or (not Active). There is few Active Nodes and we want to retrieve all active nodes.
; so we update our representation as below :
;   a Node N is active <=> N is a member of ':active-nodes'
(defmethod get [:is-active :Node] [M PP OBJ]
  (contains? (clojure.core/get M :active-nodes) OBJ) )

; we can now implement get-all [is-active Node]
(defmethod get-all [:is-active :Node] [M PP OBJ]
  (clojure.core/get M :active-nodes) )

; We also need to desactivate all Nodes.
(defn desactivate-nodes
  "(Node ?X) => (not (Active ?X))"
  [M]
  (assoc M :active-nodes #{}) )

; let's do the same thing with active links :
(defmethod get [:is-active :Link] [M PP OBJ]
  (contains? (clojure.core/get M :active-links) OBJ) )

(defmethod get-all [:is-active :Link] [M PP OBJ]
  (clojure.core/get M :active-links) )

(defn get-lex=>node
  "return Node designated by Lexical L if it exists, else returns nil."
  [M L]
  (get-in M [:lex=>node L]) )

(defn get-link
  "returns Link which link atoms A1 and A2 if it exists, else returns nil."
  [M A1 A2]
  (get-in M [:atoms=>link #{A1 A2}]) )

(defn create-lex=>node
  "create, in Memory M, a new Node indexed by Lexical L. L must be a String."
  [M L]
  (assert (string? L) L)
  (let [N (gensym "Node")
        M (assoc M N {:isa :Node, :lex L})
        M (assoc-in M [:lex=>node L] N) ]
    M ))


;;; ACTIVATION

(defn lexical? [X]
  (string? X) )

(defn concept? [X]
  (or (symbol? X) (keyword? X)) )

(defn activate-node
  "Activates Node N in Memory M. N denotes a Lexical or a Concept"
  [{:keys [M N GO], :as S, :or {GO :init}}]
  (case GO
    :init
    (cond (lexical? N)
            (recur (assoc S :GO :lexical))
          (concept? N)
            (recur (assoc S :GO :concept))
          true
            (throw (ex-info "unknow index type" S)) )
    :lexical
    (if-let [NODE (get-lex=>node M N)]
      (recur (assoc S :GO :node, :N NODE))
      (recur (assoc S :GO :lexical, :M (create-lex=>node M N))) )
    :concept
    (if (= :Node (get-in M [N :isa]))
      (recur (assoc S :GO :node))
      (throw (ex-info "concept is not a Node" S)) )
    :node
    (fassoc-in M [:active-nodes] conj N) ))


;;; PROPAGATION

(defn propagate-1
  "propagate activation one step in a memory. More propagation may be needed, cf 'propagate' for full propagation."
  [M]
  M
  )

(defn propagate
  "propagate activations in a memory until no more can be done."
  [M]
  M
  )

(defn process-place
  "process argument as one of a ':X :R :Y' form. For example, (process-place :X '3') will process '3' as the place :X."
  [M P X]
  (assert (contains? #{:X :R :Y} P) (str "P (" P ") is not one of :X :R :Y"))
  (-> M
      (desactivate-nodes)
      (activate-node P)
      (activate-node X)
      (propagate) ))

;; (activeA ?M X) ->
; {L1 {isa Link, atoms [L3 L4], active true}
;  L3 {isa Link, atoms [R =], active false}
;  L4 {isa Link, atoms [X Y], active false}
;  X  {isa Node}
;  R  {isa Node}
;  Y  {isa Node}
;  =  {isa Node}
;  :_me {:active-nodes [X]} }

;; (activeA ?M 3) ->
; {L1 {isa Link, atoms [L3 L4], active true}
;  L2 {isa Link, atoms [X 3], active true} }
;  L3 {isa Link, atoms [R =], active false}
;  L4 {isa Link, atoms [X Y], active false}
;  X  {isa Node, active true},
;  3  {isa Node, active true},
;  R  {isa Node, active false},
;  Y  {isa Node, active false},
;  =  {isa Node, active false} }


;;; (activeA (activeA ?M R) =)

;; (desactive-nodes) ->
; {L1 {isa Link, atoms [L3 L4], active true}
;  L2 {isa Link, atoms [X 3], active true} }
;  L3 {isa Link, atoms [R =], active false}
;  L4 {isa Link, atoms [X Y], active false}
;  X  {isa Node, active false},
;  3  {isa Node, active false},
;  R  {isa Node, active false},
;  Y  {isa Node, active false},
;  =  {isa Node, active false} }

;; (activeA ?M R) ->
; {L1 {isa Link, atoms [L3 L4], active true}
;  L2 {isa Link, atoms [X 3], active true} }
;  L3 {isa Link, atoms [R =], active false}
;  L4 {isa Link, atoms [X Y], active false}
;  X  {isa Node, active false},
;  3  {isa Node, active false},
;  R  {isa Node, active true},
;  Y  {isa Node, active false},
;  =  {isa Node, active false} }


;; (activeA ?M =) ->
; {L1 {isa Link, atoms [L3 L4], active true}
;  L2 {isa Link, atoms [X 3], active true} }
;  L3 {isa Link, atoms [R =], active true}
;  L4 {isa Link, atoms [X Y], active true}
;  X  {isa Node, active false},
;  3  {isa Node, active false},
;  R  {isa Node, active true},
;  Y  {isa Node, active false},
;  =  {isa Node, active true} }


;;; (activeA ?M Y)

;; (desactive-nodes) ->
; {L1 {isa Link, atoms [L3 L4], active true}
;  L2 {isa Link, atoms [X 3], active true} }
;  L3 {isa Link, atoms [R =], active true}
;  L4 {isa Link, atoms [X Y], active true}
;  L5 {isa Link, atoms [Y 3], active true} }
;  X  {isa Node, active true},
;  3  {isa Node, active true},
;  R  {isa Node, active false},
;  Y  {isa Node, active true},
;  =  {isa Node, active false} }

;; (activeA ?M Y) ->
; {L1 {isa Link, atoms [L3 L4], active true}
;  L2 {isa Link, atoms [X 3], active true} }
;  L3 {isa Link, atoms [R =], active true}
;  L4 {isa Link, atoms [X Y], active true}
;  L5 {isa Link, atoms [Y 3], active true} }
;  X  {isa Node, active true},
;  3  {isa Node, active true},
;  R  {isa Node, active false},
;  Y  {isa Node, active true},
;  =  {isa Node, active false} }

;;; reponse

;; (desactive-nodes) ->
; {L1 {isa Link, atoms [L3 L4], active true}
;  L2 {isa Link, atoms [X 3], active true} }
;  L3 {isa Link, atoms [R =], active true}
;  L4 {isa Link, atoms [X Y], active true}
;  L5 {isa Link, atoms [Y 3], active true} }
;  X  {isa Node, active false},
;  3  {isa Node, active false},
;  R  {isa Node, active false},
;  Y  {isa Node, active false},
;  =  {isa Node, active false} }

;; (activeA ?M Y) ->
; {L1 {isa Link, atoms [L3 L4], active true}
;  L2 {isa Link, atoms [X 3], active true} }
;  L3 {isa Link, atoms [R =], active true}
;  L4 {isa Link, atoms [X Y], active true}
;  L5 {isa Link, atoms [Y 3], active true} }
;  X  {isa Node, active true},
;  3  {isa Node, active true},
;  R  {isa Node, active false},
;  Y  {isa Node, active true},
;  =  {isa Node, active false} }


