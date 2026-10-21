# Task Service

> **Версия:** 5.1

Task Service — микросервисное backend-приложение для управления задачами, разработанное на **Spring Boot**. Проект демонстрирует построение современной backend-архитектуры с использованием **Kafka**, **Keycloak**, **PostgreSQL**, **Docker** и **Testcontainers**.

## Возможности

- REST API
- JWT-аутентификация и авторизация (Keycloak)
- Управление задачами
- Хранение данных в PostgreSQL
- Миграции базы данных с Flyway
- Асинхронное взаимодействие сервисов через Kafka
- Повторная обработка событий и dead letter topics
- Docker Compose окружение
- Интеграционные тесты с Testcontainers
- API Gateway
- Микросервисная архитектура

---

# Технологический стек

| Категория | Технологии |
|-----------|------------|
| Язык | Java 21 |
| Framework | Spring Boot 3, Spring Cloud Gateway |
| Data | Spring Data JPA, PostgreSQL, Flyway |
| Security | Spring Security, OAuth2 Resource Server, Keycloak |
| Messaging | Apache Kafka (KRaft) |
| Infrastructure | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, MockMvc, WebTestClient, Testcontainers |

---

# Архитектура сервисов

Проект состоит из нескольких микросервисов:

| Сервис | Назначение |
|---------|------------|
| **API Gateway** | Единая точка входа в систему, маршрутизация запросов, проверка JWT и роли `worker` |
| **Auth Service** | Регистрация, авторизация пользователей, интеграция с Keycloak |
| **Task Service** | Управление жизненным циклом задач |
| **Accounting Service** | Начисление выплат за выполненные задачи |
| **Search Service** | Хранение поисковой модели и нечеткий поиск работников |

Контракты событий Kafka и имена топиков вынесены в общий модуль **event-contracts**.

## Взаимодействие через Kafka

| Топик | Отправитель | Получатели |
|-------|-------------|------------|
| `user-flow` | Auth Service | Accounting Service, Search Service |
| `user-stream` | Auth Service | Task Service, Search Service |
| `task-flow` | Task Service | — |
| `task-stream` | Task Service | Search Service |
| `task-assigned` | Task Service | Accounting Service |
| `task-finished-flow` | Task Service | Accounting Service |
| `payment-stream` | Accounting Service | Search Service |

- Task Service публикует события после коммита транзакции.
- Auth Service публикует события регистрации в одной Kafka-транзакции; если публикация не удалась, пользователь удаляется из Keycloak, клиент получает `503`.
- Accounting Service отправляет платежи через outbox-таблицу `payment`.
- Обработчики событий идемпотентны и не зависят от порядка событий из разных топиков.
- При ошибке обработки событие повторяется 4 раза с задержкой 1, 2, 4, 8 секунд, затем попадает в топик `<топик>.<consumer-group>.DLT`.

Просмотр dead letter topic:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-flow.accounting-service.DLT \
  --from-beginning --property print.headers=true
```

---

# Функциональные возможности

### Task Service

- получение списка задач с пагинацией;
- получение задачи по идентификатору;
- создание новой задачи;
- назначение исполнителя;
- изменение статуса задачи;
- защита от одновременного изменения задачи (optimistic lock).

Допустимые переходы статусов:

| Из | В | Исполнитель |
|----|---|-------------|
| `NEW` | `IN_PROGRESS` | обязателен |
| `NEW` | `CANCELLED` | не обязателен |
| `IN_PROGRESS` | `FINISHED` | обязателен |
| `IN_PROGRESS` | `CANCELLED` | не обязателен |

Задачам в статусах `FINISHED` и `CANCELLED` нельзя менять статус и исполнителя.

### Accounting Service

- хранение балансов работников;
- автоматическое начисление выплат за каждую завершённую задачу;
- выполнение начислений через одну пятницу в 00:00 по часовому поясу `TZ`;
- первая выплата — в ближайшую пятницу после первого старта сервиса или в дату `ACCOUNTING_PAYOUT_ANCHOR_DATE`.

### Search Service

- получение работников;
- получение задач;
- получение балансов;
- нечеткий поиск работников по email.

---

# Запуск проекта

## Предварительные требования

- Docker
- Docker Compose
- Java 21

## Запуск

```bash
cp .env.example .env
docker compose up -d --build
```

Realm `demo` импортируется из `keycloak/realm/demo-realm.json` при первом запуске Keycloak. В нём настроены:

- клиент `auth-service` с ролью `manage-users` у service account;
- роль `worker`, которую получает каждый зарегистрированный пользователь.

Повторный импорт realm и пересоздание баз данных:

```bash
docker compose down -v
```

## Настройка

Все параметры задаются в `.env`, описание — в `.env.example`. Основные:

| Переменная | Назначение | По умолчанию |
|------------|------------|--------------|
| `ACCOUNTING_PAYMENT_AMOUNT` | Выплата за одну завершённую задачу | `1000.00` |
| `ACCOUNTING_PAYOUT_CRON` | Когда проверять наступление дня выплаты | `0 0 0 * * FRI` |
| `ACCOUNTING_PAYOUT_ANCHOR_DATE` | Дата первой выплаты (`yyyy-MM-dd`) | ближайшая пятница |
| `ACCOUNTING_OUTBOX_INTERVAL_SEC` | Интервал отправки платежей в Kafka, секунды | `10` |
| `KEYCLOAK_CLIENT_SECRET` | Секрет клиента `auth-service` в Keycloak | `auth-secret` |
| `TZ` | Часовой пояс расписания выплат | `UTC` |

## Тесты

```bash
./gradlew test
```

Для интеграционных тестов нужен запущенный Docker.

Сквозная проверка запущенной системы через API Gateway (PowerShell):

```powershell
.\scripts\smoke-test.ps1
```

---

# Порты

| Сервис | Порт |
|---------|-----:|
| API Gateway | **8080** |
| Keycloak Admin Console | **8082** |

Наружу публикуется только API Gateway. Порты остальных сервисов закомментированы в `docker-compose.yml`.

---

# API

Все запросы выполняются через **API Gateway**.

Параметры пагинации: `page` — от 0 (по умолчанию 0), `size` — от 1 до 100 (по умолчанию 20).

## Authentication Service

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/registration` | Регистрация пользователя |
| POST | `/login` | Авторизация пользователя |

