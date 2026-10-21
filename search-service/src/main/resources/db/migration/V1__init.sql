CREATE TABLE workers (
    id          UUID          PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    email       VARCHAR(255)  UNIQUE NOT NULL
);

-- Без внешнего ключа на workers: события из разных топиков приходят в произвольном порядке.
CREATE TABLE tasks (
    id          BIGINT        PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    description TEXT,
    status      VARCHAR(50)   NOT NULL,
    worker_id   UUID
);

CREATE TABLE balance (
    worker_id   UUID          PRIMARY KEY,
    amount      NUMERIC(19,2) NOT NULL DEFAULT 0
);
