(ns cljcms.core
  (:require
   [om.core :as om :include-macros true]
   [goog.dom :as gdom]
   [om.dom :as dom :include-macros true]
   [sablono.core :as html :refer-macros [html]]
   [cljcms.inputs :as i]
   [cljcms.utils :as u]
   [cljcms.validators :as v]
   [clojure.string :as string])
  )


(def app-state (atom
                {:user/first_name ""
                 :user/last_name ""
                 :user/username ""
                 :user/zip ""}))


(defn handle-change [e data owner key]
  (let [value (.. e -target -value)]
    (om/update! data key value)
    (print @data)
    ))

(defn commit-change [text owner]
  (om/set-state! owner :editing false))

(extend-type string
  ICloneable
  (-clone [s] (js/String. s)))

(extend-type js/String
  ICloneable
  (-clone [s] (js/String. s))
  om/IValue
  (-value [s] (str s)))

(enable-console-print!)

(defn app-view [data owner]
  (reify
    om/IInitState
    (init-state [_]
      (u/edn-xhr
       {:method :get
        :url (str "/user/info")
        :on-complete
        (fn [res]
          (om/update! data :id (:id res))
          (om/update! data :user/first_name (:user/first_name res))
          (om/update! data :user/last_name (:user/last_name res))
          (om/update! data :user/zip (:user/zip res))
          )})
      )
    om/IRenderState
    (render-state [_ state]
      (html
       [:div {:id "registry"}
        [:div {:class "row-fluid"}
         [:h4
          (str
           (:user/last_name data)
           (when
               (and
                (> (count (str (:user/last_name data))) 0 )
                (> (count (str (:user/first_name data))) 0))
             ", "  )
           (:user/first_name data))]
         (i/form "#"
                 (i/input data {
                           :label "First Name"
                           :label-size "narrow"
                           :value (:user/first_name data)
                           :onChange #(handle-change % data owner :user/first_name)})
                 (i/input data {
                           :label "Last Name"
                           :label-size "narrow"
                           :value (:user/last_name data)
                                :onChange #(handle-change % data owner :user/last_name)})
                 (i/input data {
                           :label "ZIP"
                           :label-size "narrow"
                           :value (:user/zip data)
                                :onChange #(handle-change % data owner :user/zip)})
                 (i/submit {
                            :onClick (fn [e]

                                       (u/edn-xhr
                                        {:method :put
                                         :data (dissoc (into {} @app-state) :user/username :id :username)
                                         :url (str "/user/info/" (:id @app-state))
                                         :on-complete
                                         (fn [res]
                                           (print res))}))})
                 )]]))))

(om/root app-view app-state {:target (gdom/getElement "body")})
