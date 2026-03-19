(ns vetsoft.api.prices
  (:require [ring.util.response :as resp]
            [vetsoft.db :as db]
            [cheshire.core :as json])
  (:import [java.io PipedInputStream PipedOutputStream]))

(defn get-prices
  "Handler for GET /api/prices — returns all pricing data as JSON."
  []
  (try
    (let [prices (db/get-all-prices)]
      (resp/response prices))
    (catch Exception e
      (println "Error fetching prices:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn- write-sse-event!
  "Write an SSE event to the output stream."
  [^java.io.OutputStream out data]
  (let [json-str (json/generate-string data)
        bytes (.getBytes (str "data: " json-str "\n\n") "UTF-8")]
    (.write out bytes)
    (.flush out)))

(defn prices-stream
  "SSE endpoint: GET /api/prices/stream
   Streams price updates in real-time via PostgreSQL LISTEN/NOTIFY."
  []
  (let [out (PipedOutputStream.)
        in (PipedInputStream. out 65536)]
    ;; Start listener in background
    (future
      (try
        ;; Send initial prices immediately
        (write-sse-event! out (db/get-all-prices))
        ;; Listen for DB changes and push updates
        (let [stop-fn (db/listen-price-changes!
                       (fn [_table-name]
                         (try
                           (write-sse-event! out (db/get-all-prices))
                           (catch java.io.IOException _
                              ;; Client disconnected
                             nil))))]
          ;; Keep alive until stream closes
          (while (try (.write out (.getBytes ": keepalive\n\n" "UTF-8"))
                      (.flush out)
                      (Thread/sleep 30000)
                      true
                      (catch java.io.IOException _ false)))
          (stop-fn))
        (catch Exception e
          (println "SSE stream error:" (.getMessage e)))
        (finally
          (try (.close out) (catch Exception _)))))
    ;; Return Ring streaming response
    {:status 200
     :headers {"Content-Type" "text/event-stream"
               "Cache-Control" "no-cache"
               "Connection" "keep-alive"
               "X-Accel-Buffering" "no"}
     :body in}))

