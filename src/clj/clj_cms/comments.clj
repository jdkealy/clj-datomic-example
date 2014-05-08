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

(defn by-id [id]
  (let [comment (d/.touch (d/entity (d/db config/conn) id))]
    (assoc (into {} comment) :db/id id)))

(defn create [body comment-id resource-id]
  (let [comment @(d/transact
               config/conn
               [
                {:comment/body body :db/id comment-id}
                {:db/id resource-id :comments comment-id}])]
    (by-id (first (vals (:tempids comment))))))


(defn touch-id [id]
  (let [db (d/db conn)]
    (d/touch (d/entity db id))))

(defn touch-ident [ident]
  (let [db (d/db conn)
        ident (d/touch (d/entity db (first ident)))]
    ident
    ))

(defn touch-accessor [ident]
  (let [db (d/db conn)
        item (d/touch (d/entity db (:db/id ident)))]
    (assoc (into {:db/id (:db/id item)} item) :comments (map touch-accessor (:comments item)))
    ;(into {:db/id (:db/id item)} item)
    ))

(defn all [commentable]
  (if commentable
    (let [item (touch-ident [commentable])]
      (vec (map touch-accessor (:comments item))))
    (let [
          db (d/db config/conn)
          comments (d/q
                    '[:find ?e
                      :where
                      [?e :comment/body _]]
                    db)]
      (map touch-ident comments))))

(cc/generate-string  (all 17592186045421))
(all 17592186045421)

(defn handle-post [user params]
  (let [comment-id (d/tempid :db.part/user)
        post-id (read-string (:commentable-id params))
        comment (create (:comment/body params) comment-id post-id)
        ]
    (utils/generate-response comment)))

(defroutes routes
  (POST "/" {{:keys [id]} :params body :body request :request params :edn-params}
       (friend/authenticated
        (-> (friend/current-authentication request)
            (handle-post params)
            )))
  (GET "/" {params :params}
       (utils/generate-response (all (read-string (:commentable-id params)))))


  (route/resources "/"))
