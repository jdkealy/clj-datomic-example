(defproject clj-cms "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :ring {:handler clj-cms.core/app
         :auto-reload? true}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-ring "0.8.10"]]
  :dependencies [

                 [org.clojure/clojure "1.5.1"]
                 [ring/ring "1.2.2"]
                 [com.cemerick/friend "0.2.0"]
                 [com.datomic/datomic-free "0.8.4020.26"]
                 [cheshire "4.0.3"]
                 [enlive "1.1.5"]
                 [bultitude "0.1.7"]
                 [hiccup "1.0.1"]
                 [fogus/ring-edn "0.2.0"]
                 [ring.middleware.logger "0.4.0"]
                 [compojure "1.1.6"]])
