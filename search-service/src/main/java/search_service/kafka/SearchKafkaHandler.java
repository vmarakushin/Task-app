package search_service.kafka;

import events.payment.PaymentEvent;
import events.task.TaskStreamEvent;
import events.user.UserCreatedEvent;
import events.user.UserStreamEvent;
import search_service.repository.BalanceRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import search_service.repository.PaymentRepository;
import search_service.repository.TaskRepository;
import search_service.repository.WorkerRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchKafkaHandler {

    private final BalanceRepository balanceRepository;

    private final PaymentRepository paymentRepository;

    private final WorkerRepository workerRepository;

    private final TaskRepository taskRepository;

    @Transactional
    public void createBalance(UserCreatedEvent event) {
        if (balanceRepository.createIfAbsent(event.id()) != 1) {
            log.warn("Duplicate UserCreatedEvent, user: {}", event.id());
        }
    }

    /**
     * Inbox + начисление в одной транзакции: либо событие записано и деньги
     * начислены, либо ни то ни другое. Повторное событие отсекается по payment_id.
     */
    @Transactional
    public void handlePayment(PaymentEvent event) {
        int saved = paymentRepository.saveIfNotExists(
                event.paymentId(),
                event.taskId(),
                event.workerId(),
                event.amount(),
                event.createdAt()
        );
        if (saved == 0) {
            log.warn("Duplicate PaymentEvent, payment: {}", event.paymentId());
            return;
        }
        balanceRepository.credit(event.workerId(), event.amount());
    }

    @Transactional
    public void handleUserStream(UserStreamEvent event) {
        workerRepository.upsert(event.id(), event.firstName(), event.email());
    }

    @Transactional
    public void handleTaskStream(TaskStreamEvent event) {
        taskRepository.upsert(
                        event.taskId(),
                        event.title(),
                        event.description(),
                        event.status().name(),
                        event.workerId()
        );
    }
}
