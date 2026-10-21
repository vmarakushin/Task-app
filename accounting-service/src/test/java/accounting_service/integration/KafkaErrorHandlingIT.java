package accounting_service.integration;

import accounting_service.AbstractIntegrationTest;
import accounting_service.repository.BalanceRepository;
import accounting_service.service.AccountingKafkaHandler;
import events.Topics;
import events.task.WorkerAssignedEvent;
import events.user.UserCreatedEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class KafkaErrorHandlingIT extends AbstractIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private BalanceRepository balanceRepository;

    @SpyBean
    private AccountingKafkaHandler handler;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Test
    @DisplayName("Временная ошибка: сообщение обрабатывается повторно и не теряется")
    void transientError_shouldBeRetried() throws Exception {
        UUID userId = UUID.randomUUID();

        doThrow(new RuntimeException("DB is temporarily down"))
                .doThrow(new RuntimeException("DB is temporarily down"))
                .doCallRealMethod()
                .when(handler).handleUserCreated(argThat(e -> userId.equals(e.id())));

        kafkaTemplate.send(Topics.USER_FLOW, userId.toString(), new UserCreatedEvent(userId, Instant.now())).get();

        await().atMost(TIMEOUT).until(() -> balanceRepository.findById(userId).isPresent());
        verify(handler, times(3)).handleUserCreated(argThat(e -> userId.equals(e.id())));
    }

    @Test
    @DisplayName("Ошибка, которую повтор не исправит: сразу в DLT, без повторов")
    void notRetryableError_shouldGoToDltImmediately() throws Exception {
        long taskId = 999_001L;

        doThrow(new DataIntegrityViolationException("constraint violated"))
                .when(handler).handleWorkerAssigned(argThat(e -> e.taskId() == taskId));

        try (Consumer<String, String> dlt = deadLetterConsumer(Topics.TASK_ASSIGNED)) {
            kafkaTemplate.send(Topics.TASK_ASSIGNED, String.valueOf(taskId),
                    new WorkerAssignedEvent(taskId, UUID.randomUUID())).get();

            ConsumerRecord<String, String> record = awaitRecord(dlt, String.valueOf(taskId));

            assertEquals(Topics.TASK_ASSIGNED, header(record, KafkaHeaders.DLT_ORIGINAL_TOPIC));
            verify(handler, times(1)).handleWorkerAssigned(argThat(e -> e.taskId() == taskId));
        }
    }

    @Test
    @DisplayName("Битый JSON уходит в DLT как есть, следующие сообщения обрабатываются")
    void poisonPill_shouldGoToDltAndNotBlockPartition() throws Exception {
        String poisonKey = "poison-" + UUID.randomUUID();
        String poison = "{this is not json";
        UUID nextUserId = UUID.randomUUID();

        try (Consumer<String, String> dlt = deadLetterConsumer(Topics.USER_FLOW);
             KafkaProducer<String, String> raw = rawProducer()) {

            raw.send(new ProducerRecord<>(Topics.USER_FLOW, poisonKey, poison)).get();
            kafkaTemplate.send(Topics.USER_FLOW, nextUserId.toString(),
                    new UserCreatedEvent(nextUserId, Instant.now())).get();

            ConsumerRecord<String, String> record = awaitRecord(dlt, poisonKey);

            assertEquals(poison, record.value());
            assertTrue(header(record, KafkaHeaders.DLT_EXCEPTION_FQCN).contains("Exception"));
            await().atMost(TIMEOUT).until(() -> balanceRepository.findById(nextUserId).isPresent());
        }
    }

    private Consumer<String, String> deadLetterConsumer(String sourceTopic) {
        Consumer<String, String> consumer = new KafkaConsumer<>(Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafka().getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "dlt-reader-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        ), new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(List.of(Topics.deadLetter(sourceTopic, groupId)));
        return consumer;
    }

    private KafkaProducer<String, String> rawProducer() {
        return new KafkaProducer<>(Map.<String, Object>of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafka().getBootstrapServers()
        ), new StringSerializer(), new StringSerializer());
    }

    private static ConsumerRecord<String, String> awaitRecord(Consumer<String, String> consumer, String key) {
        long deadline = System.currentTimeMillis() + TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                if (key.equals(record.key())) {
                    return record;
                }
            }
        }
        throw new AssertionError("No record with key " + key + " in DLT within " + TIMEOUT);
    }

    private static String header(ConsumerRecord<?, ?> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? "" : new String(header.value(), StandardCharsets.UTF_8);
    }
}
