-- Журнал обработанных PaymentEvent: повторная доставка отсекается по payment_id.

CREATE TABLE inbox_payment_events (
    payment_id   BIGINT        PRIMARY KEY,
    task_id      BIGINT        NOT NULL UNIQUE,
    worker_id    UUID          NOT NULL,
    amount       NUMERIC(19,2) NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL,
    received_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);
