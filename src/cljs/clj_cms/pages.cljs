(ns cljcms.pages
  (:require
   [om.core :as om :include-macros true]
   [goog.dom :as gdom]
   [om.dom :as dom :include-macros true]
   [sablono.core :as html :refer-macros [html]]
   [cljcms.inputs :as i]
   [cljcms.utils :as u]
   [clojure.string :as string])
  )

(def pages-state (atom {:pages []}))
(def new-page (atom {:page/title "" :page/body ""}))
(def pages (atom {:pages []}))

(defn clean-vals
  [items]
  (into {} (remove (fn [[k v]] (nil? v)) items)))

(defn dissoc-static-keys
  [atom]
  (dissoc (into {} atom) :db/id ))

(defn new-page-view [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div {:id "registry"}
        [:div {:class "row-fluid"}
         (i/form "#"
                 (i/input data {
                           :label "TITLE"
                           :label-size "narrow"
                           :value (:page/title data)
                                :onChange #(u/handle-change % data owner :page/title)})
                 (i/input data {
                           :label "BODY"
                           :label-size "narrow"
                           :value (:page/body data)
                                :onChange #(u/handle-change % data owner :page/body)})

                 (i/submit {
                            :onClick (fn [e]
                                       (u/edn-xhr
                                        {:method :post
                                         :data (clean-vals (dissoc-static-keys @data))
                                         :url (str "/pages")
                                         :on-complete
                                         (fn [res]
                                           (print))}))})
                 )]]))))

(defn to-page [data]
  [:li {:class "list-group-item"}
   [:a {:href (str "/" (:page/title data))}
    (:page/title data)]])

(defn pages-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (u/edn-xhr
       {:method :get
        :url (str "/pages")
        :on-complete
        (fn [res]
          (om/update! data :pages res)
          )})
      )
    om/IRender
    (render [_]
      (html
       [:div
        [:ul {:class "list-group"}
         (map to-page (:pages data))
         ]]))))

(om/root pages-view pages {:target (gdom/getElement "pages")})
(om/root new-page-view new-page {:target (gdom/getElement "new-page")})
