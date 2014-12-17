(ns mema.core
  (:refer-clojure :exclude [get]) )

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
(defn desactive-nodes
  "(Node ?X) => (not (Active ?X))"
  [M]
  (assoc M :active-nodes #{}) )

; let's do the same thing with active links :
(defmethod get [:is-active :Link] [M PP OBJ]
  (contains? (clojure.core/get M :active-links) OBJ) )

; we can now implement get-all [is-active Node]
(defmethod get-all [:is-active :Link] [M PP OBJ]
  (clojure.core/get M :active-links) )


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


