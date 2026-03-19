-- License tiers (from core.cljs lines 33-45)
INSERT INTO license_tiers (name, version, price, badge_text, badge_color, highlight, sort_order) VALUES
  ('ВЕТСОФТ ВЕТЕРИНАР - НОРМАЛ', 'Локальная (1 место)', 38200, NULL, NULL, FALSE, 0),
  ('ВЕТСОФТ ВЕТЕРИНАР - МАЛЫЙ БИЗНЕС', 'Сетевая (до 3-х подключений)', 49800, 'ПОПУЛЯРНОЕ', 'cyan', TRUE, 1),
  ('ВЕТСОФТ ВЕТЕРИНАР - УСПЕШНЫЙ', 'Сетевая (до 5-и подключений)', 84900, 'ХИТ', 'emerald', TRUE, 2),
  ('ВЕТСОФТ ВЕТЕРИНАР - СУПЕР', 'Сетевая (до 10-и подключений)', 195000, NULL, NULL, FALSE, 3);

--;;

-- Extra items (hardcoded in ui.cljs lines 287-294)
INSERT INTO extra_items (category, name, description, price, sort_order) VALUES
  ('workstation', 'Дополнительное рабочее место', '1 шт.', 19500, 0),
  ('upgrade', 'Обновление старых версий', 'До актуальной версии', 48000, 1);

--;;

-- Modules (from core.cljs lines 47-50)
INSERT INTO modules (name, note, price, sort_order) VALUES
  ('Модуль расчета заработной платы', 'Сетевая версия (для сети клиник)', 32700, 0),
  ('Модуль «Стационар»', 'Для одной клиники', 42000, 1),
  ('Модуль учета движения средств', 'Сетевая версия', 35000, 2);

--;;

-- Support rates (from core.cljs lines 52-57)
INSERT INTO support_rates (license_range, price, sort_order) VALUES
  ('от 1-й до 5-ти включительно', 1250, 0),
  ('свыше 5-ти до 10-ти', 1100, 1),
  ('свыше 10-ти до 20-ти', 1000, 2),
  ('свыше 20-ти до 30-ти', 900, 3),
  ('свыше 30-ти', 800, 4);

--;;

-- Services (from core.cljs lines 59-66)
INSERT INTO services (name, price, full_width, sort_order) VALUES
  ('Удаленная настройка 1 рабочего места', '5 000 ₽', FALSE, 0),
  ('Настройка конфигурации (мин. 2 филиала)', '12 500 ₽/ филиал', FALSE, 1),
  ('Создание отчета', 'от 5 000 ₽/шт', FALSE, 2),
  ('Разработка бланка (до 30 полей)', 'от 3 000 ₽/шт', FALSE, 3),
  ('Сборка печатной формы', 'от 3 000 ₽/шт', FALSE, 4),
  ('Вызов специалиста (только Воронеж)', '3 000 ₽', FALSE, 5),
  ('Работы в Москве / Санкт-Петербурге', 'договорная', TRUE, 6);

--;;

-- Price metadata
INSERT INTO price_metadata (key, value) VALUES
  ('valid_date', '15.12.2024'),
  ('archive_note', 'Для клиентов с договорами АБ до 31.12.2023 действуют архивные тарифы: от 6050 до 15000 руб./мес. за объект.');
