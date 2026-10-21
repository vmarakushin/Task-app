package accounting_service.integration;

import accounting_service.AbstractIntegrationTest;
import accounting_service.data.model.Balance;
import accounting_service.data.model.Job;
import accounting_service.data.model.Payment;
import accounting_service.repository.BalanceRepository;
import accounting_service.repository.JobRepository;
import accounting_service.repository.PaymentRepository;
import accounting_service.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PayFinishedJobsIT extends AbstractIntegrationTest {

    private static final BigDecimal TASK_AMOUNT = new BigDecimal("1000.00");

    private final UUID worker1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID worker2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID worker3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        jobRepository.deleteAll();
        balanceRepository.deleteAll();
    }

    @Test
    @DisplayName("Выплата начисляется за каждую завершённую задачу, а не одну на работника")
    void processPayments_shouldPayEveryFinishedJob() {
        balanceRepository.save(balance(worker1));
        balanceRepository.save(balance(worker3));

        jobRepository.saveAll(List.of(
                job(1L, worker1, true, false),
                job(2L, worker1, true, false),
                job(3L, worker1, true, false),
                job(4L, worker1, true, true),   // уже оплачена ранее
                job(5L, worker3, false, false)  // не завершена
        ));

        paymentService.processPayments();

        assertEquals(new BigDecimal("3000.00"), amountOf(worker1));
        assertEquals(new BigDecimal("0.00"), amountOf(worker3));

        List<Payment> payments = paymentRepository.findAll();
        assertEquals(3, payments.size());
        assertTrue(payments.stream().allMatch(p -> p.getWorkerId().equals(worker1)));
        assertTrue(payments.stream().allMatch(p -> TASK_AMOUNT.equals(p.getAmount())));

        assertTrue(jobRepository.findById(1L).orElseThrow().isPaid());
        assertFalse(jobRepository.findById(5L).orElseThrow().isPaid());
    }

    @Test
    @DisplayName("Баланс создаётся, если user-flow ещё не обработан")
    void processPayments_shouldCreateMissingBalance() {
        jobRepository.save(job(10L, worker2, true, false));

        paymentService.processPayments();

        assertEquals(TASK_AMOUNT, amountOf(worker2));
    }

    @Test
    @DisplayName("Повторный запуск ничего не начисляет")
    void processPayments_shouldBeIdempotent() {
        balanceRepository.save(balance(worker1));
        jobRepository.saveAll(List.of(
                job(20L, worker1, true, false),
                job(21L, worker1, true, false)
        ));

        paymentService.processPayments();
        paymentService.processPayments();

        assertEquals(new BigDecimal("2000.00"), amountOf(worker1));
        assertEquals(2, paymentRepository.count());
    }

    private BigDecimal amountOf(UUID workerId) {
        return balanceRepository.findById(workerId).orElseThrow().getAmount();
    }

    private static Balance balance(UUID workerId) {
        return Balance.builder()
                .workerId(workerId)
                .amount(BigDecimal.ZERO)
                .build();
    }

    private static Job job(Long taskId, UUID workerId, boolean finished, boolean paid) {
        return Job.builder()
                .taskId(taskId)
                .workerId(workerId)
                .finished(finished)
                .paid(paid)
                .build();
    }
}
