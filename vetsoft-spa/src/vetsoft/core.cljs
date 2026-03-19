(ns vetsoft.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [vetsoft.ui :as ui]))

;; === 1. ДАННЫЕ (FALLBACK) ===
;; Используются если API недоступен

(def hero-data
  {:title "Автоматизация "
   :title-gradient "ветеринарных клиник"
   :subtitle "Полный контроль клиники — от приёма до склада. Лицензия приобретается один раз и навсегда."
   :social-proof ["20 лет на рынке" "500+ клиник" "Бесплатные обновления"]
   :btn-primary {:text "Получить консультацию" :href "#contacts"}
   :btn-outline {:text "Прайс-лист"}})

(def contacts-data
  {:title "Контакты"
   :address "г. Воронеж, ул. Пушкина, 10"
   :phones ["+7 (473) 229-73-79 (Воронеж)"
            "+7 (495) 204-29-73 (Москва)"]
   :email "info@vetsoft.ru"})

(def modules-data
  [{:title "Клинический блок и Склад" :icon "first-aid-kit"
    :subtitle "Всё для ежедневной работы врача и учёта медикаментов"
    :items ["Ведение приёмов с готовыми шаблонами"
            "Электронная карта пациента"
            "Автоматизация документооборота"
            "Гибкий конструктор бланков"
            "Складской учёт в реальном времени"
            "Контроль остатков и сроков годности"]}
   {:title "Работа с клиентами и Касса" :icon "users"
    :subtitle "Удобная регистратура, история визитов и интеграция с кассой"
    :items ["Быстрая запись на приём"
            "Полная история визитов клиента"
            "Интеграция с телефонией (Call-центр)"
            "Кассовые операции и чеки"
            "Учёт оказанных услуг"
            "Контроль оплат и задолженностей"]}
   {:title "Управление и Аналитика" :icon "chart-bar"
    :subtitle "Полный контроль над бизнес-процессами клиники"
    :items ["Гибкое администрирование прав доступа"
            "Детальные отчёты по выручке и нагрузке"
            "Учёт рабочего времени сотрудников"
            "Система внутренних задач"
            "Быстрый поиск по всей базе"]}])

;; Fallback pricing data (used when API unavailable)
(def fallback-licenses
  [{:name "ВЕТСОФТ ВЕТЕРИНАР - НОРМАЛ" :version "Локальная (1 место)" :price 38200}
   {:name "ВЕТСОФТ ВЕТЕРИНАР - МАЛЫЙ БИЗНЕС"
    :version "Сетевая (до 3-х подключений)"
    :price 49800
    :badge-text "ПОПУЛЯРНОЕ" :badge-color "cyan"
    :highlight true}
   {:name "ВЕТСОФТ ВЕТЕРИНАР - УСПЕШНЫЙ"
    :version "Сетевая (до 5-и подключений)"
    :price 84900
    :badge-text "ХИТ" :badge-color "emerald"
    :highlight true}
   {:name "ВЕТСОФТ ВЕТЕРИНАР - СУПЕР" :version "Сетевая (до 10-и подключений)" :price 195000}])

(def fallback-extra-items
  [{:name "Дополнительное рабочее место" :description "1 шт." :price 19500}
   {:name "Обновление старых версий" :description "До актуальной версии" :price 48000}])

(def fallback-modules
  [{:name "Модуль расчета заработной платы" :note "Сетевая версия (для сети клиник)" :price 32700}
   {:name "Модуль «Стационар»" :note "Для одной клиники" :price 42000}
   {:name "Модуль учета движения средств" :note "Сетевая версия" :price 35000}])

(def fallback-support
  [{:license-range "от 1-й до 5-ти включительно" :price 1250}
   {:license-range "свыше 5-ти до 10-ти" :price 1100}
   {:license-range "свыше 10-ти до 20-ти" :price 1000}
   {:license-range "свыше 20-ти до 30-ти" :price 900}
   {:license-range "свыше 30-ти" :price 800}])

