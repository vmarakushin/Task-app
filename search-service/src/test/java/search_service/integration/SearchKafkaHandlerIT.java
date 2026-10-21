package search_service.integration;

import events.payment.PaymentEvent;
import events.task.TaskStatus;
import events.task.TaskStreamEvent;
import events.user.UserCreatedEvent;
import events.user.UserStreamEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import search_service.AbstractIntegrationTest;
import search_service.data.dto.TaskResponse;
import search_service.kafka.SearchKafkaHandler;
import search_service.repository.BalanceRepository;
import search_service.repository.PaymentRepository;
import search_service.repository.TaskRepository;
import search_service.repository.WorkerRepository;
import search_service.service.Messages;
import search_service.service.TaskService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SearchKafkaHandlerIT extends AbstractIntegrationTest {

    private static final BigDecimal AMOUNT = new BigDecimal("1000.00");

    private final UUID workerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private SearchKafkaHandler handler;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        workerRepository.deleteAll();
        paymentRepository.deleteAll();
        balanceRepository.deleteAll();
    }

    @Test
    @DisplayName("task-stream раньше user-stream: задача сохраняется, данные работника подтягиваются потом")
    void taskBeforeWorker_shouldBeSaved() {
        handler.handleTaskStream(taskEvent(1L, workerId));

        TaskResponse beforeSync = taskService.findById(1L);
        assertEquals(workerId, beforeSync.workerId());
        assertEquals(Messages.WORKER_NOT_SYNCED.text(), beforeSync.workerName());

        handler.handleUserStream(new UserStreamEvent(workerId, "Vlad", "Ivanov", "vlad@test.com"));

        TaskResponse afterSync = taskService.findById(1L);
        assertEquals("Vlad", afterSync.workerName());
        assertEquals("vlad@test.com", afterSync.workerEmail());
    }

    @Test
    @DisplayName("Задача без исполнителя")
    void taskWithoutWorker_shouldShowPlaceholder() {
        handler.handleTaskStream(taskEvent(2L, null));

        TaskResponse task = taskService.findById(2L);
        assertEquals(Messages.NO_WORKER.text(), task.workerName());
        assertEquals(1, taskService.getPage(0, 10).getTotalElements());
    }

    @Test
    @DisplayName("Платёж раньше user-flow: баланс создаётся сразу с суммой")
    void paymentBeforeBalance_shouldCreateBalance() {
        handler.handlePayment(paymentEvent(10L, 3L));
        handler.createBalance(new UserCreatedEvent(workerId, Instant.now()));

        assertEquals(AMOUNT, balanceOf(workerId));
    }

    @Test
    @DisplayName("Повторный PaymentEvent не начисляется второй раз")
    void duplicatePayment_shouldBeIgnored() {
        handler.createBalance(new UserCreatedEvent(workerId, Instant.now()));

        handler.handlePayment(paymentEvent(20L, 4L));
        handler.handlePayment(paymentEvent(20L, 4L));

        assertEquals(AMOUNT, balanceOf(workerId));
        assertEquals(1, paymentRepository.count());
    }

    private BigDecimal balanceOf(UUID id) {
        return balanceRepository.findById(id).orElseThrow().getAmount();
    }

    private static TaskStreamEvent taskEvent(Long taskId, UUID workerId) {
        return new TaskStreamEvent(taskId, "Task", "Description", TaskStatus.IN_PROGRESS,
                workerId, null, null);
    }

    private PaymentEvent paymentEvent(Long paymentId, Long taskId) {
        return new PaymentEvent(paymentId, taskId, workerId, AMOUNT, Instant.now());
    }
}
