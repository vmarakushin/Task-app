package search_service.kafka;

import accounting_service.data.dto.PaymentEvent;
import auth_service.event.user.UserStreamEvent;
import search_service.repository.BalanceRepository;
import auth_service.event.user.UserCreatedFlowEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import search_service.repository.PaymentRepository;
import search_service.repository.TaskRepository;
import search_service.repository.WorkerRepository;
import task_service.data.dto.TaskStreamEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchKafkaHandler {

    private final BalanceRepository balanceRepository;

    private final PaymentRepository paymentRepository;

    private final WorkerRepository workerRepository;

    private final TaskRepository taskRepository;

    @Transactional
    public void createBalance(UserCreatedFlowEvent event) {
        if (balanceRepository.createIfAbsent(event.id()) != 1) {
            log.warn("Duplicate UserCreatedFlowEvent, user: {}", event.id());
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
                        event.id(),
                        event.title(),
                        event.description(),
                        event.status().name(),
                        event.executor() != null ? event.executor().getId() : null
        );
    }
}
