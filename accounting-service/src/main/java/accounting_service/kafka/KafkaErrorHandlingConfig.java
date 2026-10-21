package accounting_service.kafka;

import events.Topics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Повторы с экспоненциальной задержкой, затем отправка записи в DLT {@code <topic>.<group>.DLT}. */
@Slf4j
@Configuration
public class KafkaErrorHandlingConfig {

    private static final List<String> CONSUMED_TOPICS = List.of(
            Topics.USER_FLOW,
            Topics.TASK_ASSIGNED,
            Topics.TASK_FINISHED_FLOW
    );

    private static final int MAX_RETRIES = 4;

    private final String groupId;

    public KafkaErrorHandlingConfig(@Value("${spring.kafka.consumer.group-id}") String groupId) {
        this.groupId = groupId;
    }

    @Bean
    public KafkaAdmin.NewTopics deadLetterTopics() {
        return new KafkaAdmin.NewTopics(CONSUMED_TOPICS.stream()
                .map(topic -> TopicBuilder.name(Topics.deadLetter(topic, groupId))
                        .partitions(1)
                        .replicas(1)
                        .build())
                .toArray(NewTopic[]::new));
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(ProducerFactory<Object, Object> producerFactory) {
        DefaultErrorHandler handler = new DefaultErrorHandler(deadLetterRecoverer(producerFactory), backOff());

        handler.addNotRetryableExceptions(DataIntegrityViolationException.class);
        return handler;
    }

    private ConsumerRecordRecoverer deadLetterRecoverer(ProducerFactory<Object, Object> producerFactory) {
        Map<Class<?>, KafkaOperations<? extends Object, ? extends Object>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, new KafkaTemplate<>(producerFactory,
                Map.<String, Object>of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class)));
        templates.put(Object.class, new KafkaTemplate<>(producerFactory));

        DeadLetterPublishingRecoverer publisher = new DeadLetterPublishingRecoverer(templates,
                (record, ex) -> new TopicPartition(Topics.deadLetter(record.topic(), groupId), -1));

        return (record, ex) -> {
            log.error("Kafka record sent to DLT: topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key(), ex);
            publisher.accept(record, ex);
        };
    }

    private ExponentialBackOffWithMaxRetries backOff() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(MAX_RETRIES);
        backOff.setInitialInterval(1_000);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000);
        return backOff;
    }
}
