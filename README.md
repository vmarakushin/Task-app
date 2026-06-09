# Task Service
### Версия 2.0

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

# Функционал
- получение задач с пагинацией
- получение задачи по id
- добавление задачи
- назначение исполнителя задаче
- смена статуса задаче

# Отправка событий в Kafka
При создании задачи и назначении исполнителя происходит отправка события в task.created и task.assigned топики соответственно

---

# Запуск
Запустите docker-compose в корне проекта, на этом всё

# Порты
## 8080 - основное приложение
## 8081 - авторизация

# Swagger
После запуска на http://localhost:{port}/swagger-ui/index.html будет доступен Swagger UI

# История версий
### 2.0
- Добавлен модуль авторизации
### 1.0
- Первая версия с реализацией базового функционала