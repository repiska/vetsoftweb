(ns vetsoft.admin.views
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :as resp]
            [vetsoft.db :as db]))

;; === HTML Helpers ===

(defn- html-response [body]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body body})

(defn- h [s]
  (when s
    (-> (str s)
        (clojure.string/replace "&" "&amp;")
        (clojure.string/replace "<" "&lt;")
        (clojure.string/replace ">" "&gt;")
        (clojure.string/replace "\"" "&quot;"))))

(defn- layout [title & content]
  (str "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
       "<title>" (h title) " — ВЕТСОФТ Админ</title>"
       "<link rel='preconnect' href='https://fonts.googleapis.com'>"
       "<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin='true'>"
       "<link href='https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&subset=cyrillic&display=swap' rel='stylesheet'>"
       "<script src='https://cdn.tailwindcss.com'></script>"
       "<style>body{font-family:'Inter',system-ui,sans-serif}</style>"
       "</head><body class='bg-slate-50 min-h-screen text-slate-800 antialiased'>"
       "<nav class='bg-white shadow-sm border-b border-slate-200'>"
       "<div class='max-w-6xl mx-auto px-4 flex items-center gap-5 h-14'>"
       "<a href='/admin/' class='font-extrabold text-lg text-blue-600 tracking-tight'>ВЕТСОФТ</a>"
       "<span class='text-slate-300'>|</span>"
       "<a href='/admin/licenses' class='text-sm text-slate-600 hover:text-blue-600'>Лицензии</a>"
       "<a href='/admin/modules' class='text-sm text-slate-600 hover:text-blue-600'>Модули</a>"
       "<a href='/admin/support' class='text-sm text-slate-600 hover:text-blue-600'>Поддержка</a>"
       "<a href='/admin/services' class='text-sm text-slate-600 hover:text-blue-600'>Услуги</a>"
       "<a href='/admin/extra-items' class='text-sm text-slate-600 hover:text-blue-600'>Доп. позиции</a>"
       "<a href='/admin/metadata' class='text-sm text-slate-600 hover:text-blue-600'>Метаданные</a>"
       "<a href='/' class='ml-auto text-sm text-slate-400 hover:text-blue-600'>← Сайт</a>"
       "</div></nav>"
       "<main class='max-w-6xl mx-auto px-4 py-8'>" (apply str content) "</main>"
       "</body></html>"))

;; === Form Components ===

(defn- input-field [label name type value & [{:keys [required placeholder]}]]
  (str "<div class='mb-4'>"
       "<label class='block text-sm font-medium text-slate-700 mb-1'>" (h label) "</label>"
       "<input type='" type "' name='" name "' value='" (h (str (or value ""))) "'"
       (when required " required")
       (when placeholder (str " placeholder='" (h placeholder) "'"))
       " class='w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500'>"
       "</div>"))

(defn- textarea-field [label name value]
  (str "<div class='mb-4'>"
       "<label class='block text-sm font-medium text-slate-700 mb-1'>" (h label) "</label>"
       "<textarea name='" name "' rows='3' class='w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500'>"
       (h (str (or value "")))
       "</textarea></div>"))

(defn- select-field [label name options selected]
  (str "<div class='mb-4'>"
       "<label class='block text-sm font-medium text-slate-700 mb-1'>" (h label) "</label>"
       "<select name='" name "' class='w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500'>"
       (apply str (map (fn [[val lbl]]
                         (str "<option value='" (h val) "'" (when (= val (str selected)) " selected") ">" (h lbl) "</option>"))
                       options))
       "</select></div>"))

(defn- checkbox-field [label name checked?]
  (str "<div class='mb-4 flex items-center gap-2'>"
       "<input type='hidden' name='" name "' value='false'>"
       "<input type='checkbox' name='" name "' value='true'" (when checked? " checked")
       " class='w-4 h-4 text-blue-600 rounded border-slate-300 focus:ring-blue-500'>"
       "<label class='text-sm font-medium text-slate-700'>" (h label) "</label>"
       "</div>"))

