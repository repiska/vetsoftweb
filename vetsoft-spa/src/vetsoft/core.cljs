(ns vetsoft.core
  (:require [reagent.dom :as rdom]
            [vetsoft.ui :as ui]))

;; === 1. ДАННЫЕ ===
;; Перенести в content.cljs или db.cljs если будет много данных

(def hero-data
  {:title "ВЕТСОФТ "
   :title-gradient "ВЕТЕРИНАР"
   :subtitle "Профессиональная автоматизация ветеринарных клиник. Лицензии приобретаются один раз и навсегда."
   :btn-primary {:text "Документация" :href "https://help.vetmobile.ru/"}
   :btn-outline {:text "Прайс-лист"}})

(def contacts-data
  {:title "Контакты"
   :address "г. Воронеж, ул. Пушкина, 10"
   :phones ["+7 (473) 229-73-79 (Воронеж)"
            "+7 (495) 204-29-73 (Москва)"]
   :email "info@vetsoft.ru"})

(def modules-data
  [{:title "Клинический блок и Склад" :icon "first-aid"
    :items ["Главный модуль системы" "Модуль «Кабинет врача»" "Система документооборота"
            "Конструктор документов" "Модуль «Склад»" "Ветеринарная Аптека"]}
   {:title "Работа с клиентами и Касса" :icon "users"
    :items ["Регистратура" "Карточка клиента" "Call-центр" "Касса"
            "Учет услуг" "Контроль платежей"]}
   {:title "Управление" :icon "gear"
    :items ["Администрирование" "Система отчетов" "Учет времени"
            "Внутренние задачи" "Поиск"]}])

(def prices-licenses
  [{:name "ВЕТСОФТ ВЕТЕРИНАР - НОРМАЛ" :ver "Локальная (1 место)" :price "38 200"}
   {:name "ВЕТСОФТ ВЕТЕРИНАР - МАЛЫЙ БИЗНЕС"
    :ver "Сетевая (до 3-х подключений)"
    :price "49 800"
    :badge {:text "ПОПУЛЯРНОЕ" :color :cyan}
    :highlight? true}
   {:name "ВЕТСОФТ ВЕТЕРИНАР - УСПЕШНЫЙ"
    :ver "Сетевая (до 5-и подключений)"
    :price "84 900"
    :badge {:text "ХИТ" :color :emerald}
    :highlight? true}
   {:name "ВЕТСОФТ ВЕТЕРИНАР - СУПЕР" :ver "Сетевая (до 10-и подключений)" :price "195 000"}])

(def prices-modules
  [{:name "Модуль расчета заработной платы" :note "Сетевая версия (для сети клиник)" :price "32 700"}
   {:name "Модуль «Стационар»" :note "Для одной клиники" :price "42 000"}
   {:name "Модуль учета движения средств" :note "Сетевая версия" :price "35 000"}])

(def support-rates
  [{:range "от 1-й до 5-ти включительно" :price "1 250"}
   {:range "свыше 5-ти до 10-ти" :price "1 100"}
   {:range "свыше 10-ти до 20-ти" :price "1 000"}
   {:range "свыше 20-ти до 30-ти" :price "900"}
   {:range "свыше 30-ти" :price "800"}])

(def services-items
  [{:name "Удаленная настройка 1 рабочего места" :price "5 000 ₽"}
   {:name "Настройка конфигурации (мин. 2 филиала)" :price "12 500 ₽/ филиал"}
   {:name "Создание отчета" :price "от 5 000 ₽/шт"}
   {:name "Разработка бланка (до 30 полей)" :price "от 3 000 ₽/шт"}
   {:name "Сборка печатной формы" :price "от 3 000 ₽/шт"}
   {:name "Вызов специалиста (только Воронеж)" :price "3 000 ₽"}
   {:name "Работы в Москве / Санкт-Петербурге" :price "договорная" :full-width? true}])

(def footer-data
  {:company "ВЕТСОФТ"
   :copyright "© 2006-2025 Все права защищены."})

;; === 2. ФУНКЦИЯ ПРОКРУТКИ ===

(defn scroll-to [id]
  (when-let [el (.getElementById js/document id)]
    (.scrollIntoView el #js {:behavior "smooth"})))

;; === 3. КОМПОНЕНТЫ ===



(defn hero-view []
  [ui/HeroSection {:data hero-data
                   :on-price-click #(scroll-to "prices")}])

(defn modules-view []
  [ui/ModulesSection {:data modules-data}])

(defn pricing-view []
  [ui/PricingSection {:licenses prices-licenses
                      :modules prices-modules
                      :support support-rates
                      :services services-items}])

(defn contacts-view []
  [ui/ContactsSection {:data contacts-data}])

(defn navbar []
  [ui/Navbar {:on-nav-click scroll-to}])

(defn footer []
  [ui/Footer {:data footer-data}])

(defn app-root []
  [:div {:class "min-h-screen flex flex-col font-sans text-slate-900"}
   [navbar]
   [:main {:class "flex-grow"}
    [hero-view]
    [modules-view]
    [pricing-view]
    [contacts-view]]
   [footer]])

(defn init []
  (rdom/render [app-root] (.getElementById js/document "app")))

(defn ^:dev/after-load reload []
  (init))