(ns vetsoft.middleware
  (:require [vetsoft.config :refer [config]])
  (:import [java.util Base64]))

(defn- decode-basic-auth
  "Decode Base64 Basic auth header. Returns [username password] or nil."
  [auth-header]
  (when (and auth-header (.startsWith auth-header "Basic "))
    (try
      (let [encoded (subs auth-header 6)
            decoded (String. (.decode (Base64/getDecoder) encoded) "UTF-8")
            [user pass] (clojure.string/split decoded #":" 2)]
        [user pass])
      (catch Exception _ nil))))

(defn wrap-basic-auth
  "Ring middleware for HTTP Basic Authentication.
   Only applies to /admin/* paths. Checks password against ADMIN_PASSWORD from config."
  [handler]
  (fn [request]
    (if-not (.startsWith (or (:uri request) "") "/admin")
      (handler request)
      (let [auth-header (get-in request [:headers "authorization"])
            [_user password] (decode-basic-auth auth-header)]
        (if (= password (:admin-password config))
          (handler request)
          {:status 401
           :headers {"WWW-Authenticate" "Basic realm=\"VetSoft Admin\""
                     "Content-Type" "application/json"}
           :body "{\"error\":\"Authentication required\"}"})))))
