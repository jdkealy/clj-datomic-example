(ns clj-cms.core
  (:use
   [ring.adapter.jetty             :only [run-jetty]]
   [compojure.core                 :only [defroutes GET POST DELETE PUT ANY context]]
   [ring.middleware.params         :only [wrap-params]]
   [ring.middleware  keyword-params file file-info stacktrace reload]
   )
  (:require
   [compojure.route :as route]
   [cheshire.core :as cc]
   [compojure.handler :as handler]
   [clj-cms.users :as users]
   ))

(defn index-page []
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (slurp "resources/home.html")})

(defroutes routes
  (GET "/" [] (index-page))
  (context "/user" [] users/routes)
  (GET "/login" [] (index-page))
  (route/resources "/")

  (route/not-found "<h1>Page not found</h1>"))

(def app-routes (
                 ->
                 routes
                 wrap-params
                 wrap-keyword-params
                 wrap-reload))

(def app
  (handler/site (users/wrap-auth app-routes)))
