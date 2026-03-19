(ns vetsoft.config)

(defn get-env
  "Read environment variable with a default value."
  [key default]
  (or (System/getenv key) default))

(def config
  {:database-url (get-env "DATABASE_URL" "jdbc:postgresql://localhost:5432/vetsoft?user=vetsoft&password=vetsoft")
   :port         (Integer/parseInt (get-env "PORT" "8080"))
   :admin-password (get-env "ADMIN_PASSWORD" "admin")})
