package auth_service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * У auth-service нет своей БД - для тестов нужна только Kafka.
 *
 * Singleton-контейнер: запускается один раз на всю JVM. С @Testcontainers + @Container
 * он останавливался бы после каждого тестового класса, а закешированный Spring-контекст
 * следующего класса продолжал бы ходить на порт уже остановленного контейнера.
 */
public abstract class AbstractIntegrationTest {

    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    static {
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    public static KafkaContainer getKafka() {
        return kafka;
    }
}
