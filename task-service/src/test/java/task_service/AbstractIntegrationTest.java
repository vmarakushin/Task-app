package task_service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton-контейнеры: запускаются один раз на всю JVM и живут до её завершения
 * (останавливает их Ryuk - служебный контейнер Testcontainers).
 *
 * Почему не @Testcontainers + @Container static: тогда контейнеры останавливаются
 * после каждого тестового класса, а Spring кеширует ApplicationContext между классами.
 * Следующий класс с такой же конфигурацией получал закешированный контекст
 * с портами уже остановленных контейнеров - "Connection refused" и таймауты.
 */
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("test_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static final KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
            );

    static {
        postgres.start();
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    public static KafkaContainer getKafka() {
        return kafka;
    }

    public static PostgreSQLContainer<?> getPostgres() {
        return postgres;
    }
}