(defn- form-buttons [cancel-href]
  (str "<div class='flex gap-3 mt-6'>"
       "<button type='submit' class='px-5 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors'>Сохранить</button>"
       "<a href='" cancel-href "' class='px-5 py-2 bg-white text-slate-600 text-sm font-medium rounded-lg border border-slate-300 hover:bg-slate-50 transition-colors'>Отмена</a>"
       "</div>"))

;; === Generic Table with CRUD ===

(defn- crud-table [title headers rows entity-prefix new-href]
  (layout title
    (str "<div class='flex justify-between items-center mb-6'>"
         "<h1 class='text-2xl font-bold'>" (h title) "</h1>"
         "<a href='" new-href "' class='px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors'>+ Создать</a>"
         "</div>"
         "<div class='bg-white rounded-lg border border-slate-200 shadow-sm overflow-x-auto'>"
         "<table class='min-w-full text-sm'>"
         "<thead><tr class='bg-slate-50 border-b border-slate-200'>"
         (apply str (map #(str "<th class='px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider'>" (h %) "</th>") headers))
         "<th class='px-4 py-3 w-40 text-xs font-semibold text-slate-600 uppercase tracking-wider'>Действия</th>"
         "</tr></thead><tbody>"
         (if (empty? rows)
           (str "<tr><td colspan='" (+ (count headers) 1) "' class='px-4 py-8 text-center text-slate-400'>Нет данных</td></tr>")
           (apply str
             (map (fn [row]
                    (let [id (first row)]
                      (str "<tr class='border-t border-slate-100 hover:bg-blue-50/50 transition-colors'>"
                           (apply str (map (fn [v] (str "<td class='px-4 py-3 text-slate-700'>" (h (str v)) "</td>")) (rest row)))
                           "<td class='px-4 py-3 flex gap-2'>"
                           "<a href='" entity-prefix id "/edit' class='text-xs font-medium text-blue-600 hover:text-blue-800 px-2 py-1 rounded hover:bg-blue-50'>Ред.</a>"
                           "<form method='POST' action='" entity-prefix id "/delete' style='display:inline' onsubmit='return confirm(\"Удалить?\")'>"
                           "<button class='text-xs font-medium text-red-500 hover:text-red-700 px-2 py-1 rounded hover:bg-red-50'>Удалить</button>"
                           "</form></td></tr>")))
                  rows)))
         "</tbody></table></div>")))

;; === Dashboard ===

(defn- dashboard-page []
  (layout "Панель управления"
    "<h1 class='text-2xl font-bold mb-6'>Панель управления</h1>"
    "<div class='grid grid-cols-1 md:grid-cols-3 gap-4'>"
    "<a href='/admin/licenses' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm hover:shadow-md hover:border-blue-200 transition-all'><h2 class='font-bold text-blue-600 mb-1'>Лицензии</h2><p class='text-slate-500 text-sm'>Тарифы лицензий</p></a>"
    "<a href='/admin/modules' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm hover:shadow-md hover:border-blue-200 transition-all'><h2 class='font-bold text-blue-600 mb-1'>Модули</h2><p class='text-slate-500 text-sm'>Дополнительные модули</p></a>"
    "<a href='/admin/support' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm hover:shadow-md hover:border-blue-200 transition-all'><h2 class='font-bold text-blue-600 mb-1'>Поддержка</h2><p class='text-slate-500 text-sm'>Тарифы техподдержки</p></a>"
    "<a href='/admin/services' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm hover:shadow-md hover:border-blue-200 transition-all'><h2 class='font-bold text-blue-600 mb-1'>Услуги</h2><p class='text-slate-500 text-sm'>Услуги и настройка</p></a>"
    "<a href='/admin/extra-items' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm hover:shadow-md hover:border-blue-200 transition-all'><h2 class='font-bold text-blue-600 mb-1'>Доп. позиции</h2><p class='text-slate-500 text-sm'>Рабочие места, обновления</p></a>"
    "<a href='/admin/metadata' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm hover:shadow-md hover:border-blue-200 transition-all'><h2 class='font-bold text-blue-600 mb-1'>Метаданные</h2><p class='text-slate-500 text-sm'>Дата, сноски</p></a>"
    "</div>"))

;; === LICENSES ===

(defn- license-form [action data]
  (str "<form method='POST' action='" action "' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm max-w-2xl'>"
       (input-field "Название" "name" "text" (:name data) {:required true :placeholder "ВЕТСОФТ ВЕТЕРИНАР - ..."})
       (input-field "Версия / Подключения" "version" "text" (:version data) {:required true :placeholder "Сетевая (до 3-х подключений)"})
       (input-field "Цена (₽)" "price" "number" (:price data) {:required true})
       (select-field "Цвет бейджа" "badge_color" [["" "— Нет бейджа —"] ["cyan" "Голубой (ПОПУЛЯРНОЕ)"] ["emerald" "Зелёный (ХИТ)"] ["blue" "Синий"]] (:badge-color data))
       (input-field "Текст бейджа" "badge_text" "text" (:badge-text data) {:placeholder "ПОПУЛЯРНОЕ, ХИТ..."})
       (checkbox-field "Выделить строку" "highlight" (:highlight data))
       (input-field "Порядок сортировки" "sort_order" "number" (or (:sort-order data) 0))
       (form-buttons "/admin/licenses")
       "</form>"))

(defn- licenses-list []
  (let [rows (db/get-licenses)]
    (crud-table "Лицензии"
      ["Название" "Версия" "Цена (₽)" "Бейдж" "Выделение" "Порядок"]
      (map (fn [r] [(:id r) (:name r) (:version r) (:price r) (or (:badge-text r) "—") (if (:highlight r) "Да" "Нет") (:sort-order r)]) rows)
      "/admin/licenses/" "/admin/licenses/new")))

(defn- license-new-page []
  (layout "Новая лицензия"
    "<h1 class='text-2xl font-bold mb-6'>Новая лицензия</h1>"
    (license-form "/admin/licenses" nil)))

(defn- license-edit-page [id]
  (let [item (db/query-one ["SELECT * FROM license_tiers WHERE id = ?" (Integer/parseInt id)])]
    (layout "Редактирование лицензии"
      "<h1 class='text-2xl font-bold mb-6'>Редактирование лицензии</h1>"
      (license-form (str "/admin/licenses/" id "/update") item))))

(defn- handle-license-create [params]
  (db/execute! ["INSERT INTO license_tiers (name, version, price, badge_text, badge_color, highlight, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?)"
                (get params "name") (get params "version")
                (Integer/parseInt (get params "price"))
                (let [v (get params "badge_text")] (when-not (clojure.string/blank? v) v))
                (let [v (get params "badge_color")] (when-not (clojure.string/blank? v) v))
                (= "true" (get params "highlight"))
                (Integer/parseInt (or (get params "sort_order") "0"))])
  (resp/redirect "/admin/licenses"))

(defn- handle-license-update [id params]
  (db/execute! ["UPDATE license_tiers SET name=?, version=?, price=?, badge_text=?, badge_color=?, highlight=?, sort_order=?, updated_at=NOW() WHERE id=?"
                (get params "name") (get params "version")
                (Integer/parseInt (get params "price"))
                (let [v (get params "badge_text")] (when-not (clojure.string/blank? v) v))
                (let [v (get params "badge_color")] (when-not (clojure.string/blank? v) v))
                (= "true" (get params "highlight"))
                (Integer/parseInt (or (get params "sort_order") "0"))
                (Integer/parseInt id)])
  (resp/redirect "/admin/licenses"))

;; === MODULES ===

(defn- module-form [action data]
  (str "<form method='POST' action='" action "' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm max-w-2xl'>"
       (input-field "Название" "name" "text" (:name data) {:required true})
       (input-field "Примечание" "note" "text" (:note data) {:placeholder "Сетевая версия"})
       (input-field "Цена (₽/мес)" "price" "number" (:price data) {:required true})
       (input-field "Порядок" "sort_order" "number" (or (:sort-order data) 0))
       (form-buttons "/admin/modules")
       "</form>"))

(defn- modules-list []
  (let [rows (db/get-modules)]
    (crud-table "Модули"
      ["Название" "Примечание" "Цена (₽/мес)" "Порядок"]
      (map (fn [r] [(:id r) (:name r) (or (:note r) "—") (:price r) (:sort-order r)]) rows)
      "/admin/modules/" "/admin/modules/new")))

(defn- module-new-page []
  (layout "Новый модуль" "<h1 class='text-2xl font-bold mb-6'>Новый модуль</h1>" (module-form "/admin/modules" nil)))

(defn- module-edit-page [id]
  (let [item (db/query-one ["SELECT * FROM modules WHERE id = ?" (Integer/parseInt id)])]
    (layout "Редактирование модуля" "<h1 class='text-2xl font-bold mb-6'>Редактирование модуля</h1>" (module-form (str "/admin/modules/" id "/update") item))))

(defn- handle-module-create [params]
  (db/execute! ["INSERT INTO modules (name, note, price, sort_order) VALUES (?, ?, ?, ?)"
                (get params "name") (let [v (get params "note")] (when-not (clojure.string/blank? v) v))
                (Integer/parseInt (get params "price")) (Integer/parseInt (or (get params "sort_order") "0"))])
  (resp/redirect "/admin/modules"))

(defn- handle-module-update [id params]
  (db/execute! ["UPDATE modules SET name=?, note=?, price=?, sort_order=?, updated_at=NOW() WHERE id=?"
                (get params "name") (let [v (get params "note")] (when-not (clojure.string/blank? v) v))
                (Integer/parseInt (get params "price")) (Integer/parseInt (or (get params "sort_order") "0")) (Integer/parseInt id)])
  (resp/redirect "/admin/modules"))

;; === SUPPORT RATES ===

(defn- support-form [action data]
  (str "<form method='POST' action='" action "' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm max-w-2xl'>"
       (input-field "Диапазон лицензий" "license_range" "text" (:license-range data) {:required true :placeholder "от 1 до 5 включительно"})
       (input-field "Цена (₽/мес за лицензию)" "price" "number" (:price data) {:required true})
       (input-field "Порядок" "sort_order" "number" (or (:sort-order data) 0))
       (form-buttons "/admin/support")
       "</form>"))

(defn- support-list []
  (let [rows (db/get-support-rates)]
    (crud-table "Тарифы поддержки"
      ["Диапазон лицензий" "Цена (₽/мес)" "Порядок"]
      (map (fn [r] [(:id r) (:license-range r) (:price r) (:sort-order r)]) rows)
      "/admin/support/" "/admin/support/new")))

(defn- support-new-page []
  (layout "Новый тариф" "<h1 class='text-2xl font-bold mb-6'>Новый тариф поддержки</h1>" (support-form "/admin/support" nil)))

(defn- support-edit-page [id]
  (let [item (db/query-one ["SELECT * FROM support_rates WHERE id = ?" (Integer/parseInt id)])]
    (layout "Редактирование тарифа" "<h1 class='text-2xl font-bold mb-6'>Редактирование тарифа</h1>" (support-form (str "/admin/support/" id "/update") item))))

(defn- handle-support-create [params]
  (db/execute! ["INSERT INTO support_rates (license_range, price, sort_order) VALUES (?, ?, ?)"
                (get params "license_range") (Integer/parseInt (get params "price")) (Integer/parseInt (or (get params "sort_order") "0"))])
  (resp/redirect "/admin/support"))

(defn- handle-support-update [id params]
  (db/execute! ["UPDATE support_rates SET license_range=?, price=?, sort_order=?, updated_at=NOW() WHERE id=?"
                (get params "license_range") (Integer/parseInt (get params "price")) (Integer/parseInt (or (get params "sort_order") "0")) (Integer/parseInt id)])
  (resp/redirect "/admin/support"))

;; === SERVICES ===

(defn- service-form [action data]
  (str "<form method='POST' action='" action "' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm max-w-2xl'>"
       (input-field "Название" "name" "text" (:name data) {:required true})
       (input-field "Цена" "price" "text" (:price data) {:required true :placeholder "5 000 ₽ или договорная"})
       (checkbox-field "На всю ширину (для последнего элемента)" "full_width" (:full-width data))
       (input-field "Порядок" "sort_order" "number" (or (:sort-order data) 0))
       (form-buttons "/admin/services")
       "</form>"))

(defn- services-list []
  (let [rows (db/get-services)]
    (crud-table "Услуги"
      ["Название" "Цена" "Ширина" "Порядок"]
      (map (fn [r] [(:id r) (:name r) (:price r) (if (:full-width r) "Полная" "—") (:sort-order r)]) rows)
      "/admin/services/" "/admin/services/new")))

(defn- service-new-page []
  (layout "Новая услуга" "<h1 class='text-2xl font-bold mb-6'>Новая услуга</h1>" (service-form "/admin/services" nil)))

(defn- service-edit-page [id]
  (let [item (db/query-one ["SELECT * FROM services WHERE id = ?" (Integer/parseInt id)])]
    (layout "Редактирование услуги" "<h1 class='text-2xl font-bold mb-6'>Редактирование услуги</h1>" (service-form (str "/admin/services/" id "/update") item))))

(defn- handle-service-create [params]
  (db/execute! ["INSERT INTO services (name, price, full_width, sort_order) VALUES (?, ?, ?, ?)"
                (get params "name") (get params "price") (= "true" (get params "full_width"))
                (Integer/parseInt (or (get params "sort_order") "0"))])
  (resp/redirect "/admin/services"))

(defn- handle-service-update [id params]
  (db/execute! ["UPDATE services SET name=?, price=?, full_width=?, sort_order=?, updated_at=NOW() WHERE id=?"
                (get params "name") (get params "price") (= "true" (get params "full_width"))
                (Integer/parseInt (or (get params "sort_order") "0")) (Integer/parseInt id)])
  (resp/redirect "/admin/services"))

;; === EXTRA ITEMS ===

(defn- extra-item-form [action data]
  (str "<form method='POST' action='" action "' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm max-w-2xl'>"
       (select-field "Категория" "category" [["workstation" "Рабочее место"] ["upgrade" "Обновление"]] (:category data))
       (input-field "Название" "name" "text" (:name data) {:required true})
       (input-field "Описание" "description" "text" (:description data) {:placeholder "1 шт."})
       (input-field "Цена (₽)" "price" "number" (:price data) {:required true})
       (input-field "Порядок" "sort_order" "number" (or (:sort-order data) 0))
       (form-buttons "/admin/extra-items")
       "</form>"))

(defn- extra-items-list []
  (let [rows (db/get-extra-items)]
    (crud-table "Дополнительные позиции"
      ["Категория" "Название" "Описание" "Цена (₽)" "Порядок"]
      (map (fn [r] [(:id r) (:category r) (:name r) (or (:description r) "—") (:price r) (:sort-order r)]) rows)
      "/admin/extra-items/" "/admin/extra-items/new")))

(defn- extra-item-new-page []
  (layout "Новая позиция" "<h1 class='text-2xl font-bold mb-6'>Новая доп. позиция</h1>" (extra-item-form "/admin/extra-items" nil)))

(defn- extra-item-edit-page [id]
  (let [item (db/query-one ["SELECT * FROM extra_items WHERE id = ?" (Integer/parseInt id)])]
    (layout "Редактирование позиции" "<h1 class='text-2xl font-bold mb-6'>Редактирование доп. позиции</h1>" (extra-item-form (str "/admin/extra-items/" id "/update") item))))

(defn- handle-extra-item-create [params]
  (db/execute! ["INSERT INTO extra_items (category, name, description, price, sort_order) VALUES (?, ?, ?, ?, ?)"
                (get params "category") (get params "name")
                (let [v (get params "description")] (when-not (clojure.string/blank? v) v))
                (Integer/parseInt (get params "price")) (Integer/parseInt (or (get params "sort_order") "0"))])
  (resp/redirect "/admin/extra-items"))

(defn- handle-extra-item-update [id params]
  (db/execute! ["UPDATE extra_items SET category=?, name=?, description=?, price=?, sort_order=?, updated_at=NOW() WHERE id=?"
                (get params "category") (get params "name")
                (let [v (get params "description")] (when-not (clojure.string/blank? v) v))
                (Integer/parseInt (get params "price")) (Integer/parseInt (or (get params "sort_order") "0")) (Integer/parseInt id)])
  (resp/redirect "/admin/extra-items"))

;; === METADATA ===

(defn- metadata-list []
  (let [rows (db/query ["SELECT * FROM price_metadata ORDER BY key"])]
    (layout "Метаданные"
      (str "<div class='flex justify-between items-center mb-6'>"
           "<h1 class='text-2xl font-bold'>Метаданные прайса</h1>"
           "<a href='/admin/metadata/new' class='px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700'>+ Создать</a>"
           "</div>"
           "<div class='bg-white rounded-lg border border-slate-200 shadow-sm overflow-x-auto'>"
           "<table class='min-w-full text-sm'><thead><tr class='bg-slate-50 border-b border-slate-200'>"
           "<th class='px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase'>Ключ</th>"
           "<th class='px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase'>Значение</th>"
           "<th class='px-4 py-3 w-40 text-xs font-semibold text-slate-600 uppercase'>Действия</th>"
           "</tr></thead><tbody>"
           (if (empty? rows)
             "<tr><td colspan='3' class='px-4 py-8 text-center text-slate-400'>Нет данных</td></tr>"
             (apply str (map (fn [r]
                               (str "<tr class='border-t border-slate-100 hover:bg-blue-50/50'>"
                                    "<td class='px-4 py-3 font-mono text-xs text-slate-600'>" (h (:key r)) "</td>"
                                    "<td class='px-4 py-3 text-slate-700'>" (h (:value r)) "</td>"
                                    "<td class='px-4 py-3 flex gap-2'>"
                                    "<a href='/admin/metadata/" (:id r) "/edit' class='text-xs font-medium text-blue-600 hover:text-blue-800 px-2 py-1 rounded hover:bg-blue-50'>Ред.</a>"
                                    "<form method='POST' action='/admin/metadata/" (:id r) "/delete' style='display:inline' onsubmit='return confirm(\"Удалить?\")'>"
                                    "<button class='text-xs font-medium text-red-500 hover:text-red-700 px-2 py-1 rounded hover:bg-red-50'>Удалить</button></form>"
                                    "</td></tr>"))
                             rows)))
           "</tbody></table></div>"))))

(defn- metadata-form [action data]
  (str "<form method='POST' action='" action "' class='bg-white p-6 rounded-lg border border-slate-200 shadow-sm max-w-2xl'>"
       (input-field "Ключ" "key" "text" (:key data) {:required true :placeholder "valid_date"})
       (textarea-field "Значение" "value" (:value data))
       (form-buttons "/admin/metadata")
       "</form>"))

(defn- metadata-new-page []
  (layout "Новая запись" "<h1 class='text-2xl font-bold mb-6'>Новая метаданная</h1>" (metadata-form "/admin/metadata" nil)))

(defn- metadata-edit-page [id]
  (let [item (db/query-one ["SELECT * FROM price_metadata WHERE id = ?" (Integer/parseInt id)])]
    (layout "Редактирование" "<h1 class='text-2xl font-bold mb-6'>Редактирование метаданных</h1>" (metadata-form (str "/admin/metadata/" id "/update") item))))

(defn- handle-metadata-create [params]
  (db/execute! ["INSERT INTO price_metadata (key, value) VALUES (?, ?) ON CONFLICT (key) DO UPDATE SET value = ?"
                (get params "key") (get params "value") (get params "value")])
  (resp/redirect "/admin/metadata"))

(defn- handle-metadata-update [id params]
  (db/execute! ["UPDATE price_metadata SET key=?, value=? WHERE id=?"
                (get params "key") (get params "value") (Integer/parseInt id)])
  (resp/redirect "/admin/metadata"))

;; === Delete handler ===

(defn- handle-delete [table id redirect]
  (db/execute! [(str "DELETE FROM " table " WHERE id = ?") (Integer/parseInt id)])
  (resp/redirect redirect))

;; === Routes ===

(defroutes admin-view-routes
  ;; Dashboard
  (GET "/admin/" [] (html-response (dashboard-page)))

  ;; Licenses
  (GET "/admin/licenses" [] (html-response (licenses-list)))
  (GET "/admin/licenses/new" [] (html-response (license-new-page)))
  (GET "/admin/licenses/:id/edit" [id] (html-response (license-edit-page id)))
  (POST "/admin/licenses" {params :params} (handle-license-create params))
  (POST "/admin/licenses/:id/update" [id :as {params :params}] (handle-license-update id params))
  (POST "/admin/licenses/:id/delete" [id] (handle-delete "license_tiers" id "/admin/licenses"))

  ;; Modules
  (GET "/admin/modules" [] (html-response (modules-list)))
  (GET "/admin/modules/new" [] (html-response (module-new-page)))
  (GET "/admin/modules/:id/edit" [id] (html-response (module-edit-page id)))
  (POST "/admin/modules" {params :params} (handle-module-create params))
  (POST "/admin/modules/:id/update" [id :as {params :params}] (handle-module-update id params))
  (POST "/admin/modules/:id/delete" [id] (handle-delete "modules" id "/admin/modules"))

  ;; Support
  (GET "/admin/support" [] (html-response (support-list)))
  (GET "/admin/support/new" [] (html-response (support-new-page)))
  (GET "/admin/support/:id/edit" [id] (html-response (support-edit-page id)))
  (POST "/admin/support" {params :params} (handle-support-create params))
  (POST "/admin/support/:id/update" [id :as {params :params}] (handle-support-update id params))
  (POST "/admin/support/:id/delete" [id] (handle-delete "support_rates" id "/admin/support"))

  ;; Services
  (GET "/admin/services" [] (html-response (services-list)))
  (GET "/admin/services/new" [] (html-response (service-new-page)))
  (GET "/admin/services/:id/edit" [id] (html-response (service-edit-page id)))
  (POST "/admin/services" {params :params} (handle-service-create params))
  (POST "/admin/services/:id/update" [id :as {params :params}] (handle-service-update id params))
  (POST "/admin/services/:id/delete" [id] (handle-delete "services" id "/admin/services"))

  ;; Extra items
  (GET "/admin/extra-items" [] (html-response (extra-items-list)))
  (GET "/admin/extra-items/new" [] (html-response (extra-item-new-page)))
  (GET "/admin/extra-items/:id/edit" [id] (html-response (extra-item-edit-page id)))
  (POST "/admin/extra-items" {params :params} (handle-extra-item-create params))
  (POST "/admin/extra-items/:id/update" [id :as {params :params}] (handle-extra-item-update id params))
  (POST "/admin/extra-items/:id/delete" [id] (handle-delete "extra_items" id "/admin/extra-items"))

  ;; Metadata
  (GET "/admin/metadata" [] (html-response (metadata-list)))
  (GET "/admin/metadata/new" [] (html-response (metadata-new-page)))
  (GET "/admin/metadata/:id/edit" [id] (html-response (metadata-edit-page id)))
  (POST "/admin/metadata" {params :params} (handle-metadata-create params))
  (POST "/admin/metadata/:id/update" [id :as {params :params}] (handle-metadata-update id params))
  (POST "/admin/metadata/:id/delete" [id] (handle-delete "price_metadata" id "/admin/metadata")))
