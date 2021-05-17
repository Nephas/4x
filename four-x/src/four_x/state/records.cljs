(ns four-x.state.records)

(defrecord Star [pos name col])

(defrecord Sector [pos border exploit])

(defrecord Empire [name col sectors])
