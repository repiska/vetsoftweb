(ns vetsoft.ui
  (:require [reagent.core :as r]
            [clojure.string]
            ["ogl" :refer [Renderer Program Mesh Triangle]]))

;; 1. ФАБРИКА КОМПОНЕНТОВ

(defn styled
  "Создает компонент с предустановленными классами Tailwind.
   Позволяет передавать дополнительные классы и атрибуты при использовании."
  [tag base-cls]
  (fn [& args]
    (let [[props children] (if (map? (first args))
                             [(first args) (rest args)]
                             [{} args])
          ;; Склеиваем базовые классы с теми, что передали (user-cls)
          final-cls (str base-cls " " (:class props))
          ;; Убираем :class из props, чтобы не дублировать, и собираем новый props
          final-props (assoc props :class final-cls)]
      ;; Возвращаем вектор [:tag {:class "..." ...} children]
      (into [tag final-props] children))))

;; 2. ЛЕЙАУТ И СЕТКИ

;; Основной контейнер
(def Container
  (styled :div "max-w-6xl mx-auto px-4 sm:px-6 lg:px-8"))

;; Секция — прозрачная, WebGL gradient виден насквозь
(def Section
  (styled :section "py-16 md:py-24 section-reveal"))

;; Hero-секция — прозрачная, общий WebGL gradient фон
(def SectionHero
  (styled :section "py-20 md:py-28 relative"))

;; Альтернативная секция — тоже прозрачная, единый фон
(def SectionGray
  (styled :section "py-16 md:py-24 section-reveal"))

;; Сетка для карточек
(def Grid3
  (styled :div "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"))

;; Flex-контейнер
(def FlexCenter
  (styled :div "flex justify-center items-center"))

;; 3. ТИПОГРАФИКА

;; Главный заголовок — крупнее, font-bold вместо extrabold для элегантности
(def H1
  (styled :h1 "font-display text-5xl md:text-6xl lg:text-7xl font-bold text-slate-900 tracking-tight mb-6 leading-tight"))

;; Градиентный текст для акцентов (светлый — для тёмного Hero фона)
(def GradientText
  (styled :span "text-transparent bg-clip-text bg-gradient-to-r from-blue-200 to-indigo-200"))

;; Компонент ИКОНКИ (Phosphor Icons)
;; Принимает :name (название иконки без 'ph-') и :class (для цвета и размера)
;; Пример: [ui/Icon {:name "house" :class "text-3xl"}]
(defn Icon [{:keys [name class] :or {class "text-2xl"}}]
  [:i {:class (str "ph ph-" name " " class)
       :aria-hidden "true"}])

;; Заголовки секций

;; Заголовки секций
(def H2
  (styled :h2 "font-display text-3xl md:text-4xl font-bold text-center text-slate-900 mb-12 [text-shadow:_0_1px_12px_rgba(255,255,255,0.9)]"))

;; Подзаголовки (в карточках или таблицах)
(def H3
  (styled :h3 "font-display text-xl font-bold text-slate-800 mb-3"))

;; Основной текст
(def Text
  (styled :p "text-slate-700 leading-relaxed"))

;; Крупный текст (для лида или вступления)
(def TextXL
  (styled :p "text-xl text-slate-700 leading-relaxed"))

;; Мелкий текст (сноски)
(def TextSm
  (styled :p "text-sm text-slate-600"))

;; 4. ЭЛЕМЕНТЫ UI

;; Основная кнопка (Синяя, с тенью и анимацией)
(def BtnPrimary
  (styled :a "bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-8 rounded-full shadow-lg hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1 cursor-pointer outline-none focus:ring-4 focus:ring-blue-300"))

;; Второстепенная кнопка (Прозрачная с рамкой)
(def BtnOutline
  (styled :button "bg-transparent hover:bg-blue-50 text-blue-700 font-semibold hover:text-blue-800 py-3 px-8 border-2 border-blue-600 hover:border-blue-700 rounded-full transition-all duration-300 cursor-pointer shadow-sm hover:shadow-md"))

;; Outline кнопка для тёмных фонов (Hero, etc.)
(def BtnOutlineLight
  (styled :button "bg-transparent hover:bg-white/10 text-white font-semibold hover:text-white py-3 px-8 border-2 border-white/60 hover:border-white rounded-full transition-all duration-300 cursor-pointer shadow-sm hover:shadow-md"))

;; Карточка — glass border на фоне градиента, hover lift
(def Card
  (styled :div "bg-white/95 backdrop-blur-sm p-8 rounded-xl shadow-md hover:shadow-lg hover:-translate-y-1 transition-all duration-300 border border-white/40 hover:border-blue-200"))

;; Иконка внутри карточки
(def IconWrapper
  (styled :div "text-5xl mb-6 text-blue-600"))

;; 5. ТАБЛИЦЫ (Tables)

;; Обертка (glass-эффект на градиентном фоне)
(def TableWrapper
  (styled :div "overflow-x-auto rounded-2xl border border-white/40 shadow-sm bg-white/80 backdrop-blur-md"))

(def Table
  (styled :table "min-w-full text-left text-sm"))

;; Заголовок (Контрастный, жирный)
(def Th
  (styled :th "bg-slate-100 px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-700 border-b border-slate-200"))

;; Строка (Мягкий hover)
(def Tr
  (styled :tr "hover:bg-blue-50/60 transition-colors border-b border-slate-200 last:border-none duration-150"))

;; Ячейка (темный текст, фиксированная высота строки)
(def Td
  (styled :td "px-6 py-[14px] text-slate-700 font-medium"))

;; Жирная ячейка (для цен) — правое выравнивание, нейтральный цвет
(def TdBold
  (styled :td "px-6 pr-6 py-[14px] font-semibold text-slate-900 text-right text-base"))

;; 6. СПЕЦИАЛЬНЫЕ ЭЛЕМЕНТЫ (Badges & Items)

