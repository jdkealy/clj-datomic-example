(ns clj-cms.comments
  (:use
   [compojure.core                 :only [defroutes GET POST DELETE PUT ANY]]
   [compojure.route :as route]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])
   [clj-cms.config :as config])
  (:require
   [clj-cms.utils :as utils]
   [clj-cms.templates :as t]
   [clj-cms.pages :as p]
   [cemerick.friend :as friend]
   [datomic.api :as d]
   [cheshire.core :as cc]))
(comment

(d/q '[:find ?aid ?a ?coid
       :where
       [?aid :article/comments ?coid]
       [?aid :article/title ?a]]
     (d/db conn))
)

(defn add [a b]
  (+ a b)
  )
(add 1 2)
(defn by-id [id]
  (let [page (d/.touch (d/entity (d/db config/conn) id))]
    (assoc (into {} page) :db/id id)))

(defn create [title body]
  (let [page @(d/transact
               config/conn
               [{:comment/body title :page/body body :db/id #db/id[:db.part/user]}])]
    (by-id (first (vals (:tempids page))))))

(defn handle-post [user params]
  (let [p (create
           (:page/title params)
           (:page/body params))]
    (utils/generate-response p)))

(defroutes routes
  (POST "/" {{:keys [id]} :params body :body request :request params :edn-params}
       (friend/authenticated
        (-> (friend/current-authentication request)
            (handle-post params)
            )))
  (GET "/:id" {{:keys [id]} :params}
       "FOO"
       )

  (route/resources "/"))
