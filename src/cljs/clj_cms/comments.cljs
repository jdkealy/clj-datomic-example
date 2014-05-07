(ns cljcms.comments
  (:use [domina :only [by-id value]])
  (:require
   [om.core :as om :include-macros true]
   [goog.dom :as gdom]
   [om.dom :as dom :include-macros true]
   [sablono.core :as html :refer-macros [html]]
   [cljcms.inputs :as i]
   [cljcms.utils :as u]
   [domina :as d]
   [clojure.string :as string])
  )

(print "HELLO WOLRD")

(def comments (atom {:comments []}))
(def new-comment (atom {
                        :page_id (.-value (gdom/getElement "page-id"))
                        :body ""})
  )

(defn comments-view [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div {:id "registry"}
        [:div {:class "row-fluid"}
         (i/form "#"
                 (i/textarea data {
                           :label "BODY"
                           :label-size "narrow"
                           :value (:page/body data)
                                :onChange #(u/handle-change % data owner :page/body)})
                 (i/submit {
                            :onClick (fn [e]
                                       (u/edn-xhr
                                        {:method :post
                                         :data (u/clean-vals @data)
                                         :url "comments"
                                         :on-complete
                                         (fn [res]
                                           (print))}))})
                 )]]))))

(om/root comments-view new-comment {:target (gdom/getElement "comments")})
