(ns user
  (:require [environ.core :refer [env]]))

;; Typical local config

(def config
  {:kinesis-client   {:kinesis     {:endpoint "http://localhost:4568"}
                      :dynamodb    {:endpoint "http://localhost:4567"}
                      :app-name    "kinesis-logger"
                      :stream-name "some-stream"}
   :logentries-token (env :logentries-token)})
