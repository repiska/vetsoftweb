(ns vetsoft.build
  (:require [clojure.java.io :as io]))

(defn html-template []
  (str
   "<!DOCTYPE html>
    <html class=\"scroll-smooth\">
    <head>
        <meta charset=\"UTF-8\">
        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
        <title>ВЕТСОФТ ВЕТЕРИНАР — Автоматизация ветклиник</title>
        <meta name=\"description\" content=\"ВЕТСОФТ — профессиональная автоматизация ветеринарных клиник. Лицензии, модули, техническая поддержка.\">
        <meta property=\"og:title\" content=\"ВЕТСОФТ ВЕТЕРИНАР — Автоматизация ветклиник\">
        <meta property=\"og:description\" content=\"Профессиональная автоматизация ветеринарных клиник. Лицензии, модули, техподдержка.\">
        <meta property=\"og:type\" content=\"website\">

        <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">
        <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>
        <link href=\"https://fonts.googleapis.com/css2?family=Manrope:wght@600;700;800&family=Inter:wght@300;400;500;600;700;800&subset=cyrillic&display=swap\" rel=\"stylesheet\">
        <link rel=\"stylesheet\" href=\"css/app.css\">

        <script src=\"https://unpkg.com/@phosphor-icons/web\"></script>
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
