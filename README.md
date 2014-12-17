# AAM : Artificial Associative Memory

A Clojure library designed to research about how to handle memory in artificial agent.


## Description

Continous Rule Induction

Rule learning / Induction

neural network

sequential learning

associative memory

grounded

artificial general intelligence

### constraints
reinforcement learning

case-based

pattern recognition

continous learning

simple, naive

implemented in clojure

### problems to solve

rule activation

### goals

multi-domain

## Definition

### Concepts

Node : a Node represent a concept. a Node is an Atom.
Link : a Link is between 2 Atoms. a Link is an Atom.
Atom : a Node or a Link.

Atoms are active or inactive.

### Semantic

A1, A2 : Atoms.

Link{A1, A2} active ===interpretation===> A1, A2 are active.

### Rules

(and (Active N1) (Active N2)) =>  (Active (Link N1 N2))

(and (Active A1) (Active (Link A1 A2))) =>  (Active A2)


### Productions

(and (Active N1) (Active N2) (not (Active (Link N1 N2)))) => Activer (Link N1 N2)

(and (Active N1) (Active N2) (not (Link N1 N2))) => Creer (Link N1 N2)

(and (Active A1) (Active (Link A1 A2)) (not (Active A2))) => Activer A2


## Example 1 : check an Equality AAM Network

In this example we will show how

### initial state

M1 : a memory which has learned the equality relation.

> input : X = ?

Enter, then

> 3 = 3







## Usage

FIXME

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
