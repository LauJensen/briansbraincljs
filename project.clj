(defproject brianscript "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [domina                    "1.0.2"]
                 [lein-light-nrepl          "0.0.18"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
            [lein-light-nrepl  "0.0.18"]]

  :source-paths ["src"]
  :repl-option {:nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]}

  :cljsbuild {
    :builds [{:id "brianscript"
              :source-paths ["src"]
              :compiler {
                :output-to "brianscript.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
