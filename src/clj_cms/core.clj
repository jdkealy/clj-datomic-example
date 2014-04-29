(ns clj-cms.core
  (:use
   [ring.adapter.jetty             :only [run-jetty]]
   [compojure.core                 :only [defroutes GET POST DELETE PUT ANY]]
   [ring.middleware.params         :only [wrap-params]]
                                        ;[ring.middleware.logger :as log]
   [ring.middleware  keyword-params file file-info stacktrace reload]
   )
  (:require
   [compojure.route :as route]
   [cheshire.core :as cc]
   [cemerick.friend :as friend]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])
   [compojure.handler :as handler]
   [clj-cms.config :as config]
   ))



(defn get-user [id]
  (let [todo
        (d/touch (d/entity (d/db config/conn) id))
        ]
    {:username (:user/username todo) :password (:user/password todo) :id (:db/id todo)}))

(defn create-user [username password]
  (let [
        todo @(d/transact
               config/conn
               [{:user/username username :user/password (creds/hash-bcrypt password) :db/id #db/id[:db.part/user]}])
        ]
    (get-user (first (vals (:tempids todo))))))

(defn load-user-record [uname]
  (let [
        db (d/db config/conn)
        users (d/q
               '[:find ?e
                 :in $ ?uname
                 :where
                 [?e :user/username ?uname]
                 ]
               db uname)]
    (if-let [user (first (first users))]
      (get-user (first (first users)))
      false)))

(defn index-page []
  {
   :status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (slurp "resources/layout.html")})

(defroutes routes
  (GET "/" [] (index-page))
  (GET "/authorized" request
       (friend/authorize #{::user} "This page can only be seen by authenticated users."))
  (GET "/admin" request
       (friend/authorize #{::admin} "This page can only be seen by authenticated users."))
  (GET "/login" [] (index-page))
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(def app-routes (
          ->
          routes
          wrap-params
          wrap-keyword-params
          wrap-reload
         ))

(def app
  (handler/site
   (friend/authenticate app-routes
                        {
                         :login-uri "/login"
                         :credential-fn (partial creds/bcrypt-credential-fn load-user-record)
                         :workflows [(workflows/interactive-form)]})))