;; Компонент карточки услуги (Smart UI)
(defn CardServiceItem [{:keys [name price full-width?]}]
  [:div {:class (str "bg-white p-5 rounded-lg border border-slate-200 shadow-sm flex justify-between items-center gap-4 transition-all duration-200 hover:shadow-md hover:border-blue-200 "
                     (when full-width? "md:col-span-2"))}
   [:span {:class "text-slate-700 font-medium"} name]
   [:span {:class "font-bold text-slate-900 whitespace-nowrap"} price]])

(def BadgeBlue
  (styled :span "inline-flex items-center rounded-md bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-700/10 ml-2 uppercase tracking-wide"))

(def BadgeEmerald
  (styled :span "inline-flex items-center rounded-md bg-emerald-50 px-2 py-1 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-600/20 ml-2 uppercase tracking-wide"))

(def BadgeCyan
  (styled :span "inline-flex items-center rounded-md bg-cyan-50 px-2 py-1 text-xs font-medium text-cyan-700 ring-1 ring-inset ring-cyan-600/20 ml-2 uppercase tracking-wide"))

;; LOADING SPINNER
(defn LoadingSpinner []
  [:div {:class "flex justify-center items-center py-24"}
   [:div {:class "flex flex-col items-center gap-4"}
    [:div {:class "w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin"}]
    [:p {:class "text-slate-500 text-sm"} "Загрузка прайс-листа..."]]])

;; ERROR BANNER
(defn ErrorBanner [{:keys [message on-retry]}]
  [:div {:class "bg-red-50 border border-red-200 rounded-lg p-4 mb-8 flex items-start gap-3"
         :role "alert"}
   [:div {:class "flex-shrink-0 text-red-500 mt-0.5"}
    [:i {:class "ph ph-warning-circle text-xl"}]]
   [:div {:class "flex-grow"}
    [:p {:class "text-red-800 font-medium mb-1"} "Не удалось загрузить прайс-лист"]
    (when message
      [:p {:class "text-red-600 text-sm"} message])]
   (when on-retry
     [:button {:class "flex-shrink-0 bg-red-100 hover:bg-red-200 text-red-800 text-sm font-medium px-3 py-1.5 rounded transition-colors"
               :on-click on-retry}
      "Повторить"])])

;; 7. ФОРМЫ (Forms) - На будущее

(def Label
  (styled :label "block text-sm font-medium text-gray-700 mb-1"))

(def Input
  (styled :input "block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm py-2 px-3 border"))

(def Textarea
  (styled :textarea "block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm py-2 px-3 border"))

;; 7. НАВИГАЦИЯ (Nav)
(def NavContainer
  (styled :nav "bg-white/70 backdrop-blur-xl sticky top-0 z-50 border-b border-white/30 shadow-sm"))

(def NavLink
  (styled :a "text-slate-600 hover:text-blue-600 hover:bg-blue-50 px-3 py-2 rounded-md text-sm font-medium transition-colors cursor-pointer"))

(def NavLinkActive
  (styled :a "bg-blue-100 text-blue-700 px-3 py-2 rounded-md text-sm font-medium cursor-pointer"))

;; 8. СЛОЖНЫЕ КОМПОНЕНТЫ (Smart UI)

;; Компоненты Аккордеона (визуальная часть)
(defn AccordionFrame [props & children]
  [TableWrapper {:class (str "mb-8 transition-all duration-300 " (:class props))}
   children])

