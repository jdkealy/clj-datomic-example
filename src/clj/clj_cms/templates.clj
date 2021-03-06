(ns clj-cms.templates
  (:require
   [hiccup.core :as h]))

(def head
  [:head [:link {:href "/css/main.css" :rel "stylesheet" :type "text/css"}]
   [:link {:href "/css/bootstrap.min.css" :rel "stylesheet" :type "text/css"}]
   [:script {:type "text/javascript" :src "http://fb.me/react-0.9.0.js"}]
   [:script {:type "text/javascript" :src "js/out/goog/base.js"}]
   [:script {:type "text/javascript" :src "/js/main.js"}]
])

(defn body
  [& content]
  [:body {:class ""}
   [:div {:class "container"}
    [:div {:class "row"}
     (into [:div {:class "col-sm-12"}] content)]]])

(defn form [action & content]
  [:form
   {:action action :method "POST" :class "form-horizontal"}
   content])

(defn form-group [contents]
  [:div {:class "form-group"} contents])

(defn input [label name class value]
  [:div {:class "form-group"}
   [:label {:class "control-label"} label]
   [:input {:type "text" :name name :class class :value value}]])

(defn password [label name class]
  [:div {:class "form-group"}
   [:label {:class "control-label"} label]
   [:input {:type "password" :name name :class class}]])

(defn button [label class]
  [:div {}]
  (form-group [:button {:href "#"  :class class} label]))

(defn navbar [& content]
  [:div {:class "navbar navbar-default"}
   [:div {:class "collapse navbar-collapse"} content]])

(defn nav []
  (navbar
   [:ul {:class "nav navbar-nav"}
    [:li {:class ""} [:a {:href "/user/sign-up"} "SIGN UP"]]
    [:li {:class ""} [:a {:href "/user/login"} "SIGN IN"]]
    [:li {:class ""} [:a {:href "/user/logout"} "LOGOUT"]]
    [:li {:class ""} [:a {:href "/pages/list"} "MY PAGES"]]
    [:li {:class ""} [:a {:href "/user/"} "MY ACCOUT"]]]))

(defn layout [& content]
  (h/html
   head
   (body
    (nav)
    content)))

(defn home-page []
  (layout))

(defn user-page [user]
  (layout [:div {:id "body"}][:script {:type "text/javascript" :src "/js/load.js"}]))

(defn pages-page [user]
  (layout
   [:div {:id "pages"}]
   [:div {:id "new-page"}]
   [:script {:type "text/javascript" :src "/js/pages.js"}]))

(defn page [page]
  (layout
   [:h2 (:db/id page)]
   (:page/body page)
   [:div {:id "new-comment"}]
   [:div {:id "comments"}]
   [:input {:id "page-id" :type "hidden" :value (str (:db/id page))}]
   [:input {:id "page-id" :type "hidden" :value (str (:db/id page))}]
   [:script {:type "text/javascript" :src "/js/comments.js"}]))

(defn sign-in-page [params]
  (layout
   (form "/user/login"
         (input  "USERNAME" "username" "text input form-control" (:username params))
         (password  "PASSWORD" "password" "text input form-control")
         (button "PASSWORD"  "btn-primary btn form-control"))))

(defn sign-up-page []
  (layout
   (form "/user/sign-up"
         (input  "USERNAME" "username" "text input form-control" "")
         (password  "PASSWORD" "password" "text input form-control")
         (password  "PASSWORD" "password_confirmation" "text input form-control")
         (button "PASSWORD" "btn-primary btn form-control"))))
