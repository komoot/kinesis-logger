(ns kinesis-logger.shell
  (:gen-class))

(defn -main [& args]
  (require 'kinesis-logger.core)
  (apply (resolve 'kinesis-logger.core/-main) args))