---

## Task Service

### Управление задачами

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/tasks?page={page}&size={size}` | Получить страницу задач |
| GET | `/api/tasks/{taskId}` | Получить задачу по ID |
| POST | `/api/tasks` | Создать задачу |
| PATCH | `/api/tasks/{taskId}/executor?workerId={uuid}` | Назначить исполнителя |
| PATCH | `/api/tasks/{taskId}/status?status={NEW\|IN_PROGRESS\|FINISHED\|CANCELLED}` | Изменить статус задачи |

---

## Accounting Service

### Балансы

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/balance?page={page}&size={size}` | Получить страницу балансов |
| GET | `/api/balance/{workerId}` | Получить баланс работника |

---

## Search Service

### Работники

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/search/workers?page={page}&size={size}` | Получить страницу работников |
| GET | `/api/search/workers/{workerId}` | Получить работника по ID |
| GET | `/api/search/workers/search?query={email}` | Нечеткий поиск работников по email |

### Задачи

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/search/tasks?page={page}&size={size}` | Получить страницу задач |
| GET | `/api/search/tasks/{taskId}` | Получить задачу по ID |

### Балансы

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/search/balance?page={page}&size={size}` | Получить страницу балансов |
| GET | `/api/search/balance/{workerId}` | Получить баланс работника |

> **Авторизация:** Все endpoint'ы, кроме регистрации и авторизации, требуют JWT Access Token с ролью `worker`.
>
> После успешного входа необходимо передавать заголовок:
>
> ```http
> Authorization: Bearer <access_token>
> ```

---

## Коды ответов

| Код | Когда |
|-----|-------|
| `400` | Некорректные параметры или тело запроса |
| `401` | Нет токена или токен недействителен |
| `403` | У пользователя нет роли `worker` |
| `404` | Сущность не найдена |
| `409` | Недопустимый переход статуса, изменение закрытой задачи, одновременное изменение, занятый email |
| `502` | Ошибка Keycloak |
| `503` | Регистрация временно недоступна |

---

# Известные ограничения

- Данные Search Service, в том числе балансы, обновляются с задержкой; источник правды по балансам — Accounting Service.
- Если коммит Kafka-транзакции при регистрации завершился по таймауту, но был выполнен брокером, пользователь удаляется из Keycloak, а события остаются.
- Если сервис не работал в день выплаты, начисления выполняются в следующий день выплаты.

---

# История версий

## 5.1 — Рефакторинг
- Исправлено начисление выплат: оплачивается каждая завершённая задача.
- Выплаты по расписанию через одну пятницу.
- Повторная обработка событий Kafka и **dead letter topics**.
- Обработка событий не зависит от порядка их поступления.
- Компенсация регистрации в **Auth Service** при недоступности Kafka.
- Валидация запросов, правила переходов статусов, optimistic lock в **Task Service**.
- Проверка JWT в **API Gateway** без обращения к Keycloak, роль `worker`.
- Настройки и секреты вынесены в `.env`.
- Healthcheck'и и порядок запуска в Docker Compose, облегчённые Docker-образы.
- Общий модуль контрактов событий **event-contracts**.
- Удалены Kotlin и неиспользуемые зависимости.

## 5.0
- Добавлен **Accounting Service** с автоматическим начислением выплат за завершённые задачи.
- Добавлен **Search Service**, агрегирующий данные из системы и предоставляющий API нечеткого поиска по email.

## 3.0.1
- Рефакторинг и исправление ошибок.

## 3.0
- Интеграция с **Keycloak**.
- Проверка JWT вынесена в **API Gateway**.

## 2.0
- Добавлен сервис авторизации.

## 1.0
- Первая рабочая версия проекта с базовым CRUD-функционалом.
