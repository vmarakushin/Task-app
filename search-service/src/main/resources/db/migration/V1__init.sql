-- search-service хранит read-модель (проекцию) данных других сервисов.
-- Целостность гарантирует источник, а события из разных топиков приходят
-- в произвольном порядке, поэтому внешних ключей между таблицами здесь нет.

CREATE TABLE workers (
    id          UUID          PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    email       VARCHAR(255)  UNIQUE NOT NULL
);

-- id приходит из task-service, поэтому BIGINT, а не BIGSERIAL.
-- worker_id без FOREIGN KEY: задача может прийти раньше, чем работник (user-stream).
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
