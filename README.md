# Task Service
### Версия 3.01

---
Task Service — backend приложение для управления задачами с поддержкой:
- REST API
- PostgreSQL (Flyway migrations)
- Kafka events
- Dockerized environment
- Integration tests with Testcontainers
- Authorization

---
# Стек

- Java 21 / Kotlin
- Spring Boot 3
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Flyway
- Apache Kafka (KRaft mode)
- Docker / Docker Compose
- Testcontainers
- JUnit 5 / MockMvc
- Spring Oauth + Security
- Keycloak

# Функционал
- получение задач с пагинацией
- получение задачи по id
- добавление задачи
- назначение исполнителя задаче
- смена статуса задаче

# Отправка событий в Kafka
- Создание задачи - task-stream и task-flow топики
- Назначение исполнителя - task-assigned топик
- Создание юзера - user-stream и user-flow
---
# Запуск
- Запустите docker-compose в корне проекта
- Выдайте клиенту "auth-service" в realm "demo" account service role "manage users"

# Порты
## 8080 - основное приложение
## 8082 - keycloak панель(admin/admin)
# API
### POST /registration - Регистрация
{
"email":"vasilisk@mail.ru",
"password":"K9#vT2!mQ8@xL7$wR4^nP1&z",
"firstName":"Djedai",
"lastName":"Vasilyev"
}
### POST /login - Логин
{
"email":"vasilisk@mail.ru",
"password":"K9#vT2!mQ8@xL7$wR4^nP1&z"
}
### /api/tasks/ - API задач
POST 
{
"title": "NewOne",
"description": "somedescription"
}

# История версий
### 3.01
- Доведено до ума, работает (у меня)
### 3.0
- В качестве провайдера авторизации подключен Keycloak
- Проверка авторизации выведена на Api Gateway

### 2.0
- Добавлен модуль авторизации
### 1.0
- Первая версия с реализацией базового функционала