(ns vetsoft.ui
  (:require [reagent.core :as r]))

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

;; Секция с отступами
(def Section
  (styled :section "py-16 md:py-24 bg-white border-b border-gray-100"))

;; Hero-секция с градиентом
(def SectionHero
  (styled :section "py-20 md:py-28 bg-gradient-to-br from-blue-50 via-white to-blue-50 border-b border-gray-100 relative overflow-hidden"))

;; Альтернативная секция
(def SectionGray
  (styled :section "py-16 md:py-24 bg-slate-50 border-b border-gray-200"))

;; Сетка для карточек
(def Grid3
  (styled :div "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"))

;; Flex-контейнер
(def FlexCenter
  (styled :div "flex justify-center items-center"))


;; 3. ТИПОГРАФИКА

;; Главный заголовок
(def H1
  (styled :h1 "text-4xl md:text-5xl lg:text-6xl font-extrabold text-slate-900 tracking-tight mb-6 leading-tight"))

;; Градиентный текст для акцентов
(def GradientText
  (styled :span "text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600"))

;; Компонент ИКОНКИ (Phosphor Icons)
;; Принимает :name (название иконки без 'ph-') и :class (для цвета и размера)
;; Пример: [ui/Icon {:name "house" :class "text-3xl"}]
(defn Icon [{:keys [name class] :or {class "text-2xl"}}]
  [:i {:class (str "ph ph-" name " " class)}])

;; Заголовки секций

;; Заголовки секций
(def H2
  (styled :h2 "text-3xl font-bold text-center text-slate-800 mb-10"))

;; Подзаголовки (в карточках или таблицах)
(def H3
  (styled :h3 "text-xl font-bold text-slate-800 mb-3"))

;; Основной текст
(def Text
  (styled :p "text-slate-700 leading-relaxed"))

;; Крупный текст (для лида или вступления)
(def TextXL
  (styled :p "text-xl text-slate-700 leading-relaxed"))

;; Мелкий текст (сноски)
(def TextSm
  (styled :p "text-sm text-slate-500 italic"))


;; 4. ЭЛЕМЕНТЫ UI

;; Основная кнопка (Синяя, с тенью и анимацией)
(def BtnPrimary
  (styled :a "bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-8 rounded-full shadow-lg hover:shadow-xl transition duration-300 transform hover:-translate-y-1 cursor-pointer outline-none focus:ring-4 focus:ring-blue-300"))

;; Второстепенная кнопка (Прозрачная с рамкой)
(def BtnOutline
  (styled :button "bg-transparent hover:bg-blue-50 text-blue-700 font-semibold hover:text-blue-800 py-2 px-6 border-2 border-blue-600 hover:border-blue-700 rounded-lg transition duration-300 cursor-pointer"))

;; Карточка (Белая плашка с тенью)
(def Card
  (styled :div "bg-white p-8 rounded-2xl shadow-sm hover:shadow-lg transition-shadow duration-300 border border-slate-200"))

;; Иконка внутри карточки
(def IconWrapper
  (styled :div "text-5xl mb-6 text-blue-600"))


;; 5. ТАБЛИЦЫ (Tables)

;; Обертка (скругление, легкая тень, бордер)
(def TableWrapper
  (styled :div "overflow-x-auto rounded-2xl border border-slate-200 shadow-sm bg-white"))

(def Table
  (styled :table "min-w-full text-left text-sm"))

;; Заголовок (Контрастный, жирный)
(def Th
  (styled :th "bg-slate-100 px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-700 border-b border-slate-200"))

;; Строка (Контрастный hover)
(def Tr
  (styled :tr "hover:bg-blue-100 transition-colors border-b border-slate-200 last:border-none duration-150"))

;; Ячейка (темный текст)
(def Td
  (styled :td "px-6 py-4 text-slate-700 whitespace-nowrap font-medium"))

;; Жирная ячейка (для цен)
(def TdBold
  (styled :td "px-6 py-4 font-bold text-blue-700 whitespace-nowrap text-base"))



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

;; 7. ФОРМЫ (Forms) - На будущее

(def Label
  (styled :label "block text-sm font-medium text-gray-700 mb-1"))

(def Input
  (styled :input "block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm py-2 px-3 border"))

(def Textarea
  (styled :textarea "block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm py-2 px-3 border"))


;; 7. НАВИГАЦИЯ (Nav)
(def NavContainer
  (styled :nav "bg-white/80 backdrop-blur-md sticky top-0 z-50 border-b border-slate-200"))

(def NavLink
  (styled :a "text-slate-600 hover:text-blue-600 hover:bg-blue-50 px-3 py-2 rounded-md text-sm font-medium transition-colors cursor-pointer"))

(def NavLinkActive
  (styled :a "bg-blue-100 text-blue-700 px-3 py-2 rounded-md text-sm font-medium cursor-pointer"))


;; 8. СЛОЖНЫЕ КОМПОНЕНТЫ (Smart UI)

;; Компоненты Аккордеона (визуальная часть)
(defn AccordionFrame [props & children]
  [TableWrapper {:class (str "mb-8 transition-all duration-300 " (:class props))}
   children])

