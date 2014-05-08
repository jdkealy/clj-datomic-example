(ns clj-cms.config
  (:require
   [datomic.api :as d])
  )

(def uri "datomic:free://localhost:4334/blog_six")

;; create database
(d/create-database uri)

;; connect to database
(def conn (d/connect uri))

;; parse schema edn file
(def schema-tx (read-string (slurp "resources/schema/users.edn")))

;; submit schema transaction
(d/transact conn schema-tx)
