(ns cljcms.comments
  (:use [domina :only [by-id value]])
  (:require
   [om.core :as om :include-macros true]
   [goog.dom :as gdom]
   [om.dom :as dom :include-macros true]
   [sablono.core :as html :refer-macros [html]]
   [cljcms.inputs :as i]
   [cljcms.utils :as u]

   [clojure.string :as string])
  )

(def comments (atom
               {
                :new-comment ""
                :comments []}))

(defn to-comment [data owner opts]
  (reify
    om/IInitState
    (init-state [_]
      {:open false})
    om/IRenderState
    (render-state [_ state]
      (print opts)
      (html
       [:div
        [:li {:class (str "list-group-item row-fluid" (:class opts))}
         (:comment/body data)
         [:div {:class "btn-group pull-right"}
          [:a {
               :onClick (fn [e]
                          (om/set-state! owner :open true))
               :class "btn btn-primary"} "REPLY"]]]
        (map (fn [e]                 (om/build to-comment e {:opts {:class "child inset"}}))(:comments data))
        (when
            (om/get-state owner :open)
          [:li {:class "list-group-item row-fluid"}
           (om/build new-comment-view data {:init-state {:commentable-id (str (:db/id data))}})])]))))

(defn comments-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (u/edn-xhr
       {:method :get
        :url (str "/comments?commentable-id=" (value (by-id "page-id")))
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
         (map (fn [e]
                (om/build to-comment e )
                )(:comments data))
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
                                                        :commentable-id (value (by-id "page-id"))
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
                                   :value (:new-comment data)
                                   :onChange #([ee ff]
                                                 (om/update! data :new-comment (.. % -target -value)))})
                 (i/submit {
                            :onClick (fn [e]
                                       (u/edn-xhr {
                                                   :method :post
                                                   :data (->
                                                          {:comment/body (:new-comment @data)}
                                                          (assoc :commentable-id (:commentable-id (om/get-state owner)))
                                                          (u/clean-vals))
                                                   :url "comments"
                                                   :on-comlete
                                                   (fn [res]
                                                     (print))}))}))]]))))

(om/root comment-btn  comments {:target (gdom/getElement "new-comment")})
(om/root comments-view comments {:target (gdom/getElement "comments")})
