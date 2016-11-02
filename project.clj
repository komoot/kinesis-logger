(defproject kinesis-logger "0.1.8"
  :description "Simple logger for Kinesis events, mainly for debugging purposes"
  :url "https://github.com/komoot/kinesis-logger"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [environ "1.0.2"]
                 [byte-streams "0.2.0"]
                 [de.komoot/clj-kinesis-worker "1.0.1"]
                 [cheshire "5.5.0"]
                 [com.taoensso/timbre "4.2.1"]
                 [de.komoot/timbre-logentries "1.0.0"]]

  :plugins [[lein-environ "1.0.2"]
            [org.clojars.jstaffans/uberimage "0.4.2"]]

  :main kinesis-logger.shell

  :target-path "target/%s/"

  :uberimage {:base-image "tifayuki/java:8"
              :files      {"run.sh" "docker/run.sh"}
              :cmd        ["/bin/sh" "/run.sh"]}

  :profiles {:uberjar      {:aot [kinesis-logger.shell]}
             :dev          [:project/dev :profiles/dev]
             ;; use profiles.clj to override
             :profiles/dev {}
             :project/dev  {:source-paths ["dev"]
                            :repl-options {:init-ns user}
                            :dependencies [[reloaded.repl "0.2.0"]
                                           [clj-kinesis-client "0.0.7"]]}})
