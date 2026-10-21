-- Начисляет выплату за каждую завершённую неоплаченную задачу и возвращает число платежей.
-- Баланс пополняется суммой по работнику (GROUP BY): UPDATE ... FROM обновил бы строку только один раз.

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