(defn AccordionHeader [{:keys [icon title subtitle open? on-click]}]
  [:div {:class "bg-white p-6 border-b border-slate-100 flex items-start sm:items-center gap-4 cursor-pointer select-none group"
         :on-click on-click}
   [:div {:class (str "hidden sm:flex items-center justify-center w-12 h-12 rounded-lg transition-colors "
                      (if open? "bg-blue-50 text-blue-600" "bg-slate-100 text-slate-500 group-hover:bg-slate-200"))}
    [Icon {:name icon :class "text-2xl"}]]
   [:div {:class "flex-grow"}
    [H3 {:class (str "!mb-1 text-lg sm:text-xl transition-colors " (if open? "text-blue-900" "text-slate-800"))} title]
    [:p {:class "text-sm text-slate-500"} subtitle]]
   [:div {:class (str "text-slate-300 transition-transform duration-300 " (if open? "rotate-180" ""))}
    [Icon {:name "caret-up" :class "text-xl"}]]])

(defn AccordionBody [{:keys [open?]} & children]
  [:div {:class (str "transition-all duration-500 ease-in-out overflow-hidden bg-white "
                     (if open? "max-h-[2000px] opacity-100" "max-h-0 opacity-0"))}
   children])

;; Строка таблицы прайса с логикой подсветки 
(defn PricingTableRow [{:keys [highlight? hover-effect class]} & children]
  (let [base-cls "border-b border-slate-100 transition-colors duration-150"
        bg-cls (cond
                 highlight? "bg-emerald-50/50 hover:bg-emerald-50"
                 hover-effect "hover:bg-blue-50/50"
                 :else "")]
    (into [:tr {:class (str base-cls " " bg-cls " " class)}] children)))


;; 9. СЕКЦИИ СТРАНИЦЫ (Page Sections)

