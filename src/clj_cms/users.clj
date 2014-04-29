(ns clj-cms.users
  (:use
   [compojure.core                 :only [defroutes GET POST DELETE PUT ANY]]
   [compojure.route :as route]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])
   [clj-cms.config :as config])
  (:require
   [clj-cms.utils :as utils]
   [cemerick.friend :as friend]
   [datomic.api :as d]
   [cheshire.core :as cc]))

                                        ;models

(defn by-id [id]
  (let [user (d/touch (d/entity (d/db config/conn) id))]
    {:username (:user/username user) :password (:user/password user) :id (:db/id user)}))

(defn create [username password]
  (let [user @(d/transact
               config/conn
               [{:user/username username :user/password (creds/hash-bcrypt password) :db/id #db/id[:db.part/user]}])]
    (by-id (first (vals (:tempids user))))))

(defn by-username[uname]
  (let [
        db (d/db config/conn)
        users (d/q
               '[:find ?e
                 :in $ ?uname
                 :where
                 [?e :user/username ?uname]]
               db uname)]
    (if-let [user (first (first users))]
      (by-id (first (first users)))
      false)))

                                        ;views

(defn account-page [acc]
  (println acc)
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (slurp "resources/user/account.html")})

(defn login-page []
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (slurp "resources/user/login.html")})

(defn sign-up-page []
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (slurp "resources/user/sign-up.html")})

                                        ;api
(defn sign-up-handler []
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (cc/generate-string {:foo "BAR"})})

(defn current-user [request]
  (friend/authenticated
   (-> (friend/current-authentication request)
                                        ; (select-keys [:username :password])
       utils/json-response)))

                                        ;routes

(defroutes routes
  (GET "/" [request]
       (friend/authenticated
        (-> (friend/current-authentication request)
            (account-page))))
  (GET "/login" [] (login-page))
  (GET "/sign-up" [] (sign-up-page))
  (POST "/sign-up" [] (sign-up-handler))
  (GET "/echo-roles" request (current-user request))
  (GET "/admin" request
       (friend/authorize #{::user} "This page can only be seen by authenticated users."))
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
  (route/resources "/"))

                                        ;util
(defn wrap-auth [app-routes]
  (friend/authenticate app-routes
                       {
                        :login-uri "/user/login"
                        :credential-fn (partial creds/bcrypt-credential-fn by-username)
                        :workflows [(workflows/interactive-form)]}))
