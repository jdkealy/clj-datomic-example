(ns clj-cms.utils
  (:require
   [ring.util.response :as resp]
   [cheshire.core :as cc]))

(defn json-response
  [x]
  (-> (cc/generate-string x)
    resp/response
    (resp/content-type "application/json")))
