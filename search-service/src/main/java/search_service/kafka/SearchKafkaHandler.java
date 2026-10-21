package search_service.kafka;

import events.payment.PaymentEvent;
import events.task.TaskStreamEvent;
import events.user.UserCreatedEvent;
import events.user.UserStreamEvent;
import search_service.repository.BalanceRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void handleSavePaymentToInbox(PaymentEvent event) {
        int saved = paymentRepository.saveIfNotExists(
                event.paymentId(),
                event.taskId(),
                event.workerId(),
                event.amount(),
                false,
                event.createdAt()
        );
        if (saved != 1) {
            log.warn("Duplicate PaymentEvent, user: {}", event.paymentId());
        }
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