(def fallback-services
  [{:name "Удаленная настройка 1 рабочего места" :price "5 000 ₽"}
   {:name "Настройка конфигурации (мин. 2 филиала)" :price "12 500 ₽/ филиал"}
   {:name "Создание отчета" :price "от 5 000 ₽/шт"}
   {:name "Разработка бланка (до 30 полей)" :price "от 3 000 ₽/шт"}
   {:name "Сборка печатной формы" :price "от 3 000 ₽/шт"}
   {:name "Вызов специалиста (только Воронеж)" :price "3 000 ₽"}
   {:name "Работы в Москве / Санкт-Петербурге" :price "договорная" :full-width true}])

(def fallback-metadata
  {"valid_date" "19.03.2026"
   "archive_note" "Для клиентов с договорами АБ до 31.12.2023 действуют архивные тарифы: от 6050 до 15000 руб./мес. за объект."})

(def fallback-prices
  {:licenses fallback-licenses
   :extra_items fallback-extra-items
   :modules fallback-modules
   :support fallback-support
   :services fallback-services
   :metadata fallback-metadata})

(def footer-data
  {:company "ВЕТСОФТ"
   :copyright "© 2006-2026 Все права защищены."
   :nav-links [{:label "Главная" :section "home"}
               {:label "Модули" :section "modules"}
               {:label "Цены" :section "prices"}
               {:label "Контакты" :section "contacts"}]
   :phones ["+7 (473) 229-73-79" "+7 (495) 204-29-73"]
   :email "info@vetsoft.ru"})

;; === 2. APP STATE ===

(def app-state (r/atom {:loading? true :error nil :prices nil}))

(defonce active-section (r/atom "home"))

;; === 2.1. ФУНКЦИЯ ПРОКРУТКИ ===