(defn AccordionHeader [{:keys [icon title subtitle open? on-click accordion-id]}]
  [:div {:class "bg-white/80 backdrop-blur-md px-6 border-b border-white/30 flex items-start sm:items-center gap-4 cursor-pointer select-none group min-h-[44px] py-4"
         :role "button"
         :aria-expanded (str open?)
         :aria-controls accordion-id
         :tab-index 0
         :on-click on-click
         :on-key-down #(when (= (.-key %) "Enter") (on-click))}
   [:div {:class (str "hidden sm:flex items-center justify-center w-12 h-12 rounded-lg transition-colors "
                      (if open? "bg-blue-50 text-blue-600" "bg-slate-100 text-slate-500 group-hover:bg-slate-200"))}
    [Icon {:name icon :class "text-2xl"}]]
   [:div {:class "flex-grow"}
    [H3 {:class (str "!mb-1 text-lg sm:text-xl transition-colors " (if open? "text-blue-900" "text-slate-800"))} title]
    [:p {:class "text-sm text-slate-500"} subtitle]]
   [:div {:class (str "text-slate-300 transition-transform duration-300 " (if open? "rotate-180" ""))}
    [Icon {:name "caret-up" :class "text-xl"}]]])

(defn AccordionBody [{:keys [open? accordion-id]} & children]
  [:div {:id accordion-id
         :role "region"
         :class (str "accordion-grid bg-white/80 backdrop-blur-md "
                     (if open? "accordion-grid-open" "accordion-grid-closed"))}
   (into [:div {:class (str "transition-opacity duration-300 "
                            (if open? "opacity-100" "opacity-0"))}]
         children)])

;; Строка таблицы прайса с логикой подсветки 
(defn PricingTableRow [{:keys [highlight? hover-effect class]} & children]
  (let [base-cls "border-b border-slate-100 transition-colors duration-150"
        bg-cls (cond
                 highlight? "bg-emerald-50/50 hover:bg-emerald-50"
                 hover-effect "hover:bg-blue-50/50"
                 :else "")]
    (into [:tr {:class (str base-cls " " bg-cls " " class)}] children)))

;; 9. СЕКЦИИ СТРАНИЦЫ (Page Sections)

;; Accordion — supports optional external :open-state atom for controlled mode
(defn Accordion [props & _]
  (let [internal-open? (r/atom (:default-open? props))
        accordion-id (str "accordion-body-" (clojure.string/replace (or (:title props) (str (random-uuid))) #"[^a-zA-Z0-9]" "-"))]
    (fn [props & children]
      (let [open? (or (:open-state props) internal-open?)]
        [AccordionFrame {:class (:class props)}
         [AccordionHeader (merge props {:open? @open?
                                        :on-click #(swap! open? not)
                                        :accordion-id accordion-id})]
         [AccordionBody {:open? @open? :accordion-id accordion-id} children]]))))

;; COUNT-UP ANIMATION (inspired by ReactBits CountUp)
(defn CountUp
  "Animated number counter. Counts from 0 to `target` when visible.
   Props: :target (number), :suffix (string, e.g. '+'), :duration (ms, default 2000)"
  [{:keys [target suffix duration] :or {suffix "" duration 2000}}]
  (let [display-val (r/atom 0)
        started? (atom false)
        el-ref (atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (when-let [el @el-ref]
          (let [observer (js/IntersectionObserver.
                          (fn [entries]
                            (doseq [entry (array-seq entries)]
                              (when (and (.-isIntersecting entry) (not @started?))
                                (reset! started? true)
                                (let [start-time (atom nil)
                                      ease-out (fn [t] (- 1 (js/Math.pow (- 1 t) 3)))
                                      animate! (fn animate! [timestamp]
                                                 (when (nil? @start-time)
                                                   (reset! start-time timestamp))
                                                 (let [elapsed (- timestamp @start-time)
                                                       progress (min 1.0 (/ elapsed duration))
                                                       eased (ease-out progress)
                                                       current (js/Math.round (* target eased))]
                                                   (reset! display-val current)
                                                   (when (< progress 1.0)
                                                     (js/requestAnimationFrame animate!))))]
                                  (js/requestAnimationFrame animate!)))))
                          #js {:threshold 0.5})]
            (.observe observer el))))
      :reagent-render
      (fn [{:keys [target suffix class] :or {suffix "" class ""}}]
        [:span {:ref #(reset! el-ref %)
                :class class}
         (str @display-val suffix)])})))

;; Hero Section — frosted glass panel на общем WebGL gradient фоне
(defn HeroSection [{:keys [data on-price-click on-pill-click]}]
  (let [{:keys [title title-gradient subtitle social-proof btn-primary btn-outline]} data
        pill-items [{:label "Лицензии" :key :licenses}
                    {:label "Модули" :key :modules}
                    {:label "Поддержка" :key :support}
                    {:label "Услуги" :key :services}]]
    [SectionHero {:id "home"}
     [Container {:class "text-center"}
      ;; Frosted glass backdrop
      [:div {:class "bg-white/50 backdrop-blur-lg rounded-3xl px-6 py-10 md:px-12 md:py-14 shadow-xl max-w-4xl mx-auto"}
       [H1 title [:span {:class "text-transparent bg-clip-text bg-gradient-to-r from-blue-700 to-indigo-500"} title-gradient]]
       [:p {:class "max-w-3xl mx-auto mb-6 text-lg md:text-xl text-slate-700 leading-relaxed"}
        [:span {:class "hero-subtitle"} subtitle]]
       ;; Social proof
       (when social-proof
         [:div {:class "flex flex-wrap justify-center gap-6 md:gap-10 mb-8"}
          [:div {:class "flex items-center gap-2 text-slate-800 text-sm md:text-base font-semibold"}
           [Icon {:name "check-circle" :class "text-emerald-500 text-lg"}]
           [CountUp {:target 20 :suffix "" :duration 2600 :class "font-bold"}]
           " лет на рынке"]
          [:div {:class "flex items-center gap-2 text-slate-800 text-sm md:text-base font-semibold"}
           [Icon {:name "check-circle" :class "text-emerald-500 text-lg"}]
           [CountUp {:target 500 :suffix "+" :duration 3250 :class "font-bold"}]
           " клиник"]
          [:div {:class "flex items-center gap-2 text-slate-800 text-sm md:text-base font-semibold"}
           [Icon {:name "check-circle" :class "text-emerald-500 text-lg"}]
           "Бесплатные обновления"]])
       ;; PillNav
       [:div {:class "pill-nav-dark mb-8 mt-2"}
        (for [{:keys [label key]} pill-items]
          ^{:key key}
          [:a {:class "pill-nav-dark-item"
               :role "button"
               :on-click #(when on-pill-click (on-pill-click key))}
           label])]
       [FlexCenter {:class "gap-4 flex-col sm:flex-row"}
        [:div {:class "star-border"}
         [BtnPrimary {:href (:href btn-primary)
                      :on-click (fn [e]
                                  (when (= (:href btn-primary) "#contacts")
                                    (.preventDefault e)
                                    (.scrollIntoView (.getElementById js/document "contacts")
                                                     #js {:behavior "smooth"})))}
          [:span {:class "shiny-cta"} (:text btn-primary)]]]
        ;; Solid outline CTA — видимый на градиенте
        [:button {:class "bg-white/80 hover:bg-white text-blue-700 font-semibold py-3 px-8 rounded-full border border-blue-200 hover:border-blue-300 shadow-sm hover:shadow-md transition-all duration-300 cursor-pointer backdrop-blur-sm"
                  :on-click on-price-click}
         (:text btn-outline)]]]]]))

;; Modules Section (T049 Card style + T051 icon wrappers)
(defn ModulesSection [{:keys [data on-contact-click]}]
  [SectionGray {:id "modules" :class "scroll-mt-24"}
   [Container
    [H2 "Модули и системы"]
    [TextSm {:class "text-center -mt-8 mb-10 text-slate-700 text-base [text-shadow:_0_1px_8px_rgba(255,255,255,0.8)]"}
     "Всё необходимое для эффективной работы вашей клиники"]
    [Grid3
     (map-indexed
      (fn [idx group]
        ^{:key (:title group)}
        [Card {:class "stagger-card"
               :style {:transition-delay (str (* idx 195) "ms")}}
         ;; Иконка с gradient фоном
         [:div {:class "inline-flex items-center justify-center rounded-xl bg-gradient-to-br from-blue-50 to-indigo-50 p-4 text-blue-600 mb-6"}
          [Icon {:name (:icon group) :class "text-4xl"}]]
         [H3 (:title group)]
         (when (:subtitle group)
           [:p {:class "text-sm text-slate-500 mb-4"} (:subtitle group)])
         [:ul {:class "space-y-4 mt-4"}
          (for [item (:items group)]
            ^{:key item}
            [:li {:class "flex items-start text-slate-600 leading-snug"}
             [:span {:class "mr-3 text-emerald-500 mt-0.5 flex-shrink-0"}
              [Icon {:name "check-circle" :class "text-lg"}]]
             item])]])
      data)]
    ;; CTA
    [FlexCenter {:class "mt-12"}
     [BtnPrimary {:href "#contacts"
                  :on-click (fn [e]
                              (.preventDefault e)
                              (when on-contact-click (on-contact-click)))}
      "Запросить демо"]]]])

;; Mobile price card — glass card for mobile layout (replaces table rows on <md)
(defn- render-badge [badge-text badge-color]
  (when badge-text
    (cond
      (#{:blue "blue"} badge-color) [BadgeBlue badge-text]
      (#{:cyan "cyan"} badge-color) [BadgeCyan badge-text]
      :else [BadgeEmerald badge-text])))

(defn MobilePriceCard [{:keys [name version price badge-text badge-color highlight? note]}]
  [:div {:class (str "bg-white/60 backdrop-blur-md rounded-xl p-4 border shadow-sm transition-all "
                     (if highlight? "border-emerald-200/60 bg-emerald-50/30" "border-white/40"))}
   [:div {:class "flex items-start justify-between gap-2 mb-1"}
    [:span {:class "font-bold text-slate-800 text-sm uppercase flex-1 leading-snug"} name]
    (render-badge badge-text badge-color)]
   (when version
     [:div {:class "text-sm text-slate-500 mb-2"} version])
   (when note
     [:div {:class "text-sm text-slate-500 mb-2"} note])
   [:div {:class "text-right font-bold text-slate-900 text-lg"} price]])

;; Mobile support card — compact row for support tiers
(defn MobileSupportCard [{:keys [range price]}]
  [:div {:class "bg-white/60 backdrop-blur-md rounded-xl px-4 py-3 border border-white/40 shadow-sm flex justify-between items-center"}
   [:span {:class "text-slate-700 font-medium text-sm"} range]
   [:span {:class "font-bold text-slate-900"} price]])

;; Helper: format integer price with spaces
(defn- format-price [price]
  (if (number? price)
    (let [s (str price)
          groups (loop [s s result []]
                   (if (<= (count s) 3)
                     (cons s result)
                     (recur (subs s 0 (- (count s) 3))
                            (cons (subs s (- (count s) 3)) result))))]
      (clojure.string/join " " groups))
    (str price)))

;; Pricing Section
(defn PricingSection [{:keys [licenses extra-items modules support services metadata accordion-states on-contact-click]}]
  (let [valid-date (or (get metadata "valid_date") (get metadata :valid_date) "19.03.2026")
        archive-note (or (get metadata "archive_note") (get metadata :archive_note)
                         "Для клиентов с договорами АБ до 31.12.2023 действуют архивные тарифы: от 6050 до 15000 руб./мес. за объект.")]
    [Section {:id "prices" :class "scroll-mt-24"}
     [Container
      [H2 "Прайс-лист"]
      [TextSm {:class "text-center -mt-8 mb-10 text-slate-700 text-base [text-shadow:_0_1px_8px_rgba(255,255,255,0.8)]"}
       "Гибкие тарифы для клиник любого размера"]
      [:div {:class "text-center mb-10"}
       [:span {:class "bg-white/70 backdrop-blur-sm rounded-full px-5 py-1.5 text-sm text-slate-600 inline-block shadow-sm"}
        (str "Цены действительны на " valid-date)]]

      ;; 1. ЛИЦЕНЗИИ
      [Accordion
       {:icon "desktop"
        :title "Стоимость лицензий (Единоразовая покупка)"
        :subtitle "Базовое программное обеспечение. Оплачивается один раз."
        :default-open? true
        :open-state (get accordion-states :licenses)}
       [:div
        ;; Desktop table
        [:div {:class "hidden md:block"}
         [Table
          [:thead
           [:tr {:class "bg-slate-50/80 border-b border-slate-100"}
            [Th {:class "text-left"} "Наименование"]
            [Th {:class "text-left"} "Версия / Подключения"]
            [Th {:class "text-right"} "Стоимость"]]]
          [:tbody
           (for [row licenses]
             ^{:key (or (:id row) (:name row))}
             [PricingTableRow {:highlight? (or (:highlight row) (:highlight? row)) :hover-effect true}
              [Td {:class "py-5"}
               [:div {:class "flex items-center flex-wrap gap-2"}
                [:span {:class "font-bold text-slate-800 text-sm sm:text-base uppercase"} (:name row)]
                (render-badge (or (:badge-text row) (get-in row [:badge :text]))
                              (or (:badge-color row) (get-in row [:badge :color])))]]
              [Td {:class "py-5 text-sm text-slate-600"}
               (or (:version row) (:ver row))]
              [TdBold {:class "py-5"}
               (str (format-price (:price row)) " ₽")]])
           (for [item (or extra-items [])]
             ^{:key (or (:id item) (:name item))}
             [PricingTableRow {:hover-effect true}
              [Td {:class "py-5"} (:name item)]
              [Td {:class "py-5 text-sm text-slate-600"} (:description item)]
              [TdBold {:class "py-5"} (str (format-price (:price item)) " ₽")]])]]]
        ;; Mobile cards
        [:div {:class "md:hidden space-y-3 p-4"}
         (for [row licenses]
           ^{:key (str "m-" (or (:id row) (:name row)))}
           [MobilePriceCard {:name (:name row)
                             :version (or (:version row) (:ver row))
                             :price (str (format-price (:price row)) " ₽")
                             :badge-text (or (:badge-text row) (get-in row [:badge :text]))
                             :badge-color (or (:badge-color row) (get-in row [:badge :color]))
                             :highlight? (or (:highlight row) (:highlight? row))}])
         (for [item (or extra-items [])]
           ^{:key (str "m-" (or (:id item) (:name item)))}
           [MobilePriceCard {:name (:name item)
                             :version (:description item)
                             :price (str (format-price (:price item)) " ₽")}])]]]

      ;; 2. МОДУЛИ
      [Accordion
       {:icon "puzzle-piece"
        :title "Дополнительные модули"
        :subtitle "Расширение функционала системы."
        :open-state (get accordion-states :modules)}
       [:div
        ;; Desktop table
        [:div {:class "hidden md:block"}
         [Table
          [:thead
           [:tr {:class "bg-slate-50/80 border-b border-slate-100"}
            [Th {:class "text-left"} "Наименование"]
            [Th {:class "text-left"} "Примечание"]
            [Th {:class "text-right"} "Стоимость (руб./мес.)"]]]
          [:tbody
           (for [m modules]
             ^{:key (or (:id m) (:name m))}
             [PricingTableRow {:hover-effect true}
              [Td {:class "py-5 font-bold text-slate-800"} (:name m)]
              [Td {:class "py-5 text-slate-500"} (:note m)]
              [TdBold {:class "py-5"} (str (format-price (:price m)) " ₽")]])]]]
        ;; Mobile cards
        [:div {:class "md:hidden space-y-3 p-4"}
         (for [m modules]
           ^{:key (str "m-" (or (:id m) (:name m)))}
           [MobilePriceCard {:name (:name m)
                             :note (:note m)
                             :price (str (format-price (:price m)) " ₽")}])]]]

      ;; 3. ПОДДЕРЖКА
      [Accordion
       {:icon "headset"
        :title "Техническая поддержка (Абонентская плата)"
        :subtitle "Стоимость за 1 лицензию в месяц."
        :open-state (get accordion-states :support)}
       [:div
        ;; Desktop table
        [:div {:class "hidden md:block"}
         [Table
          [:thead
           [:tr {:class "bg-slate-50/80 border-b border-slate-100"}
            [Th {:class "text-left"} "Количество лицензий"]
            [Th {:class "text-right"} "Стоимость (руб./мес. за 1 лицензию)"]]]
          [:tbody
           (for [s support]
             ^{:key (or (:id s) (:license-range s) (:range s))}
             [PricingTableRow {:hover-effect true}
              [Td {:class "py-5"} (or (:license-range s) (:range s))]
              [TdBold {:class "py-5"} (str (format-price (:price s)) " ₽")]])]]]
        ;; Mobile cards
        [:div {:class "md:hidden space-y-2 p-4"}
         (for [s support]
           ^{:key (str "m-" (or (:id s) (:license-range s) (:range s)))}
           [MobileSupportCard {:range (or (:license-range s) (:range s))
                               :price (str (format-price (:price s)) " ₽")}])]]]

      ;; 4. УСЛУГИ
      [Accordion
       {:icon "wrench"
        :title "Услуги и Настройка"
        :subtitle "Работы по обслуживанию и индивидуальной конфигурации."
        :open-state (get accordion-states :services)}
       [:div {:class "p-6 bg-slate-50"}
        [:div {:class "grid grid-cols-1 md:grid-cols-2 gap-4"}
         (for [item services]
           ^{:key (or (:id item) (:name item))}
           [CardServiceItem {:name (:name item)
                             :price (:price item)
                             :full-width? (or (:full-width item) (:full_width item))}])]]]

      [:div {:class "text-center mt-4 mb-8"}
       [:span {:class "bg-white/60 backdrop-blur-sm rounded-lg px-4 py-2 text-sm text-slate-600 inline-block"}
        (str "* " archive-note)]]

      ;; CTA
      [FlexCenter {:class "mt-4"}
       [:button {:class "bg-white/80 hover:bg-white text-blue-700 font-semibold py-3 px-8 rounded-full border border-blue-200 hover:border-blue-300 shadow-sm hover:shadow-md transition-all duration-300 cursor-pointer backdrop-blur-sm"
                 :on-click (fn [] (when on-contact-click (on-contact-click)))}
        "Получить коммерческое предложение"]]]]))

;; Contacts Section
(defn- phone->tel
  "Strip formatting from a phone string and produce a tel: href.
   E.g. '+7 (473) 229-73-79 (Воронеж)' -> 'tel:+74732297379'"
  [phone]
  (str "tel:" (clojure.string/replace phone #"[^+\d]" "")))

(defn ContactsSection [{:keys [data]}]
  (let [{:keys [title address phones email]} data
        first-phone (first phones)]
    [SectionGray {:id "contacts" :class "scroll-mt-24"}
     [Container
      [H2 title]
      [TextSm {:class "text-center -mt-8 mb-10 text-slate-700 text-base [text-shadow:_0_1px_8px_rgba(255,255,255,0.8)]"}
       "Свяжитесь с нами для консультации или демонстрации системы"]
      ;; 2-column grid on desktop, stacked on mobile
      [:div {:class "grid grid-cols-1 md:grid-cols-2 gap-8 items-start"}
       ;; LEFT — Contact card
       [Card {:class "text-center py-10"}
        ;; Address
        [TextXL {:class "mb-6 font-medium flex flex-col items-center gap-3"}
         [:div {:class "text-blue-600"} [Icon {:name "map-pin" :class "text-5xl"}]]
         address]
        ;; Phones + email
        [:div {:class "space-y-4 text-lg mb-8"}
         (for [phone phones]
           ^{:key phone}
           [:div {:class "flex items-center justify-center gap-3"}
            [Icon {:name "phone" :class "text-slate-500 text-xl"}]
            [:a {:href (phone->tel phone)
                 :class "hover:text-blue-600 transition-colors"}
             phone]])
         [:div {:class "flex items-center justify-center gap-3 font-medium"}
          [Icon {:name "envelope" :class "text-xl text-blue-600"}]
          [:a {:href (str "mailto:" email)
               :class "text-blue-600 hover:text-blue-800 transition-colors"}
           email]]]
        ;; CTA buttons
        [:div {:class "flex flex-col sm:flex-row gap-3 justify-center"}
         [BtnPrimary {:href (phone->tel first-phone)
                      :class "text-center justify-center flex items-center gap-2"}
          [Icon {:name "phone" :class "text-lg"}]
          "Позвонить"]
         [BtnOutline {:class "flex items-center gap-2 justify-center"
                      :on-click #(.assign js/window.location (str "mailto:" email))}
          [Icon {:name "envelope" :class "text-lg"}]
          "Написать на email"]]]
       ;; RIGHT — About the company
       [Card {:class "py-10"}
        [:div {:class "inline-flex items-center justify-center rounded-xl bg-blue-50 p-3 text-blue-600 mb-6"}
         [Icon {:name "clock" :class "text-3xl"}]]
        [H3 "Режим работы"]
        [:div {:class "space-y-3 text-slate-700 mb-8"}
         [:div {:class "flex items-start gap-3"}
          [Icon {:name "calendar" :class "text-slate-500 text-lg mt-0.5"}]
          [:div
           [:p {:class "font-medium text-slate-800"} "Понедельник — Пятница"]
           [:p {:class "text-slate-600"} "9:00 — 18:00 (по московскому времени)"]]]
         [:div {:class "flex items-start gap-3"}
          [Icon {:name "calendar-x" :class "text-slate-500 text-lg mt-0.5"}]
          [:div
           [:p {:class "font-medium text-slate-800"} "Суббота — Воскресенье"]
           [:p {:class "text-slate-600"} "Выходной"]]]]
        [:div {:class "h-px bg-slate-100 my-6"}]
        [:div {:class "inline-flex items-center justify-center rounded-xl bg-emerald-50 p-3 text-emerald-600 mb-6"}
         [Icon {:name "shield-check" :class "text-3xl"}]]
        [H3 "Почему ВЕТСОФТ"]
        [:ul {:class "space-y-3"}
         (for [item ["Работаем с 2006 года"
                     "Техподдержка включена в абонплату"
                     "Обновления системы бесплатно"
                     "Индивидуальный подход"]]
           ^{:key item}
           [:li {:class "flex items-start gap-3 text-slate-600"}
            [Icon {:name "check-circle" :class "text-emerald-500 text-lg mt-0.5"}]
            item])]]]]]))

;; Scroll-to-top floating button (T059)
(defn ScrollToTop []
  (let [visible? (r/atom false)
        on-scroll (fn []
                    (reset! visible? (> (.-scrollY js/window) 400)))]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (.addEventListener js/window "scroll" on-scroll #js {:passive true}))
      :component-will-unmount
      (fn [_]
        (.removeEventListener js/window "scroll" on-scroll))
      :reagent-render
      (fn []
        [:button
         {:class (str "fixed bottom-6 right-6 z-50 w-12 h-12 rounded-full bg-blue-600 text-white "
                      "flex items-center justify-center shadow-lg hover:bg-blue-700 "
                      "focus:outline-none focus:ring-4 focus:ring-blue-300 "
                      "scroll-top-btn "
                      (if @visible? "scroll-top-visible" "scroll-top-hidden"))
          :aria-label "Прокрутить наверх"
          :on-click #(.scrollTo js/window #js {:top 0 :behavior "smooth"})}
         [Icon {:name "arrow-up" :class "text-xl"}]])})))

;; Navbar
(defn Navbar [{:keys [on-nav-click active-section]}]
  (let [menu-open? (r/atom false)
        close-menu! #(reset! menu-open? false)
        toggle-menu! #(swap! menu-open? not)]
    (fn [{:keys [on-nav-click active-section]}]
      (let [nav-click (fn [section]
                        (close-menu!)
                        (on-nav-click section))
            current-section (if active-section @active-section "home")
            nav-link-for (fn [section label]
                           (if (= current-section section)
                             [NavLinkActive {:on-click #(nav-click section)} label]
                             [NavLink {:on-click #(nav-click section)} label]))
            mobile-link-cls (fn [section]
                              (str "block px-3 py-3 rounded-md text-sm font-medium transition-colors cursor-pointer "
                                   (if (= current-section section)
                                     "bg-blue-100 text-blue-700"
                                     "text-slate-600 hover:text-blue-600 hover:bg-blue-50")))]
        [NavContainer
         [Container
          [:div {:class "flex justify-between items-center h-16"}
           [:div {:class "font-display font-extrabold text-2xl text-blue-600 tracking-tight cursor-pointer"
                  :on-click #(nav-click "home")}
            "ВЕТСОФТ"]
           ;; Desktop nav links — hidden on mobile
           [:div {:class "hidden md:flex space-x-1"}
            [nav-link-for "home" "Главная"]
            [nav-link-for "modules" "Модули"]
            [nav-link-for "prices" "Цены"]
            [nav-link-for "contacts" "Контакты"]]
           ;; Hamburger button — visible only on mobile
           [:button {:class "md:hidden flex items-center justify-center w-10 h-10 rounded-md text-slate-600 hover:text-blue-600 hover:bg-blue-50 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-300"
                     :on-click toggle-menu!
                     :aria-expanded (str @menu-open?)
                     :aria-controls "mobile-menu"
                     :aria-label (if @menu-open? "Закрыть меню" "Открыть меню")}
            [:i {:class (str "ph text-2xl " (if @menu-open? "ph-x" "ph-list"))
                 :aria-hidden "true"}]]]
          ;; Mobile dropdown — visible only on mobile, animated
          [:div {:id "mobile-menu"
                 :class (str "md:hidden overflow-hidden transition-all duration-300 ease-in-out "
                             (if @menu-open? "max-h-64 opacity-100" "max-h-0 opacity-0"))}
           [:div {:class "py-2 border-t border-slate-100 space-y-1"}
            [:a {:class (mobile-link-cls "home") :on-click #(nav-click "home")} "Главная"]
            [:a {:class (mobile-link-cls "modules") :on-click #(nav-click "modules")} "Модули"]
            [:a {:class (mobile-link-cls "prices") :on-click #(nav-click "prices")} "Цены"]
            [:a {:class (mobile-link-cls "contacts") :on-click #(nav-click "contacts")} "Контакты"]]]]]))))

;; Footer
(defn Footer [{:keys [data on-nav-click]}]
  (let [{:keys [company copyright nav-links phones email]} data
        nav-click (fn [section]
                    (when on-nav-click (on-nav-click section)))]
    [:footer {:class "bg-slate-900 text-slate-500 py-14 relative"}
     ;; Gradient transition from WebGL bg to dark footer
     [:div {:class "absolute -top-24 left-0 right-0 h-24 bg-gradient-to-b from-transparent to-slate-900 pointer-events-none"}]
     [Container
      ;; 3-column layout on desktop, stacked on mobile
      [:div {:class "grid grid-cols-1 md:grid-cols-3 gap-10 mb-10"}
       ;; Column 1: Brand
       [:div {:class "flex flex-col items-center md:items-start gap-2"}
        [:div {:class "font-display font-extrabold text-2xl text-white tracking-tight mb-2"} company]
        [:p {:class "text-sm text-slate-400 leading-relaxed"}
         "Профессиональная автоматизация\nветеринарных клиник."]]
       ;; Column 2: Navigation links
       [:div {:class "flex flex-col items-center md:items-start gap-3"}
        [:p {:class "text-xs font-bold uppercase tracking-wider text-slate-400 mb-1"} "Навигация"]
        (for [{:keys [label section]} nav-links]
          ^{:key section}
          [:a {:class "text-slate-200 hover:text-white transition-colors text-sm cursor-pointer"
               :on-click #(nav-click section)}
           label])]
       ;; Column 3: Contacts
       [:div {:class "flex flex-col items-center md:items-start gap-3"}
        [:p {:class "text-xs font-bold uppercase tracking-wider text-slate-400 mb-1"} "Контакты"]
        (for [phone phones]
          ^{:key phone}
          [:a {:href (str "tel:" (clojure.string/replace phone #"[^+\d]" ""))
               :class "text-slate-300 hover:text-white transition-colors text-sm flex items-center gap-2"}
           [:i {:class "ph ph-phone text-slate-400" :aria-hidden "true"}]
           phone])
        (when email
          [:a {:href (str "mailto:" email)
               :class "text-slate-300 hover:text-white transition-colors text-sm flex items-center gap-2"}
           [:i {:class "ph ph-envelope text-slate-400" :aria-hidden "true"}]
           email])]]
      ;; Bottom copyright bar
      [:div {:class "border-t border-slate-800 pt-8 text-center text-xs text-slate-500"}
       copyright]]]))

;; ============================================================
;; 10. WebGL GRAINIENT BACKGROUND (ported from ReactBits)
;; ============================================================

(def ^:private grainient-vertex
  "#version 300 es\nin vec2 position;\nvoid main() { gl_Position = vec4(position, 0.0, 1.0); }")

(def ^:private grainient-fragment
  "#version 300 es
precision highp float;
uniform vec2 iResolution;
uniform float iTime;
uniform float uTimeSpeed;
uniform float uColorBalance;
uniform float uWarpStrength;
uniform float uWarpFrequency;
uniform float uWarpSpeed;
uniform float uWarpAmplitude;
uniform float uBlendAngle;
uniform float uBlendSoftness;
uniform float uRotationAmount;
uniform float uNoiseScale;
uniform float uGrainAmount;
uniform float uGrainScale;
uniform float uGrainAnimated;
uniform float uContrast;
uniform float uGamma;
uniform float uSaturation;
uniform vec2 uCenterOffset;
uniform float uZoom;
uniform vec3 uColor1;
uniform vec3 uColor2;
uniform vec3 uColor3;
out vec4 fragColor;
#define S(a,b,t) smoothstep(a,b,t)
mat2 Rot(float a){float s=sin(a),c=cos(a);return mat2(c,-s,s,c);}
vec2 hash(vec2 p){p=vec2(dot(p,vec2(2127.1,81.17)),dot(p,vec2(1269.5,283.37)));return fract(sin(p)*43758.5453);}
float noise(vec2 p){vec2 i=floor(p),f=fract(p),u=f*f*(3.0-2.0*f);float n=mix(mix(dot(-1.0+2.0*hash(i+vec2(0.0,0.0)),f-vec2(0.0,0.0)),dot(-1.0+2.0*hash(i+vec2(1.0,0.0)),f-vec2(1.0,0.0)),u.x),mix(dot(-1.0+2.0*hash(i+vec2(0.0,1.0)),f-vec2(0.0,1.0)),dot(-1.0+2.0*hash(i+vec2(1.0,1.0)),f-vec2(1.0,1.0)),u.x),u.y);return 0.5+0.5*n;}
void mainImage(out vec4 o,vec2 C){
  float t=iTime*uTimeSpeed;
  vec2 uv=C/iResolution.xy;
  float ratio=iResolution.x/iResolution.y;
  vec2 tuv=uv-0.5+uCenterOffset;
  tuv/=max(uZoom,0.001);
  float degree=noise(vec2(t*0.1,tuv.x*tuv.y)*uNoiseScale);
  tuv.y*=1.0/ratio;
  tuv*=Rot(radians((degree-0.5)*uRotationAmount+180.0));
  tuv.y*=ratio;
  float frequency=uWarpFrequency;
  float ws=max(uWarpStrength,0.001);
  float amplitude=uWarpAmplitude/ws;
  float warpTime=t*uWarpSpeed;
  tuv.x+=sin(tuv.y*frequency+warpTime)/amplitude;
  tuv.y+=sin(tuv.x*(frequency*1.5)+warpTime)/(amplitude*0.5);
  vec3 colLav=uColor1;vec3 colOrg=uColor2;vec3 colDark=uColor3;
  float b=uColorBalance;float s=max(uBlendSoftness,0.0);
  mat2 blendRot=Rot(radians(uBlendAngle));
  float blendX=(tuv*blendRot).x;
  float edge0=-0.3-b-s;float edge1=0.2-b+s;
  float v0=0.5-b+s;float v1=-0.3-b-s;
  vec3 layer1=mix(colDark,colOrg,S(edge0,edge1,blendX));
  vec3 layer2=mix(colOrg,colLav,S(edge0,edge1,blendX));
  vec3 col=mix(layer1,layer2,S(v0,v1,tuv.y));
  vec2 grainUv=uv*max(uGrainScale,0.001);
  if(uGrainAnimated>0.5){grainUv+=vec2(iTime*0.05);}
  float grain=fract(sin(dot(grainUv,vec2(12.9898,78.233)))*43758.5453);
  col+=(grain-0.5)*uGrainAmount;
  col=(col-0.5)*uContrast+0.5;
  float luma=dot(col,vec3(0.2126,0.7152,0.0722));
  col=mix(vec3(luma),col,uSaturation);
  col=pow(max(col,0.0),vec3(1.0/max(uGamma,0.001)));
  col=clamp(col,0.0,1.0);
  o=vec4(col,1.0);
}
void main(){vec4 o=vec4(0.0);mainImage(o,gl_FragCoord.xy);fragColor=o;}")

(defn- hex->rgb
  "Convert hex color to [r g b] floats 0-1."
  [hex]
  (let [h (clojure.string/replace hex #"^#" "")
        r (js/parseInt (subs h 0 2) 16)
        g (js/parseInt (subs h 2 4) 16)
        b (js/parseInt (subs h 4 6) 16)]
    [(/ r 255.0) (/ g 255.0) (/ b 255.0)]))

(defn GrainientBg
  "WebGL animated gradient background (ReactBits Grainient port).
   Renders a fixed-position canvas behind all content."
  [{:keys [color1 color2 color3 time-speed grain-amount class]
    :or {color1 "#93C5FD" color2 "#2563EB" color3 "#818CF8"
         time-speed 0.15 grain-amount 0.05 class ""}}]
  (let [container-ref (atom nil)
        cleanup-ref (atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (when-let [container @container-ref]
          (try
            (let [renderer (Renderer. #js {:webgl 2 :alpha true :antialias false
                                           :dpr (min (or js/window.devicePixelRatio 1) 2)})
                  gl (.-gl renderer)
                  canvas (.-canvas gl)
                  _ (set! (.. canvas -style -width) "100%")
                  _ (set! (.. canvas -style -height) "100%")
                  _ (set! (.. canvas -style -display) "block")
                  _ (.appendChild container canvas)
                  geometry (Triangle. gl)
                  [r1 g1 b1] (hex->rgb color1)
                  [r2 g2 b2] (hex->rgb color2)
                  [r3 g3 b3] (hex->rgb color3)
                  program (Program. gl #js
                            {:vertex grainient-vertex
                             :fragment grainient-fragment
                             :uniforms #js
                              {:iTime       #js {:value 0}
                               :iResolution #js {:value (js/Float32Array. #js [1 1])}
                               :uTimeSpeed  #js {:value time-speed}
                               :uColorBalance #js {:value 0.0}
                               :uWarpStrength #js {:value 0.5}
                               :uWarpFrequency #js {:value 5.0}
                               :uWarpSpeed  #js {:value 2.0}
                               :uWarpAmplitude #js {:value 50.0}
                               :uBlendAngle #js {:value 0.0}
                               :uBlendSoftness #js {:value 0.05}
                               :uRotationAmount #js {:value 500.0}
                               :uNoiseScale #js {:value 2.0}
                               :uGrainAmount #js {:value grain-amount}
                               :uGrainScale #js {:value 2.0}
                               :uGrainAnimated #js {:value 0.0}
                               :uContrast   #js {:value 1.2}
                               :uGamma      #js {:value 1.0}
                               :uSaturation #js {:value 0.8}
                               :uCenterOffset #js {:value (js/Float32Array. #js [0 0])}
                               :uZoom       #js {:value 0.9}
                               :uColor1     #js {:value (js/Float32Array. #js [r1 g1 b1])}
                               :uColor2     #js {:value (js/Float32Array. #js [r2 g2 b2])}
                               :uColor3     #js {:value (js/Float32Array. #js [r3 g3 b3])}}})
                  mesh (Mesh. gl #js {:geometry geometry :program program})
                  set-size! (fn []
                              (let [rect (.getBoundingClientRect container)
                                    w (max 1 (js/Math.floor (.-width rect)))
                                    h (max 1 (js/Math.floor (.-height rect)))]
                                (.setSize renderer w h)
                                (let [res (.. program -uniforms -iResolution -value)]
                                  (aset res 0 (.-drawingBufferWidth gl))
                                  (aset res 1 (.-drawingBufferHeight gl)))))
                  ro (js/ResizeObserver. set-size!)
                  _ (.observe ro container)
                  _ (set-size!)
                  t0 (js/performance.now)
                  raf-id (atom 0)
                  loop-fn (fn loop-fn [t]
                            (set! (.. program -uniforms -iTime -value) (* (- t t0) 0.001))
                            (.render renderer #js {:scene mesh})
                            (reset! raf-id (js/requestAnimationFrame loop-fn)))]
              (reset! raf-id (js/requestAnimationFrame loop-fn))
              (reset! cleanup-ref
                      (fn []
                        (js/cancelAnimationFrame @raf-id)
                        (.disconnect ro)
                        (try (.removeChild container canvas) (catch :default _)))))
            (catch :default e
              (js/console.warn "WebGL Grainient failed, using CSS fallback:" e)))))
      :component-will-unmount
      (fn [_]
        (when-let [cleanup @cleanup-ref]
          (cleanup)))
      :reagent-render
      (fn [{:keys [class] :or {class ""}}]
        [:div {:ref #(reset! container-ref %)
               :class (str "grainient-container " class)
               :aria-hidden "true"}])})))

