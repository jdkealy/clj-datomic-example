(ns clj-cms.users
  (:require
   [datomic.api :as d]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])
   [clj-cms.config :as config]))

(defn by-id [id]
  (let [user
        (d/touch (d/entity (d/db config/conn) id))
        ]
    {:username (:user/username user) :password (:user/password user) :id (:db/id user)}))

(defn create [username password]
  (let [
        user @(d/transact
               config/conn
               [{:user/username username :user/password (creds/hash-bcrypt password) :db/id #db/id[:db.part/user]}])
        ]
    (by-id (first (vals (:tempids user))))))

(defn by-username[uname]
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
