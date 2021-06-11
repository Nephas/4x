(ns four-x.state.records)

(defrecord Star [pos name col])

(defrecord Sector [pos border exploit exploit-limit])

(defrecord Empire [actions name col sectors capital conquests])
