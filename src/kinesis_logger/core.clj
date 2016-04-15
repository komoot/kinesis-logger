(ns kinesis-logger.core
  (:require [kinesis-logger.util :as util]
            [environ.core :refer [env]]
            [clj-kinesis-worker.core :as kinesis]
            [taoensso.timbre :as log]
            [timbre-logentries.core :refer [logentries-appender]]))

(def config
  {:kinesis-client   {:region      "eu-west-1"
                      :app-name    "kinesis-logger"
                      :stream-name (env :stream-name)}
   :logentries-token (env :logentries-token)})

(log/merge-config!
 (merge
  {:level      :info
   ;; reduce logging from the slf4j adapter to WARN
   :middleware [(fn min-level-for-ns [msg]
                  (when
                      (or (not= "slf4j-timbre.adapter" (:?ns-str msg))
                          (log/level>= (:level msg) :warn))
                    msg))]}
  (when (:logentries-token config)
    {:appenders {:logentries (logentries-appender {:token (:logentries-token config)})}})))

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
  (launch-worker! config)
  (future-cancel *logger-thread*)
  (.shutdown *worker*))