;; Accordion
(defn Accordion [props & _]
  (let [open? (r/atom (:default-open? props))]
    (fn [props & children]
      [AccordionFrame {:class (:class props)}
       [AccordionHeader (merge props {:open? @open? :on-click #(swap! open? not)})]
       [AccordionBody {:open? @open?} children]])))

;; Hero Section
(defn HeroSection [{:keys [data on-price-click]}]
  (let [{:keys [title title-gradient subtitle btn-primary btn-outline]} data]
    [SectionHero {:id "home"}
     [Container {:class "text-center"}
      [H1 title [GradientText title-gradient]]
      [TextXL {:class "max-w-3xl mx-auto mb-10 text-lg md:text-xl text-slate-500"}
       subtitle]
      [FlexCenter {:class "gap-4 flex-col sm:flex-row"}
       [BtnPrimary {:href (:href btn-primary)} (:text btn-primary)]
       [BtnOutline {:on-click on-price-click} (:text btn-outline)]]]]))

;; Modules Section
(defn ModulesSection [{:keys [data]}]
  [SectionGray {:id "modules" :class "scroll-mt-24"}
   [Container
    [H2 "Модули и системы"]
    [Grid3
     (for [group data]
       ^{:key (:title group)}
       [Card
        [:div {:class "mb-6 text-blue-600"}
         [Icon {:name (:icon group) :class "text-5xl"}]]
        [H3 (:title group)]
        [:ul {:class "space-y-4 mt-6"}
         (for [item (:items group)]
           ^{:key item}
           [:li {:class "flex items-start text-slate-600 leading-snug"}
            [:span {:class "mr-3 text-emerald-500 mt-0.5"}
             [Icon {:name "check-circle" :class "text-lg"}]]
            item])]])]]])

;; Pricing Section
(defn PricingSection [{:keys [licenses modules support services]}]
  [Section {:id "prices" :class "scroll-mt-24"}
   [Container
    [H2 "Прайс-лист"]
    [TextSm {:class "text-center mb-10 text-slate-400"} "Цены действительны на 15.12.2024."]

    ;; 1. ЛИЦЕНЗИИ
    [Accordion
     {:icon "desktop"
      :title "Стоимость лицензий (Единоразовая покупка)"
      :subtitle "Базовое программное обеспечение. Оплачивается один раз."
      :default-open? true}
     [Table
      [:thead
       [:tr {:class "bg-slate-50 border-b border-slate-100"}
        [:th {:class "px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-left"} "Наименование"]
        [:th {:class "px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-left"} "Версия / Подключения"]
        [:th {:class "px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-right"} "Стоимость"]]]
      [:tbody
       (for [row licenses]
         ^{:key (:name row)}
         [PricingTableRow {:highlight? (:highlight? row) :hover-effect true}
          [:td {:class "px-6 py-5"}
           [:div {:class "flex items-center flex-wrap gap-2"}
            [:span {:class "font-bold text-slate-800 text-sm sm:text-base uppercase"} (:name row)]
            (when-let [b (:badge row)]
              (cond
                (= (:color b) :blue) [BadgeBlue (:text b)]
                (= (:color b) :cyan) [BadgeCyan (:text b)]
                :else [BadgeEmerald (:text b)]))]]
          [:td {:class "px-6 py-5 text-sm text-slate-600"}
           (:ver row)]
          [:td {:class "px-6 py-5 text-right font-bold text-slate-900 whitespace-nowrap"}
           (str (:price row) " ₽")]])
       [PricingTableRow {:hover-effect true}
        [:td {:class "px-6 py-5 text-slate-700 font-medium"} "Дополнительное рабочее место"]
        [:td {:class "px-6 py-5 text-slate-600 text-sm"} "1 шт."]
        [:td {:class "px-6 py-5 text-right font-bold text-slate-900"} "19 500 ₽"]]
       [PricingTableRow {:hover-effect true}
        [:td {:class "px-6 py-5 text-slate-700 font-medium"} "Обновление старых версий"]
        [:td {:class "px-6 py-5 text-slate-600 text-sm"} "До актуальной версии"]
        [:td {:class "px-6 py-5 text-right font-bold text-slate-900"} "48 000 ₽"]]]]]

    ;; 2. МОДУЛИ
    [Accordion
     {:icon "puzzle-piece"
      :title "Дополнительные модули"
      :subtitle "Расширение функционала системы."}
     [Table
      [:thead
       [Tr
        [:th {:class "px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-left"} "Наименование"]
        [:th {:class "px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-left"} "Примечание"]
        [:th {:class "px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-right"} "Стоимость (руб./мес.)"]]]
      [:tbody
       (for [m modules]
         ^{:key (:name m)}
         [PricingTableRow {:hover-effect true}
          [:td {:class "px-6 py-5 text-slate-800 font-bold"} (:name m)]
          [:td {:class "px-6 py-5 text-slate-500"} (:note m)]
          [:td {:class "px-6 py-5 text-right font-bold text-slate-900"} (str (:price m) " ₽")]])]]]

    ;; 3. ПОДДЕРЖКА
    [Accordion
     {:icon "headset"
      :title "Техническая поддержка (Абонентская плата)"
      :subtitle "Стоимость за 1 лицензию в месяц."}
     [Table
      [:thead
       [Tr
        [:th {:class "px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-left"} "Количество лицензий"]
        [:th {:class "px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-right"} "Стоимость (руб./мес. за 1 лицензию)"]]]
      [:tbody
       (for [s support]
         ^{:key (:range s)}
         [PricingTableRow {:hover-effect true}
          [:td {:class "px-6 py-5 text-slate-700 font-medium"} (:range s)]
          [:td {:class "px-6 py-5 text-right font-bold text-slate-900"} (str (:price s) " ₽")]])]]]

    ;; 4. УСЛУГИ
    [Accordion
     {:icon "wrench"
      :title "Услуги и Настройка"
      :subtitle "Работы по обслуживанию и индивидуальной конфигурации."}
     [:div {:class "p-6 bg-slate-50"}
      [:div {:class "grid grid-cols-1 md:grid-cols-2 gap-4"}
       (for [item services]
         ^{:key (:name item)}
         [CardServiceItem item])]]]
    [TextSm {:class "text-center mb-10 text-slate-400"} "* Для клиентов с договорами АБ до 31.12.2023 действуют архивные тарифы: от 6050 до 15000 руб./мес. за объект."]]])

;; Contacts Section
(defn ContactsSection [{:keys [data]}]
  (let [{:keys [title address phones email]} data]
    [SectionGray {:id "contacts" :class "scroll-mt-24"}
     [Container
      [H2 title]
      [Card {:class "max-w-xl mx-auto text-center py-12"}
       [TextXL {:class "mb-8 font-medium flex flex-col items-center gap-3"}
        [:div {:class "text-blue-600"} [Icon {:name "map-pin" :class "text-5xl"}]]
        address]
       [:div {:class "space-y-4 text-lg"}
        (for [phone phones]
          ^{:key phone}
          [:div {:class "flex items-center justify-center gap-3"}
           [Icon {:name "phone" :class "text-slate-400 text-xl"}]
           phone])
        [:div {:class "flex items-center justify-center gap-3 text-blue-600 font-medium"}
         [Icon {:name "envelope" :class "text-xl"}]
         email]]]]]))

;; Navbar
(defn Navbar [{:keys [on-nav-click]}]
  [NavContainer
   [Container
    [:div {:class "flex justify-between items-center h-16"}
     [:div {:class "font-extrabold text-2xl text-blue-600 tracking-tight cursor-pointer"
            :on-click #(on-nav-click "home")}
      "ВЕТСОФТ"]
     [:div {:class "hidden md:flex space-x-1"}
      [NavLink {:on-click #(on-nav-click "home")} "Главная"]
      [NavLink {:on-click #(on-nav-click "modules")} "Модули"]
      [NavLink {:on-click #(on-nav-click "prices")} "Цена"]
      [NavLink {:on-click #(on-nav-click "contacts")} "Контакты"]]]]])

;; Footer
(defn Footer [{:keys [data]}]
  (let [{:keys [company copyright]} data]
    [:footer {:class "bg-slate-900 text-slate-400 py-12 text-center text-sm border-t border-slate-800"}
     [Container
      [:div {:class "font-bold text-lg text-slate-200 mb-4"} company]
      copyright]]))