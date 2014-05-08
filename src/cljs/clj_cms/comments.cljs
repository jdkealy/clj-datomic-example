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

(def comments (atom {:comments []}))
(def new-comment (atom {}))

(defn to-comment [comment]
  [:li {:class "list-group-item row-fluid"}
   (:comment/body comment)
   [:div {:class "btn-group pull-right"}
    [:a {
         :onClick (fn [e]
                    (print "YEAH OK")
                    )
         :class "btn btn-primary"} "REPLY"]]])

(defn comments-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (u/edn-xhr
       {:method :get
        :url (str "/comments")
        :on-complete
        (fn [res]
          (om/update! data :comments res)
          )})
      )
    om/IRender
    (render [_]
      (html
       [:div
        [:ul {:class "list-group"}
         (map to-comment (:comments data))
         ]]))))

(defn loadCommentForm [e d]
  (om/set-state! e :open true))

(defn comment-btn [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:open false})
    om/IRenderState
    (render-state [_ state]
      (html
       [:div {:id "registry"}
        [:a {
             :onClick (partial loadCommentForm owner);(fn [e] (om/set-state! owner :open true));
             :href "#"
             :class "btn btn-primary"} (str "LEAVE A COMMENT +")]
        (when (om/get-state owner :open)
          (om/build new-comment-view data {:init-state {
                                                        :commentable-id 1
                                                        }}))]))))

(defn new-comment-view [data owner]
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
                           :value (:comment/body data)
                                :onChange #(u/handle-change % data owner :comment/body)})
                 (i/submit {
                            :onClick (fn [e]
                                       (u/edn-xhr {
                                                   :method :post
                                                   :data (->
                                                          @data
                                                          (assoc :commentable-id (:commentable-id (om/get-state owner)))
                                                          (u/clean-vals))
                                                   :url "comments"
                                                   :on-comlete
                                                   (fn [res]
                                                     (print))}))}))]]))))

(om/root comment-btn new-comment {:target (gdom/getElement "new-comment")})
(om/root comments-view comments {:target (gdom/getElement "comments")})
