(ns kinesis-logger.util
  (:require [byte-streams :as bytes]
            [cheshire.core :as json]))

(defn parse-record
  [r]
  "Parses a Record instance into a hash map."
  (-> r
      .getData
      (bytes/convert String)
      json/parse-string))