(defn scroll-to [id]
  (when-let [el (.getElementById js/document id)]
    (.scrollIntoView el #js {:behavior "smooth"})))

;; Accordion states (controlled from PillNav)
(defonce accordion-states
  {:licenses  (r/atom true)
   :modules   (r/atom false)
   :support   (r/atom false)
   :services  (r/atom false)})

(defn open-accordion!
  "Open a specific accordion and close others, then scroll to prices."
  [key]
  (doseq [[k v] accordion-states]
    (reset! v (= k key)))
  (js/setTimeout #(scroll-to "prices") 100))

;; === 3. API FETCH ===

(defn- js->clj-keys
  "Convert JS object to ClojureScript with keyword keys."
  [obj]
  (js->clj obj :keywordize-keys true))

(defn- api-available?
  "Check if we're running with a backend (not static GitHub Pages)."
  []
  (not (re-find #"github\.io" (.. js/window -location -hostname))))

(defn fetch-prices!
  "Fetch pricing data from API and update app-state.
   On static hosting (GitHub Pages) skips API call and uses fallback."
  []
  (if (api-available?)
    (do
      (reset! app-state {:loading? true :error nil :prices nil})
      (-> (js/fetch "/api/prices")
          (.then (fn [response]
                   (if (.-ok response)
                     (.json response)
                     (throw (js/Error. (str "HTTP " (.-status response)))))))
          (.then (fn [data]
                   (let [prices (js->clj-keys data)]
                     (reset! app-state {:loading? false :error nil :prices prices}))))
          (.catch (fn [error]
                    (js/console.warn "API unavailable, using fallback data:" (.-message error))
                    (reset! app-state {:loading? false :error nil :prices fallback-prices})))))
    ;; Static hosting — use fallback immediately
    (reset! app-state {:loading? false :error nil :prices fallback-prices})))

;; === 3.1. SSE (Server-Sent Events) ===

(defonce sse-connection (atom nil))

(defn connect-sse!
  "Connect to SSE stream for real-time price updates.
   Only connects when backend API is available (not on static hosting)."
  []
  (when (api-available?)
    (when-let [old @sse-connection]
      (.close old))
    (let [es (js/EventSource. "/api/prices/stream")]
      (set! (.-onmessage es)
            (fn [event]
              (try
                (let [prices (js->clj-keys (.parse js/JSON (.-data event)))]
                  (reset! app-state {:loading? false :error nil :prices prices}))
                (catch :default e
                  (js/console.warn "SSE parse error:" e)))))
      (set! (.-onerror es)
            (fn [_]
              (js/console.warn "SSE connection lost, reconnecting in 5s...")
              (.close es)
              (js/setTimeout connect-sse! 5000)))
      (reset! sse-connection es))))

;; === 4. SCROLL SPY ===

(defonce scroll-observer (atom nil))

(defonce reveal-observer (atom nil))

(defn setup-scroll-spy!
  "Install IntersectionObservers: one for active-section tracking,
   one for section-reveal animations."
  []
  ;; Scroll-spy for navbar
  (when-let [old @scroll-observer]
    (.disconnect old))
  (let [sections ["home" "modules" "prices" "contacts"]
        observer (js/IntersectionObserver.
                  (fn [entries]
                    (doseq [entry (array-seq entries)]
                      (when (.-isIntersecting entry)
                        (reset! active-section (.. entry -target -id)))))
                  #js {:threshold 0.3})]
    (doseq [id sections]
      (when-let [el (.getElementById js/document id)]
        (.observe observer el)))
    (reset! scroll-observer observer))
  ;; Section reveal animations
  (when-let [old @reveal-observer]
    (.disconnect old))
  (let [observer (js/IntersectionObserver.
                  (fn [entries]
                    (doseq [entry (array-seq entries)]
                      (when (.-isIntersecting entry)
                        (.add (.-classList (.-target entry)) "section-visible"))))
                  #js {:threshold 0.15 :rootMargin "0px 0px -50px 0px"})]
    (doseq [el (array-seq (.querySelectorAll js/document ".section-reveal"))]
      (.observe observer el))
    (reset! reveal-observer observer)))

;; === 5. КОМПОНЕНТЫ ===

(defn hero-view []
  [ui/HeroSection {:data hero-data
                   :on-price-click #(scroll-to "prices")
                   :on-pill-click open-accordion!}])

(defn modules-view []
  [ui/ModulesSection {:data modules-data
                      :on-contact-click #(scroll-to "contacts")}])

(defn pricing-view []
  (let [{:keys [loading? error prices]} @app-state]
    (if loading?
      [ui/LoadingSpinner]
      [:div
       (when error
         [ui/ErrorBanner {:message error
                          :on-retry fetch-prices!}])
       [ui/PricingSection {:licenses (:licenses prices)
                           :extra-items (:extra_items prices)
                           :modules (:modules prices)
                           :support (:support prices)
                           :services (:services prices)
                           :metadata (:metadata prices)
                           :accordion-states accordion-states
                           :on-contact-click #(scroll-to "contacts")}]])))

(defn contacts-view []
  [ui/ContactsSection {:data contacts-data}])

(defn navbar []
  [ui/Navbar {:on-nav-click scroll-to
              :active-section active-section}])

(defn footer []
  [ui/Footer {:data footer-data
              :on-nav-click scroll-to}])

(defn app-root []
  [:div {:class "min-h-screen flex flex-col font-sans text-slate-900"}
   ;; WebGL Grainient background (ReactBits port) — fixed behind all content
   [ui/GrainientBg {:color1 "#bfdbfe" :color2 "#93c5fd" :color3 "#c7d2fe"
                    :time-speed 0.105 :grain-amount 0.04
                    :class "fixed inset-0 z-[-1]"}]
   [navbar]
   [:main {:class "flex-grow"}
    [hero-view]
    [modules-view]
    [pricing-view]
    [contacts-view]]
   [footer]
   [ui/ScrollToTop]])

(defn init []
  (fetch-prices!)
  (rdom/render [app-root] (.getElementById js/document "app"))
  ;; Connect SSE for real-time updates after initial render
  (js/setTimeout connect-sse! 1000)
  ;; Set up scroll-spy after DOM is ready
  (js/setTimeout setup-scroll-spy! 500))

(defn ^:dev/after-load reload []
  (init))
