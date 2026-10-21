-- Опорная дата графика выплат (первая платёжная пятница), всегда одна строка.

CREATE TABLE payout_schedule (
    id           SMALLINT      PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    anchor_date  DATE          NOT NULL
);
