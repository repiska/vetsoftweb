(ns vetsoft.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :as resp]
            [compojure.core :refer [defroutes GET context routes]]
            [compojure.route :as route]
            [vetsoft.config :refer [config]]
            [vetsoft.db :as db]
            [vetsoft.api.prices :as prices-api]
            [vetsoft.middleware :as mw]
            [vetsoft.admin.api :as admin-api]
            [vetsoft.admin.views :as admin-views])
  (:gen-class))

;; === Public Routes ===

(defroutes public-routes
  (GET "/" [] (resp/resource-response "public/index.html"))
  (GET "/api/prices" [] (prices-api/get-prices))
  (GET "/api/prices/stream" [] (prices-api/prices-stream))
  (GET "/health" []
    (try
      (db/query-one ["SELECT 1 AS ok"])
      (resp/response {:status "ok" :db "connected"})
      (catch Exception e
        {:status 503
         :body {:status "error" :db (.getMessage e)}}))))

;; === Admin Routes (protected by basic auth) ===

(def admin-routes
  (mw/wrap-basic-auth
    (routes admin-api/admin-api-routes
            admin-views/admin-view-routes)))

;; === Combined Routes ===

(defroutes app-routes
  public-routes
  admin-routes
  (route/not-found {:status 404 :body {:error "Not found"}}))

;; === Middleware ===

(defn wrap-exception [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (println "ERROR:" (.getMessage e))
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body "{\"error\":\"Internal server error\"}"}))))

(defn wrap-logging [handler]
  (fn [request]
    (let [start (System/currentTimeMillis)
          response (handler request)
          duration (- (System/currentTimeMillis) start)]
      (println (str (:request-method request) " " (:uri request) " → " (:status response) " (" duration "ms)"))
      response)))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})
      (wrap-resource "public")
      wrap-content-type
      wrap-not-modified
      wrap-logging
      wrap-exception))

;; === Server ===

(defn -main [& _args]
  (println "Starting VetSoft server...")
  (db/init-pool!)
  (db/migrate!)
  (let [port (:port config)]
    (println (str "Server running on http://localhost:" port))
    (println (str "Admin panel: http://localhost:" port "/admin/"))
    (jetty/run-jetty app {:port port :join? true})))
