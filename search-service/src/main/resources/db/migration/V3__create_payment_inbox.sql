-- Inbox: журнал уже обработанных PaymentEvent.
-- Kafka доставляет сообщения "как минимум один раз", поэтому одно событие может
-- прийти повторно. Первичный ключ payment_id не даст начислить его дважды:
-- запись в inbox и начисление на баланс делаются в одной транзакции (SearchKafkaHandler).
CREATE TABLE inbox_payment_events (
    payment_id   BIGINT        PRIMARY KEY,
    task_id      BIGINT        NOT NULL UNIQUE,
    worker_id    UUID          NOT NULL,
    amount       NUMERIC(19,2) NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL,
    received_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);
