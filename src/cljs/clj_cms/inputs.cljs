(ns cljcms.inputs
  (:require [om.core :as om :include-macros true]
            [goog.dom :as gdom]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as string]))

(defn input [label name class value]
  [:div {:class "form-group"}
   [:label {:class "control-label"} label]
   [:input {:type "text" :name name :class class :value value}]])

(defn form [action & content]
  [:form
   {:action action :method "POST" :class "form-horizontal"}
   content])

(defn label-size-class [size]
  (case size
    "narrow" {:label "  col-sm-2 " :input " col-sm-10"}
    "medium" {:label "  col-sm-4 " :input " col-sm-6"}
    "wide"   {:label "  col-sm-6 " :input " col-sm-6"}
    {:label "  col-sm-2 " :input " col-sm-10"}))

(defn label [att]
  (let [label (:label att)
        label-attrs {
                     :class (str
                             "control-label "
                             (:label (label-size-class (:label-size att)))
                             )}]
    [:label label-attrs label]))

(comment
  )

(defn om-input [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (let [input-attrs {
                         :type "text"
                         :class "control"
                         :value (:value data)
                         :onChange (:onChange data)}]
        (html [:input input-attrs])))))

(defn to-option [option]
                                        ;  [:option {:value (:value) option} (:display option)]
  [:option {:value (:value option)} (:display option)]
  )
(defn om-select [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (let [input-attrs {
                         :type "text"
                         :class "control form-control"
                         :value (:value data)
                         :onChange (:onChange data)}]
        (html [:select input-attrs
               (map to-option (:options data))
               ])))))

(defn input [data att]
  [:div {:class "form-group"}
     (label att)
     [:div {:class (str (:input (label-size-class (:label-size att))))}
      (om/build om-input att)]])

(defn dropdown [data att]

  [:div {:class "form-group"}
   (label att)
   [:div {:class (str (:input (label-size-class (:label-size att))))}
    (om/build om-select att)]])

(defn submit [att]
  [:a {
       :onClick (:onClick att)
       :class "btn btn-primary"} "SUBMIT"])
