CREATE TABLE workers (
                       id         BIGSERIAL     PRIMARY KEY,
                       name       VARCHAR(255)  NOT NULL,
                       email      VARCHAR(255)  UNIQUE NOT NULL
);

CREATE TABLE tasks (
                       id          BIGSERIAL    PRIMARY KEY,
                       title       VARCHAR(255) NOT NULL,
                       description TEXT,
                       status      VARCHAR(50)  NOT NULL,
                       worker_id   BIGINT,

                       CONSTRAINT fk_tasks_worker
                           FOREIGN KEY (worker_id)
                               REFERENCES workers(id)
                               ON DELETE SET NULL
);
