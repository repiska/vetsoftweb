(ns vetsoft.admin.api
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE]]
            [ring.util.response :as resp]
            [vetsoft.db :as db]))

;; === Generic CRUD helpers ===

(defn- create-entity! [table params returning-cols]
  (let [cols (keys params)
        vals (map params cols)
        col-str (clojure.string/join ", " (map name cols))
        placeholders (clojure.string/join ", " (repeat (count cols) "?"))
        ret-str (clojure.string/join ", " (map name returning-cols))
        sql (str "INSERT INTO " table " (" col-str ") VALUES (" placeholders ") RETURNING " ret-str)]
    (db/query-one (into [sql] vals))))

(defn- update-entity! [table id params]
  (let [cols (keys params)
        set-clause (clojure.string/join ", " (map #(str (name %) " = ?") cols))
        vals (concat (map params cols) [id])
        sql (str "UPDATE " table " SET " set-clause ", updated_at = NOW() WHERE id = ?")]
    (db/execute! (into [sql] vals))
    (db/query-one [(str "SELECT * FROM " table " WHERE id = ?") id])))

(defn- delete-entity! [table id]
  (let [result (db/execute! [(str "DELETE FROM " table " WHERE id = ?") id])]
    (if (pos? (:next.jdbc/update-count result 0))
      (resp/status 204)
      (resp/not-found {:error "Not found"}))))

;; === License Tiers ===

(defn- license-params [body]
  (select-keys body [:name :version :price :badge_text :badge_color :highlight :sort_order]))

;; === Routes ===

(defroutes admin-api-routes
  ;; Licenses
  (GET "/admin/api/licenses" []
    (resp/response (db/get-licenses)))
  (POST "/admin/api/licenses" {body :body}
    (resp/created "" (create-entity! "license_tiers" (license-params body)
                                     [:id :name :version :price :badge_text :badge_color :highlight :sort_order])))
  (PUT "/admin/api/licenses/:id" [id :as {body :body}]
    (if-let [updated (update-entity! "license_tiers" (Integer/parseInt id) (license-params body))]
      (resp/response updated)
      (resp/not-found {:error "License not found"})))
  (DELETE "/admin/api/licenses/:id" [id]
    (delete-entity! "license_tiers" (Integer/parseInt id)))

  ;; Modules
  (GET "/admin/api/modules" []
    (resp/response (db/get-modules)))
  (POST "/admin/api/modules" {body :body}
    (resp/created "" (create-entity! "modules" (select-keys body [:name :note :price :sort_order])
                                     [:id :name :note :price :sort_order])))
  (PUT "/admin/api/modules/:id" [id :as {body :body}]
    (resp/response (update-entity! "modules" (Integer/parseInt id) (select-keys body [:name :note :price :sort_order]))))
  (DELETE "/admin/api/modules/:id" [id]
    (delete-entity! "modules" (Integer/parseInt id)))

  ;; Support rates
  (GET "/admin/api/support-rates" []
    (resp/response (db/get-support-rates)))
  (POST "/admin/api/support-rates" {body :body}
    (resp/created "" (create-entity! "support_rates" (select-keys body [:license_range :price :sort_order])
                                     [:id :license_range :price :sort_order])))
  (PUT "/admin/api/support-rates/:id" [id :as {body :body}]
    (resp/response (update-entity! "support_rates" (Integer/parseInt id) (select-keys body [:license_range :price :sort_order]))))
  (DELETE "/admin/api/support-rates/:id" [id]
    (delete-entity! "support_rates" (Integer/parseInt id)))

  ;; Services
  (GET "/admin/api/services" []
    (resp/response (db/get-services)))
  (POST "/admin/api/services" {body :body}
    (resp/created "" (create-entity! "services" (select-keys body [:name :price :full_width :sort_order])
                                     [:id :name :price :full_width :sort_order])))
  (PUT "/admin/api/services/:id" [id :as {body :body}]
    (resp/response (update-entity! "services" (Integer/parseInt id) (select-keys body [:name :price :full_width :sort_order]))))
  (DELETE "/admin/api/services/:id" [id]
    (delete-entity! "services" (Integer/parseInt id)))

  ;; Extra items
  (GET "/admin/api/extra-items" []
    (resp/response (db/get-extra-items)))
  (POST "/admin/api/extra-items" {body :body}
    (resp/created "" (create-entity! "extra_items" (select-keys body [:category :name :description :price :sort_order])
                                     [:id :category :name :description :price :sort_order])))
  (PUT "/admin/api/extra-items/:id" [id :as {body :body}]
    (resp/response (update-entity! "extra_items" (Integer/parseInt id) (select-keys body [:category :name :description :price :sort_order]))))
  (DELETE "/admin/api/extra-items/:id" [id]
    (delete-entity! "extra_items" (Integer/parseInt id)))

  ;; Metadata
  (GET "/admin/api/metadata" []
    (resp/response (db/get-metadata)))
  (PUT "/admin/api/metadata/:key" [key :as {body :body}]
    (db/execute! ["INSERT INTO price_metadata (key, value) VALUES (?, ?) ON CONFLICT (key) DO UPDATE SET value = ?" key (:value body) (:value body)])
    (resp/response {:key key :value (:value body)}))
  (DELETE "/admin/api/metadata/:key" [key]
    (db/execute! ["DELETE FROM price_metadata WHERE key = ?" key])
    (resp/status 204)))
