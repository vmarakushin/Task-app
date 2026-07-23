# Task Service

> **Версия:** 5.0

Task Service — микросервисное backend-приложение для управления задачами, разработанное на **Spring Boot**. Проект демонстрирует построение современной backend-архитектуры с использованием **Kafka**, **Keycloak**, **PostgreSQL**, **Docker** и **Testcontainers**.

## Возможности

- REST API
- JWT-аутентификация и авторизация (Keycloak)
- Управление задачами
- Хранение данных в PostgreSQL
- Миграции базы данных с Flyway
- Асинхронное взаимодействие сервисов через Kafka
- Docker Compose окружение
- Интеграционные тесты с Testcontainers
- API Gateway
- Микросервисная архитектура

---

# Технологический стек

| Категория | Технологии |
|-----------|------------|
| Язык | Java 21 / Kotlin |
| Framework | Spring Boot 3 |
| Data | Spring Data JPA, PostgreSQL, Flyway |
| Security | Spring Security, OAuth2, Keycloak |
| Messaging | Apache Kafka (KRaft) |
| Infrastructure | Docker, Docker Compose |
| Testing | JUnit 5, MockMvc, Testcontainers |

---

# Архитектура сервисов

Проект состоит из нескольких микросервисов:

| Сервис | Назначение |
|---------|------------|
| **API Gateway** | Единая точка входа в систему, маршрутизация запросов, проверка JWT |
| **Auth Service** | Регистрация, авторизация пользователей, интеграция с Keycloak |
| **Task Service** | Управление жизненным циклом задач |
| **Accounting Service** | Начисление выплат за выполненные задачи |
| **Search Service** | Хранение поисковой модели и нечеткий поиск работников |

---

# Функциональные возможности

### Task Service

- получение списка задач с пагинацией;
- получение задачи по идентификатору;
- создание новой задачи;
- назначение исполнителя;
- изменение статуса задачи.

### Accounting Service

- хранение балансов работников;
- автоматическое начисление выплат за завершённые задачи;
- выполнение начислений по расписанию (раз в две недели).

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
docker compose up -d
```

После запуска необходимо выдать клиенту **auth-service** роль:

- **Realm:** `demo`
- **Client:** `auth-service`
- **Service Account Role:** `manage-users`

---

# Порты

| Сервис | Порт |
|---------|-----:|
| API Gateway | **8080** |
| Keycloak Admin Console | **8082** |

---

# API

Все запросы выполняются через **API Gateway**.

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

> **Авторизация:** Все endpoint'ы, кроме регистрации и авторизации, требуют JWT Access Token.
>
> После успешного входа необходимо передавать заголовок:
>
> ```http
> Authorization: Bearer <access_token>
> ```

---

# История версий

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