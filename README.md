# Task Service

Task Service — это backend приложение для управления задачами с поддержкой:
- REST API
- PostgreSQL (Flyway migrations)
- Kafka events
- Dockerized environment
- Integration tests with Testcontainers

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

# Функционал
- получение задач с пагинацией
- получение задачи по id
- добавление задачи
- назначение исполнителя задаче
- смена статуса задаче

# Запуск
Запустите docker-compose в корне проекта, на этом всё

# Swagger
После запуска на http://localhost:8080/swagger-ui/index.html будет доступен Swagger UI

# Отправка сообщений в Kafka
При создании задачи и назначении исполнителя происходит отправка события в task.created и task.assigned топики соответственно

# Тесты
Юнит-тесты для service слоя, интеграционные service -> db, service -> kafka, mockMvc -> controller -> db
(c)Лучше плохие тесты, чем без тестов