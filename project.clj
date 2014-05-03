(defproject clj-cms "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :ring {:handler clj-cms.core/app
         :auto-reload? true}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-ring "0.8.10"]
            [lein-cljsbuild "1.0.2"]]
  :source-paths ["src/clj" "src/cljs"]
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
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [compojure "1.1.6"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [om "0.5.3"]
                 [cljs-ajax "0.2.3"]
                 [secretary "1.1.0"]]
  :cljsbuild {:builds [{
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-dir "resources/public/js/out"
                                   :output-to "resources/public/js/main.js"
                                   :optimizations :none
                                   :source-map true
                                        }}]})
