package auth_service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * У auth-service нет своей БД - для тестов нужна только Kafka.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse(
                            "apache/kafka:3.7.0"
                    )
            );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );
    }

    public static KafkaContainer getKafka() {
        return kafka;
    }
}
