(ns clj-cms.users
  (:use
   [compojure.core                 :only [defroutes GET POST DELETE PUT ANY]]
   [compojure.route :as route]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])
   [clj-cms.config :as config])
  (:require
   [hiccup.core :as h]
   [clj-cms.utils :as utils]
   [clj-cms.templates :as t]
   [cemerick.friend :as friend]
   [datomic.api :as d]
   [cheshire.core :as cc]))

                                        ;models
(defn string-to-enum [i]
  (case (:user/gender (apply array-map i))
    "M" [:user/gender :user.gender/male]
    "male" [:user/gender :user.gender/male]
    "F" [:user/gender :user.gender/female]
    "female" [:user/gender :user.gender/female]
    i))

(comment
  (map string-to-enum {:user/first_name "MEOW" :user/gender "M"})
  (map (fn [e]
         (string-to-enum e)
         ) {:user/first_name "MEOW" :user/gender "M"}))


(defn by-id [id]
  (let [user (d/.touch (d/entity (d/db config/conn) id))]
    (assoc
        (assoc
            (assoc
                (into {} user)
              :username (:user/username user))
          :password (:user/password user))
      :id id)))

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

(defn update-transaction [transactions]
  @(d/transact
    config/conn
    transactions))

(comment
  (type (by-username "jdkealy@gmail.com"))
  (by-username "jdkealy@gmail.com"))

(defn create [username password]
  (if (by-username username)
    {:error "user already exist"}
    (let [user @(d/transact
               config/conn
               [{:user/username username :user/password (creds/hash-bcrypt password) :db/id #db/id[:db.part/user]}])]
      (by-id (first (vals (:tempids user)))))))

                                        ;views
(defn account-page [acc]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (t/user-page acc)})

(defn login-page [params]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (t/sign-in-page params)})

(defn sign-up-page []
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (t/sign-up-page)})

                                        ;api
(defn sign-up-handler [r]
  (let [user (create (:username r) (:password r))]
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (cc/generate-string user)}))

(defn current-user [request]
  (friend/authenticated
   (-> (friend/current-authentication request)
       utils/json-response)))

(defn user-info [request]
  (utils/generate-response (by-username (:username  request))))

(defn update-transact [transactions]
  (let [updates @(d/transact
                  config/conn
                  transactions)]
    updates
    ))
(defn map-transaction [id params]
  (vec (map (fn [e]
              (let [map-nums (string-to-enum e)]
                ;map-nums
                (assoc (apply array-map map-nums) :db/id id)
                )) params)))

(comment
  (map-transaction 1 {
                      :foo ""
                      }))

(defn update-user-info [request id params]
  (let [transaction (map-transaction id params)]
    (update-transact transaction)
    (utils/generate-response (by-id id))
    ))

(comment
  (friend/auth? {})
  (by-id 17592186045520)
  (create "jdkealy@gmai2l.com" "foobar"))
                                        ;routes

(defroutes routes
  (GET "/" request
       (friend/authenticated
        (-> (friend/current-authentication request)
            (account-page))))
  (GET "/login" {params :params} (login-page params))
  (GET "/info" request
       (friend/authenticated
        (-> (friend/current-authentication request)
            (user-info))))

  (PUT "/info/:id" {{:keys [id]} :params body :body request :request params :edn-params}
       (friend/authenticated
        (->
         (friend/current-authentication request)
         (update-user-info (read-string  id) params))))

  (GET "/sign-up" [] (sign-up-page))
  (POST "/sign-up" {params :params} (sign-up-handler params))
  (GET "/echo-roles" request (current-user request))
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
  (route/resources "/"))

                                        ;util
(defn wrap-auth [app-routes]
  (friend/authenticate app-routes
                       {
                        :login-uri "/user/login"
                        :credential-fn (partial creds/bcrypt-credential-fn by-username)
                        :workflows [(workflows/interactive-form)]}))
