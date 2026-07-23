CREATE TABLE inbox_payment_events (
                                                    id           BIGSERIAL     PRIMARY KEY,
                                                    task_id      BIGSERIAL     NOT NULL UNIQUE,
                                                    worker_id    UUID          NOT NULL,
                                                    amount       NUMERIC(19,2) NOT NULL,
                                                    processed    BOOLEAN       NOT NULL DEFAULT FALSE,
                                                    created_at   TIMESTAMP     NOT NULL
    );



CREATE OR REPLACE FUNCTION update_worker_balance_on_payment()
RETURNS TRIGGER AS $$
DECLARE
updated_rows INT;
BEGIN

UPDATE balance
SET amount = amount + NEW.amount
WHERE worker_id = NEW.worker_id;

GET DIAGNOSTICS updated_rows = ROW_COUNT;

IF updated_rows > 0 THEN
UPDATE inbox_payment_events
SET processed = TRUE
WHERE id = NEW.id;
END IF;

RETURN NEW;
EXCEPTION
    WHEN OTHERS THEN
        RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_update_balance_on_payment
    AFTER INSERT ON inbox_payment_events
    FOR EACH ROW
    EXECUTE FUNCTION update_worker_balance_on_payment();
