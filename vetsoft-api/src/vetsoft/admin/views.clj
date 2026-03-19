(ns vetsoft.admin.views
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :as resp]
            [vetsoft.db :as db]))

;; === Hiccup Helpers ===

(defn- html-response [body]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body body})

(defn- h [s] (when s (-> s (clojure.string/replace "&" "&amp;") (clojure.string/replace "<" "&lt;") (clojure.string/replace ">" "&gt;") (clojure.string/replace "\"" "&quot;"))))

(defn- layout [title & content]
  (str "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
       "<title>" (h title) " — ВЕТСОФТ Администрирование</title>"
       "<link rel='preconnect' href='https://fonts.googleapis.com'>"
       "<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin='true'>"
       "<link href='https://fonts.googleapis.com/css2?family=Manrope:wght@600;700;800&family=Inter:wght@300;400;500;600;700&subset=cyrillic&display=swap' rel='stylesheet'>"
       "<script src='https://cdn.tailwindcss.com'></script>"
       "<style>body{font-family:'Inter',ui-sans-serif,system-ui,sans-serif}.font-display{font-family:'Manrope','Inter',ui-sans-serif,system-ui,sans-serif}</style>"
       "</head>"
       "<body class='bg-slate-50 min-h-screen text-slate-800 antialiased'>"
       "<nav class='bg-white shadow-sm border-b border-slate-200'>"
       "<div class='max-w-6xl mx-auto px-4 py-0 flex items-center gap-6 h-14'>"
       "<a href='/admin/' class='font-display font-extrabold text-lg text-blue-600 tracking-tight mr-2'>ВЕТСОФТ</a>"
       "<span class='text-slate-300 text-lg select-none'>|</span>"
       "<a href='/admin/licenses' class='text-sm text-slate-600 hover:text-blue-600 transition-colors'>Лицензии</a>"
       "<a href='/admin/modules' class='text-sm text-slate-600 hover:text-blue-600 transition-colors'>Модули</a>"
       "<a href='/admin/support' class='text-sm text-slate-600 hover:text-blue-600 transition-colors'>Поддержка</a>"
       "<a href='/admin/services' class='text-sm text-slate-600 hover:text-blue-600 transition-colors'>Услуги</a>"
       "<a href='/admin/extra-items' class='text-sm text-slate-600 hover:text-blue-600 transition-colors'>Доп. позиции</a>"
       "<a href='/admin/metadata' class='text-sm text-slate-600 hover:text-blue-600 transition-colors'>Метаданные</a>"
       "<a href='/' class='ml-auto text-sm text-slate-400 hover:text-blue-600 transition-colors'>← На сайт</a>"
       "</div></nav>"
       "<main class='max-w-6xl mx-auto px-4 py-8'>" (apply str content) "</main>"
       "</body></html>"))

