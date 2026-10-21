package accounting_service.integration;

import accounting_service.AbstractIntegrationTest;
import accounting_service.repository.BalanceRepository;
import accounting_service.repository.JobRepository;
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
import org.junit.jupiter.api.AfterEach;
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

/**
 * Своя consumer group: Spring держит в кеше контексты других тестовых классов, их листенеры
 * живы и сидят в группе accounting-service. Внутри одной группы Kafka отдала бы сообщение
 * любому из них, а не этому контексту со шпионом. В отдельной группе этот контекст
 * получает все сообщения сам, и DLT у него тоже свой.
 */
@SpringBootTest(properties = "spring.kafka.consumer.group-id=accounting-error-handling-it")
public class KafkaErrorHandlingIT extends AbstractIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private JobRepository jobRepository;

    @SpyBean
    private AccountingKafkaHandler handler;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /** Балансы и job создают хендлеры по событиям - убираем, чтобы не мешать другим тестовым классам. */
    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
        balanceRepository.deleteAll();
    }

    @Test
    @DisplayName("Временная ошибка: сообщение обрабатывается повторно и не теряется")
    void transientError_shouldBeRetried() throws Exception {
        UUID userId = UUID.randomUUID();

        doThrow(new RuntimeException("DB is temporarily down"))
                .doThrow(new RuntimeException("DB is temporarily down"))
                .doCallRealMethod()
                .when(handler).handleUserCreated(argThat(e -> userId.equals(e.id())));

        kafkaTemplate.send(Topics.USER_FLOW, userId.toString(), new UserCreatedEvent(userId, Instant.now())).get();

        // Ждём вызовов именно шпиона: баланс в общей БД мог бы создать и хендлер другого контекста
        await().atMost(TIMEOUT).untilAsserted(() ->
                verify(handler, times(3)).handleUserCreated(argThat(e -> userId.equals(e.id()))));
        assertTrue(balanceRepository.findById(userId).isPresent());
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
            // Следующее сообщение дошло до хендлера этого контекста - партиция не застряла
            await().atMost(TIMEOUT).untilAsserted(() ->
                    verify(handler).handleUserCreated(argThat(e -> nextUserId.equals(e.id()))));
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
