(ns vetsoft.build
  (:require [clojure.java.io :as io]))

(defn html-template []
  (str
   "<!DOCTYPE html>
    <html class=\"scroll-smooth\">
    <head>
        <meta charset=\"UTF-8\">
        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
        <title>ВЕТСОФТ ВЕТЕРИНАР</title>
        
        <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">
        <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>
        <link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap\" rel=\"stylesheet\">
        
        <script src=\"https://unpkg.com/@phosphor-icons/web\"></script>
        <script src=\"https://cdn.tailwindcss.com\"></script>
        
        <script>
          tailwind.config = {
            theme: {
              extend: {
                fontFamily: {
                  sans: ['Inter', 'sans-serif'],
                },
                colors: {
                  primary: '#2563eb',
                  primaryDark: '#1e40af',
                  secondary: '#64748b',
                }
              }
            }
          }
        </script>
    </head>
    <body class=\"bg-white text-slate-800 antialiased font-sans\">
        <div id=\"app\"></div>
        <script src=\"js/main.js\"></script>
    </body>
    </html>"))

(defn generate-html
  {:shadow.build/stage :flush}
  [build-state]
  (let [out-file (io/file "public/index.html")]
    (io/make-parents out-file)
    (spit out-file (html-template))
    (println "index.html обновлен (Tailwind подключен)."))
  build-state)
