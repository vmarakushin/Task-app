package accounting_service.service;

import accounting_service.repository.BalanceRepository;
import accounting_service.repository.JobRepository;
import events.task.TaskFinishedEvent;
import events.task.WorkerAssignedEvent;
import events.user.UserCreatedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountingKafkaHandler {

    private final BalanceRepository balanceRepository;

    private final JobRepository jobRepository;

    @Transactional
    public void handleUserCreated(UserCreatedEvent event) {
        if (balanceRepository.createIfAbsent(event.id()) != 1) {
            log.warn("Duplicate UserCreatedEvent, user: {}", event.id());
        }
    }

    @Transactional
    public void handleWorkerAssigned(WorkerAssignedEvent event) {
        if (jobRepository.upsert(event.taskId(), event.workerId()) != 1) {
            log.warn("Duplicate WorkerAssignedEvent or an attempt to reassign finished job were occurred. Task ID: {}", event.taskId());
        }
    }

    @Transactional
    public void handleTaskFinished(TaskFinishedEvent event) {
        if (jobRepository.finish(event.taskId(), event.workerId()) != 1) {
            log.warn("Duplicate TaskFinishedEvent, job already finished. Task ID: {}", event.taskId());
        }
    }
}
