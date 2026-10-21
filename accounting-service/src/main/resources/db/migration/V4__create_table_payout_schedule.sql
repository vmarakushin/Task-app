-- Опорная дата графика выплат: первая платёжная пятница.
-- Следующие выплаты - каждые 14 дней от неё.
-- Таблица всегда содержит не больше одной строки (id = 1).
CREATE TABLE payout_schedule (
    id           SMALLINT      PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    anchor_date  DATE          NOT NULL
);
