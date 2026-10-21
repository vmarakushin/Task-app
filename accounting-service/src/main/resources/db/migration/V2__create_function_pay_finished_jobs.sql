-- Начисляет выплату за каждую завершённую и ещё не оплаченную задачу.
-- Возвращает количество созданных платежей.
--
-- Все шаги выполняются одним SQL-выражением, поэтому атомарны:
--   1. paid_jobs        - помечаем задачи оплаченными и забираем их;
--   2. created_payments - по каждой задаче создаём платёж (outbox для Kafka);
--   3. credited         - прибавляем к балансу СУММУ платежей работника.
--
-- Почему не UPDATE balance ... FROM paid_jobs: если у работника несколько задач,
-- строка баланса совпадёт с несколькими строками paid_jobs, а PostgreSQL обновит
-- её только один раз (по одной произвольной из них). Поэтому сначала GROUP BY.
--
-- Баланс обновляется через upsert: если событие user-flow потерялось или ещё
-- не обработано, строка баланса будет создана здесь, и деньги не пропадут.
CREATE OR REPLACE FUNCTION pay_finished_jobs(payment_amount NUMERIC(19,2))
    RETURNS INTEGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    payment_count INTEGER;
BEGIN
    WITH paid_jobs AS (
        UPDATE job
        SET paid = TRUE
        WHERE finished
          AND NOT paid
        RETURNING task_id, worker_id
    ),
    created_payments AS (
        INSERT INTO payment (task_id, worker_id, amount)
        SELECT task_id, worker_id, payment_amount
        FROM paid_jobs
        RETURNING worker_id, amount
    ),
    credited AS (
        INSERT INTO balance (worker_id, amount)
        SELECT worker_id, SUM(amount)
        FROM created_payments
        GROUP BY worker_id
        ON CONFLICT (worker_id) DO UPDATE
            SET amount = balance.amount + EXCLUDED.amount
        RETURNING worker_id
    )
    SELECT count(*)
    INTO payment_count
    FROM created_payments;

    RETURN payment_count;
END;
$$;
