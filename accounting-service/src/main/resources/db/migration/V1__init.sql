CREATE TABLE balance (
    worker_id    UUID          PRIMARY KEY,
    amount       NUMERIC(19,2) NOT NULL DEFAULT 0
);

CREATE TABLE job (
    task_id      BIGINT        PRIMARY KEY,
    worker_id    UUID          NOT NULL,
    paid         BOOLEAN       NOT NULL DEFAULT FALSE,
    finished     BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE TABLE payment (
    id           BIGSERIAL     PRIMARY KEY,
    task_id      BIGINT        NOT NULL UNIQUE,
    worker_id    UUID          NOT NULL,
    amount       NUMERIC(19,2) NOT NULL,
    processed    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_unprocessed_created_at
    ON payment(created_at)
    WHERE processed = FALSE;
