(ns clj-cms.templates)

(def head
  [:head [:link {:href "/css/normalize.css" :rel "stylesheet" :type "text/css"}]
         [:link {:href "/css/foundation.min.css" :rel "stylesheet" :type "text/css"}]
         [:style {:type "text/css"} "ul { padding-left: 2em }"]
         [:script {:src "/js/foundation.min.js" :type "text/javascript"}]])

(defn body
  [& content]
  [:body {:class "row"}
   (into [:div {:class "columns small-12"}] content)])
