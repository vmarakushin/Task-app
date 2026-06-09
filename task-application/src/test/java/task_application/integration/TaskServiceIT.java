package task_application.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import task_application.AbstractIntegrationTest;
import task_application.data.dto.TaskCreateRequest;
import task_application.data.dto.TaskCreatedEvent;
import task_application.data.dto.TaskResponse;
import task_application.data.model.Status;
import task_application.data.model.Task;
import task_application.data.model.Worker;
import task_application.repository.TaskRepository;
import task_application.repository.WorkerRepository;
import task_application.service.TaskService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceIT extends AbstractIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WorkerRepository workerRepository;

    private Worker worker;

    private Task taskNew;

    private Task taskFinished;

    private static Consumer<String, TaskCreatedEvent> consumer;

    @BeforeAll
    static void setupConsumer() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafka().getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        consumer = new DefaultKafkaConsumerFactory<String, TaskCreatedEvent>(props).createConsumer();

        consumer.subscribe(List.of("task.created"));
    }

    @BeforeEach
    void setUp() {

        worker = workerRepository.save(
                Worker.builder()
                        .name("Stas")
                        .email("stas@gmail.com")
                        .build()
        );

        taskNew = taskRepository.save(
                Task.builder()
                        .title("Need to be fixed")
                        .description("Exactly this stuff must be fixed")
                        .status(Status.NEW)
                        .executor(worker)
                        .build()
        );

        taskFinished = taskRepository.save(
                Task.builder()
                        .title("Another title")
                        .description("Another description")
                        .status(Status.FINISHED)
                        .executor(worker)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        workerRepository.deleteAll();
    }

    @Test
    @DisplayName("Проверка создания задачи")
    void create_shouldPersistTask() {

        TaskCreateRequest request =
                new TaskCreateRequest(
                        "Created task",
                        "Created desc"
                );

        TaskResponse response = taskService.create(request);

        assertEquals("Created task", response.title());
        assertEquals("Created desc", response.description());
        assertEquals(Status.NEW, response.status());

        assertEquals(3, taskRepository.count());
    }

    @Test
    @DisplayName("Проверка получения задачи")
    void findById_shouldReturnExistingTask() {

        TaskResponse response =
                taskService.findById(taskNew.getId());

        assertEquals(taskNew.getId(), response.id());
        assertEquals(taskNew.getTitle(), response.title());
    }

    @Test
    @DisplayName("Проверка обновления исполнителя")
    void setExecutor_shouldAssignExistingWorker() {

        TaskResponse response =
                taskService.setExecutor(
                        taskNew.getId(),
                        worker.getId()
                );

        assertEquals(worker.getId(), response.workerId());
        assertEquals(worker.getName(), response.workerName());
    }

    @Test
    @DisplayName("Проверка обновления статуса")
    void setStatus_shouldChangeStatusSuccessfully() {

        TaskResponse response =
                taskService.setStatus(
                        taskNew.getId(),
                        Status.IN_PROGRESS
                );

        assertEquals(Status.IN_PROGRESS, response.status());
    }

    @Test
    @DisplayName("Проверка получения задач с пагинацией")
    void getPage_shouldReturnSeededTasks() {

        var page = taskService.getPage(0, 10);

        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("Проверка Kafka")
    void kafka_should_receive_event() {

        taskService.create(
                new TaskCreateRequest("Task", "Desc")
        );

        ConsumerRecords<String, TaskCreatedEvent> records =
                consumer.poll(Duration.ofSeconds(10));

        assertFalse(records.isEmpty());

        TaskCreatedEvent event = records.iterator().next().value();

        assertEquals("Task", event.title());
        assertEquals("Desc", event.description());
    }
}
