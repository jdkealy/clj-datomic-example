(ns cljcms.utils
  (:require
   [om.core :as om :include-macros true]
   [cljs.reader :as reader]
   [goog.events :as events])
  (:import
   [goog.net XhrIo]
   goog.net.EventType
   [goog.events EventType]))

(enable-console-print!)

(extend-type string
  ICloneable
  (-clone [s] (js/String. s)))

(extend-type js/String
  ICloneable
  (-clone [s] (js/String. s))
  om/IValue
  (-value [s] (str s)))

(def ^:private meths
  {:get "GET"
   :put "PUT"
   :post "POST"
   :delete "DELETE"})

(defn edn-xhr [{:keys [method url data on-complete]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.COMPLETE
      (fn [e]
        (on-complete (reader/read-string (.getResponseText xhr)))))
    (. xhr
      (send url (meths method) (when data (pr-str data))
        #js {"Content-Type" "application/edn"}))))


(defn handle-change [e data owner key]
  (let [value (.. e -target -value)]
    (om/update! data key value)))

(defn clean-vals
  [items]
  (into {} (remove (fn [[k v]] (nil? v)) items)))
