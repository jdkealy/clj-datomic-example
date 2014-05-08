(ns clj-cms.pages
  (:use
   [compojure.core                 :only [defroutes GET POST DELETE PUT ANY]]
   [compojure.route :as route]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])
   [clj-cms.config :as config])
  (:require
   [clj-cms.utils :as utils]
   [clj-cms.templates :as t]
   [cemerick.friend :as friend]
   [datomic.api :as d]
   [cheshire.core :as cc]))

(defn touch-ident [ident]
  (let [db (d/db conn)]
    (d/touch (d/entity db (first ident)))))

(defn by-id [id]
  (let [page (d/.touch (d/entity (d/db config/conn) id))]
    (assoc (into {} page) :db/id id)))

(defn by-title[title]
  (let [
        db (d/db config/conn)
        pages (d/q
               '[:find ?e
                 :in $ ?title
                 :where
                 [?e :page/title ?title]]
               db title)]
    (if-let [page (first (first pages))]
      (by-id (first (first pages)))
      false)))

(defn create [title body]
  (let [page @(d/transact
               config/conn
               [{:page/title title :page/body body :db/id #db/id[:db.part/user]}])]
    (by-id (first (vals (:tempids page))))))

(comment
  (create "FOO" "BAR")
  )

(defn list [search-params]
  (vec (map touch-ident (let [db (d/db config/conn)]
                           (d/q '[:find ?e
                                  :where
                                  [?e :page/title _]
                                  ]db)) )))

(defn handle-post [user params]
  (let [p (create
           (:page/title params)
           (:page/body params))]
    (utils/generate-response p)))

(defn pages-page [acc]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (t/pages-page acc)})

(defn page [id]
  (if-let [p (by-title id)]
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (t/page p)}
    {:status 500}))

(defn handle-update [id params])

(defroutes routes
  (POST "/" {{:keys [id]} :params body :body request :request params :edn-params}
       (friend/authenticated
        (-> (friend/current-authentication request)
            (handle-post params)
            )))
  (PUT "/:id" {{:keys [id]} :params body :body request :request params :edn-params}
       (friend/authenticated
        (->
         (friend/current-authentication request)
         (handle-update (read-string  id) params))))

  (GET "/" {{:keys [id]} :params :keys [headers params body]}
       (utils/generate-response (list "")))

  (GET "/list" request
       (friend/authenticated
        (-> (friend/current-authentication request)
            (pages-page))))
  (route/resources "/"))
