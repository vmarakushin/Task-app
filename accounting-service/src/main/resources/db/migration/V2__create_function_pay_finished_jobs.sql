CREATE
OR REPLACE FUNCTION pay_finished_jobs(
    payment_amount NUMERIC(19,2)
)
RETURNS INTEGER
LANGUAGE plpgsql
AS
$$
DECLARE
payment_count INTEGER;
BEGIN
WITH tasks_to_pay AS (SELECT task_id, worker_id
                      FROM job
                      WHERE finished = TRUE
                      AND paid = FALSE),
updated_balances  AS (UPDATE balance b
                      SET amount = b.amount + payment_amount
                      FROM tasks_to_pay t
                      WHERE b.worker_id = t.worker_id
                      RETURNING t.task_id, t.worker_id),
created_payments  AS (INSERT INTO payment(task_id, worker_id, amount)
                      SELECT task_id, worker_id, payment_amount
                      FROM updated_balances
                      RETURNING task_id)
UPDATE job j
SET paid = TRUE
FROM created_payments p
WHERE j.task_id = p.task_id;

GET DIAGNOSTICS payment_count = ROW_COUNT;

RETURN payment_count;
END;
$$;
