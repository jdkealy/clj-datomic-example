(ns clj-cms.core
  (:use
   [ring.adapter.jetty             :only [run-jetty]]

   [compojure.core                 :only [defroutes GET POST DELETE PUT ANY context]]
   [ring.middleware.params         :only [wrap-params]]
   [ring.middleware.edn :refer [wrap-edn-params]]
   [ring.middleware  keyword-params file file-info stacktrace reload]
   )
  (:require
   [compojure.route :as route]
   [cheshire.core :as cc]
   [compojure.handler :as handler]
   [clj-cms.users :as users]
   [clj-cms.templates :as t]))

(defn index-page []
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (t/home-page)})

(defroutes routes
  (GET "/" [] (index-page))
  (context "/user" [] users/routes)
  (GET "/login" [] (index-page))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))

(def app-routes (
                 ->
                 routes
                 wrap-reload
                 users/wrap-auth
                 wrap-params
                 wrap-keyword-params
                 wrap-edn-params
                 ))


(defonce session-store (ring.middleware.session.memory/memory-store))

(def app (handler/site app-routes
                {:session {:store session-store}}))
