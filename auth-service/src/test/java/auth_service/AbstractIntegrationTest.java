package auth_service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Kafka запускается один раз на JVM: закешированные Spring-контексты
 * разных тестовых классов должны ссылаться на один и тот же порт.
 * Первая транзакция создаёт {@code __transaction_state}: одна партиция вместо 50
 * и увеличенный {@code max.block.ms}, чтобы не упереться в таймаут продюсера.
 */
public abstract class AbstractIntegrationTest {

    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"))
                    .withEnv("KAFKA_TRANSACTION_STATE_LOG_NUM_PARTITIONS", "1");

    static {
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.properties.max.block.ms", () -> "30000");
    }

    public static KafkaContainer getKafka() {
        return kafka;
    }
}
