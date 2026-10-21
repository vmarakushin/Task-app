package accounting_service.integration;

import accounting_service.AbstractIntegrationTest;
import accounting_service.data.model.Job;
import accounting_service.repository.BalanceRepository;
import accounting_service.repository.JobRepository;
import accounting_service.repository.PaymentRepository;
import accounting_service.service.AccountingKafkaHandler;
import accounting_service.service.PaymentService;
import events.task.TaskFinishedEvent;
import events.task.WorkerAssignedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task-assigned и task-finished-flow - разные топики, Kafka не гарантирует порядок между ними.
 */
@SpringBootTest
public class EventOrderIT extends AbstractIntegrationTest {

    private final UUID worker1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID worker2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private AccountingKafkaHandler handler;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        jobRepository.deleteAll();
        balanceRepository.deleteAll();
    }

    @Test
    @DisplayName("finished раньше assigned: задача всё равно будет оплачена")
    void finishedBeforeAssigned_shouldBePaid() {
        handler.handleTaskFinished(new TaskFinishedEvent(1L, worker1, Instant.now()));
        handler.handleWorkerAssigned(new WorkerAssignedEvent(1L, worker1));

        paymentService.processPayments();

        Job job = jobRepository.findById(1L).orElseThrow();
        assertTrue(job.isFinished());
        assertTrue(job.isPaid());
        assertEquals(new BigDecimal("1000.00"), balanceRepository.findById(worker1).orElseThrow().getAmount());
    }

    @Test
    @DisplayName("Опоздавший assigned не меняет исполнителя завершённой задачи")
    void lateAssigned_shouldNotChangeFinishedJob() {
        handler.handleTaskFinished(new TaskFinishedEvent(2L, worker1, Instant.now()));
        handler.handleWorkerAssigned(new WorkerAssignedEvent(2L, worker2));

        assertEquals(worker1, jobRepository.findById(2L).orElseThrow().getWorkerId());
    }

    @Test
    @DisplayName("Платит тому, кто завершил задачу, даже если её переназначали")
    void reassignedTask_shouldPayFinisher() {
        handler.handleWorkerAssigned(new WorkerAssignedEvent(3L, worker2));
        handler.handleTaskFinished(new TaskFinishedEvent(3L, worker1, Instant.now()));

        paymentService.processPayments();

        assertEquals(new BigDecimal("1000.00"), balanceRepository.findById(worker1).orElseThrow().getAmount());
        assertTrue(balanceRepository.findById(worker2).isEmpty());
    }

    @Test
    @DisplayName("Повторный finished ничего не меняет")
    void duplicateFinished_shouldBeIgnored() {
        handler.handleTaskFinished(new TaskFinishedEvent(4L, worker1, Instant.now()));
        paymentService.processPayments();
        handler.handleTaskFinished(new TaskFinishedEvent(4L, worker1, Instant.now()));
        paymentService.processPayments();

        assertEquals(1, paymentRepository.count());
        assertEquals(new BigDecimal("1000.00"), balanceRepository.findById(worker1).orElseThrow().getAmount());
    }
}
