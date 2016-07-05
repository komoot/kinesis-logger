(ns kinesis-logger.core
  (:require [kinesis-logger.util :as util]
            [environ.core :refer [env]]
            [clj-kinesis-worker.core :as kinesis]
            [taoensso.timbre :as log]
            [timbre-logentries.core :refer [logentries-appender raw-output]]))

(def config
  {:kinesis-client   {:region      "eu-west-1"
                      :app-name    (str "kinesis-logger-" (env :stream-name))
                      :stream-name (env :stream-name)}
   :logentries-token (env :logentries-token)})

(def logging-config
  {:level      :info
   ;; reduce logging from the slf4j adapter to WARN
   :middleware [(fn min-level-for-ns [msg]
                  (when
                      (or (not= "slf4j-timbre.adapter" (:?ns-str msg))
                          (log/level>= (:level msg) :warn))
                    msg))]

   :timestamp-opts log/default-timestamp-opts

   :output-fn log/default-output-fn

   :appenders (if-let [token (:logentries-token config)]
                {:logentries (logentries-appender
                              {:token     token
                               :output-fn raw-output})}
                {:println (log/println-appender {:stream :auto})})})

(log/set-config! logging-config)

(defrecord Logger [config]
  kinesis/RecordProcessor
  (initialize [_ _])

  (process-records [_ _ records checkpointer]
    (doseq [record (map util/parse-record records)]
      (log/info record))
    (.checkpoint checkpointer))

  (shutdown [_ _ _ _]))

;; State handling with dynamic vars. Dirty!

(def ^:dynamic *worker* nil)

(def ^:dynamic *logger-thread* nil)

(defn launch-worker!
  [config]
  (let [worker-config (merge
                       (:kinesis-client config)
                       {:processor-factory-fn #(->Logger config)})]
    (alter-var-root #'*worker* (fn [_] (kinesis/create-worker worker-config)))
    (alter-var-root #'*logger-thread* (fn [_] (log/logged-future (.run *worker*))))))

(defn -main [& args]
  (launch-worker! config))

(comment
  (require '[kinesis-logger.core :refer :all])
  (launch-worker! config)
  (future-cancel *logger-thread*)
  (.shutdown *worker*))
