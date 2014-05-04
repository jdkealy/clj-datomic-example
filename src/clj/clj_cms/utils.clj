(ns clj-cms.utils
  (:require
   [ring.util.response :as resp]
   [cheshire.core :as cc]))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn json-response
  [x]
  (-> (cc/generate-string x)
    resp/response
    (resp/content-type "application/json")))