(defn- table-page [title headers rows edit-prefix delete-prefix]
  (layout title
    (str "<div class='flex justify-between items-center mb-6'>"
         "<h1 class='text-2xl font-bold text-slate-900'>" (h title) "</h1>"
         "</div>"
         "<div class='bg-white rounded-lg border border-slate-200 shadow-sm overflow-x-auto'>"
         "<table class='min-w-full text-sm'>"
         "<thead><tr class='bg-slate-50 border-b border-slate-200'>"
         (apply str (map #(str "<th class='px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider'>" (h %) "</th>") headers))
         "<th class='px-4 py-3 w-24 text-xs font-semibold text-slate-600 uppercase tracking-wider'>Действия</th>"
         "</tr></thead><tbody>"
         (apply str
           (map (fn [row]
                  (str "<tr class='border-t border-slate-100 hover:bg-blue-50 transition-colors'>"
                       (apply str (map (fn [v] (str "<td class='px-4 py-3 text-slate-700'>" (h (str v)) "</td>")) (rest row)))
                       "<td class='px-4 py-3'>"
                       "<form method='POST' action='" delete-prefix (first row) "/delete' style='display:inline' onsubmit='return confirm(\"Удалить запись?\")'>"
                       "<button class='text-xs font-medium text-red-500 hover:text-red-700 hover:bg-red-50 px-2 py-1 rounded-lg transition-colors'>Удалить</button>"
                       "</form>"
                       "</td></tr>"))
                rows))
         "</tbody></table></div>")))

;; === Dashboard ===

(defn- dashboard-card [href title desc]
  (str "<a href='" href "' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm hover:shadow-md hover:border-blue-200 transition-all'>"
       "<h2 class='font-bold text-base text-blue-600 mb-1'>" title "</h2>"
       "<p class='text-slate-500 text-sm'>" desc "</p>"
       "</a>"))

(defn- dashboard-page []
  (layout "Панель управления"
    "<h1 class='text-2xl font-bold text-slate-900 mb-6'>Панель управления</h1>"
    "<div class='grid grid-cols-1 md:grid-cols-3 gap-4'>"
    (dashboard-card "/admin/licenses" "Лицензии" "Управление тарифами лицензий")
    (dashboard-card "/admin/modules" "Модули" "Дополнительные модули")
    (dashboard-card "/admin/support" "Поддержка" "Тарифы техподдержки")
    (dashboard-card "/admin/services" "Услуги" "Услуги и настройка")
    (dashboard-card "/admin/extra-items" "Доп. позиции" "Рабочие места, обновления")
    (dashboard-card "/admin/metadata" "Метаданные" "Дата актуальности, сноски")
    "</div>"))

;; === Page Handlers ===

(defn- licenses-page []
  (let [rows (db/get-licenses)]
    (table-page "Лицензии"
      ["Название" "Версия" "Цена (₽)" "Бейдж" "Выделение" "Порядок"]
      (map (fn [r] [(:id r) (:name r) (:version r) (:price r) (or (:badge-text r) "—") (if (:highlight r) "Да" "Нет") (:sort-order r)]) rows)
      "/admin/licenses/" "/admin/licenses/")))

(defn- modules-page []
  (let [rows (db/get-modules)]
    (table-page "Модули"
      ["Название" "Примечание" "Цена (₽/мес)" "Порядок"]
      (map (fn [r] [(:id r) (:name r) (or (:note r) "—") (:price r) (:sort-order r)]) rows)
      "/admin/modules/" "/admin/modules/")))

(defn- support-page []
  (let [rows (db/get-support-rates)]
    (table-page "Тарифы поддержки"
      ["Диапазон лицензий" "Цена (₽/мес)" "Порядок"]
      (map (fn [r] [(:id r) (:license-range r) (:price r) (:sort-order r)]) rows)
      "/admin/support/" "/admin/support/")))

(defn- services-page []
  (let [rows (db/get-services)]
    (table-page "Услуги"
      ["Название" "Цена" "На всю ширину" "Порядок"]
      (map (fn [r] [(:id r) (:name r) (:price r) (if (:full-width r) "Да" "Нет") (:sort-order r)]) rows)
      "/admin/services/" "/admin/services/")))

(defn- extra-items-page []
  (let [rows (db/get-extra-items)]
    (table-page "Дополнительные позиции"
      ["Категория" "Название" "Описание" "Цена (₽)" "Порядок"]
      (map (fn [r] [(:id r) (:category r) (:name r) (or (:description r) "—") (:price r) (:sort-order r)]) rows)
      "/admin/extra-items/" "/admin/extra-items/")))

(defn- metadata-page []
  (let [meta-map (db/get-metadata)]
    (layout "Метаданные"
      "<h1 class='text-2xl font-bold text-slate-900 mb-6'>Метаданные прайса</h1>"
      "<div class='bg-white rounded-lg border border-slate-200 shadow-sm overflow-x-auto'>"
      "<table class='min-w-full text-sm'>"
      "<thead><tr class='bg-slate-50 border-b border-slate-200'>"
      "<th class='px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider'>Ключ</th>"
      "<th class='px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider'>Значение</th>"
      "</tr></thead><tbody>"
      (apply str (map (fn [[k v]]
                        (str "<tr class='border-t border-slate-100 hover:bg-blue-50 transition-colors'>"
                             "<td class='px-4 py-3 font-mono text-slate-600 text-xs'>" (h k) "</td>"
                             "<td class='px-4 py-3 text-slate-700'>" (h v) "</td>"
                             "</tr>"))
                      meta-map))
      "</tbody></table></div>")))

;; === Delete handlers ===

(defn- handle-delete [table id redirect]
  (db/execute! [(str "DELETE FROM " table " WHERE id = ?") (Integer/parseInt id)])
  (resp/redirect redirect))

;; === Routes ===

(defroutes admin-view-routes
  (GET "/admin/" [] (html-response (dashboard-page)))
  (GET "/admin/licenses" [] (html-response (licenses-page)))
  (GET "/admin/modules" [] (html-response (modules-page)))
  (GET "/admin/support" [] (html-response (support-page)))
  (GET "/admin/services" [] (html-response (services-page)))
  (GET "/admin/extra-items" [] (html-response (extra-items-page)))
  (GET "/admin/metadata" [] (html-response (metadata-page)))

  ;; Delete actions
  (POST "/admin/licenses/:id/delete" [id] (handle-delete "license_tiers" id "/admin/licenses"))
  (POST "/admin/modules/:id/delete" [id] (handle-delete "modules" id "/admin/modules"))
  (POST "/admin/support/:id/delete" [id] (handle-delete "support_rates" id "/admin/support"))
  (POST "/admin/services/:id/delete" [id] (handle-delete "services" id "/admin/services"))
  (POST "/admin/extra-items/:id/delete" [id] (handle-delete "extra_items" id "/admin/extra-items")))
