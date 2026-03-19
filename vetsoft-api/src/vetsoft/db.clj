(ns vetsoft.db
  (:require [hikari-cp.core :as hikari]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [migratus.core :as migratus]
            [vetsoft.config :refer [config]]))

;; === Connection Pool ===

(defonce datasource (atom nil))

(defn- parse-jdbc-url
  "Parse JDBC URL into HikariCP config map."
  [url]
  {:jdbc-url url})

(defn init-pool!
  "Initialize HikariCP connection pool."
  []
  (let [pool-config (merge (parse-jdbc-url (:database-url config))
                           {:maximum-pool-size 5
                            :minimum-idle 2
                            :connection-timeout 10000})]
    (reset! datasource (hikari/make-datasource pool-config))))

(defn get-datasource []
  (or @datasource
      (do (init-pool!)
          @datasource)))

;; === Migrations ===

(defn- migratus-config []
  {:store :database
   :migration-dir "migrations"
   :db {:datasource (get-datasource)}})

(defn migrate!
  "Run pending database migrations."
  []
  (println "Running database migrations...")
  (migratus/migrate (migratus-config))
  (println "Migrations complete."))

;; === Query helpers ===

(def ^:private default-opts
  {:builder-fn rs/as-unqualified-kebab-maps})

(defn query
  "Execute a SQL query and return results as kebab-case maps."
  [sql-vec]
  (jdbc/execute! (get-datasource) sql-vec default-opts))

(defn query-one
  "Execute a SQL query and return a single result."
  [sql-vec]
  (jdbc/execute-one! (get-datasource) sql-vec default-opts))

(defn execute!
  "Execute a SQL statement (INSERT/UPDATE/DELETE)."
  [sql-vec]
  (jdbc/execute-one! (get-datasource) sql-vec default-opts))

;; === Price Queries ===

(defn get-licenses []
  (query ["SELECT id, name, version, price, badge_text, badge_color, highlight, sort_order FROM license_tiers ORDER BY sort_order ASC"]))

(defn get-extra-items []
  (query ["SELECT id, category, name, description, price, sort_order FROM extra_items ORDER BY sort_order ASC"]))

(defn get-modules []
  (query ["SELECT id, name, note, price, sort_order FROM modules ORDER BY sort_order ASC"]))

(defn get-support-rates []
  (query ["SELECT id, license_range, price, sort_order FROM support_rates ORDER BY sort_order ASC"]))

(defn get-services []
  (query ["SELECT id, name, price, full_width, sort_order FROM services ORDER BY sort_order ASC"]))

(defn get-metadata []
  (let [rows (query ["SELECT key, value FROM price_metadata"])]
    (into {} (map (fn [row] [(:key row) (:value row)]) rows))))

(defn get-all-prices
  "Return all pricing data as a single map."
  []
  {:licenses (get-licenses)
   :extra_items (get-extra-items)
   :modules (get-modules)
   :support (get-support-rates)
   :services (get-services)
   :metadata (get-metadata)})

;; === PostgreSQL LISTEN/NOTIFY for SSE ===

(defn listen-price-changes!
  "Start listening for PostgreSQL NOTIFY on 'price_changes' channel.
   Calls (callback table-name) on each notification.
   Returns a function that stops the listener when called."
  [callback]
  (let [running? (atom true)
        raw-conn (.getConnection (get-datasource))]
    ;; Set up LISTEN
    (with-open [stmt (.createStatement raw-conn)]
      (.execute stmt "LISTEN price_changes"))
    ;; Start polling thread
    (future
      (try
        (while @running?
          (let [pg-conn (.unwrap raw-conn org.postgresql.PGConnection)
                notifications (.getNotifications pg-conn 1000)]
            (when notifications
              (doseq [n notifications]
                (try
                  (callback (.getParameter n))
                  (catch Exception e
                    (println "SSE callback error:" (.getMessage e))))))))
        (catch Exception e
          (when @running?
            (println "LISTEN error:" (.getMessage e))))
        (finally
          (try (.close raw-conn) (catch Exception _)))))
    ;; Return stop function
    (fn []
      (reset! running? false))))

