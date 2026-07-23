package accounting_service.service;

import accounting_service.repository.BalanceRepository;
import accounting_service.repository.JobRepository;
import auth_service.event.user.UserCreatedFlowEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import task_service.data.dto.TaskFinishedFlowEvent;
import task_service.data.dto.WorkerAssignedEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountingKafkaHandler {

    private final BalanceRepository balanceRepository;

    private final JobRepository jobRepository;

    @Transactional
    public void handleUserCreated(UserCreatedFlowEvent event) {
        if (balanceRepository.createIfAbsent(event.id()) != 1) {
            log.warn("Duplicate UserCreatedFlowEvent, user: {}", event.id());
        }
    }

    @Transactional
    public void handleWorkerAssigned(WorkerAssignedEvent event) {
        if (jobRepository.upsert(event.taskId(), event.workerId()) != 1) {
            log.warn("Duplicate WorkerAssignedEvent or an attempt to reassign finished job were occurred. Task ID: {}", event.taskId());
        }
    }

    @Transactional
    public void handleTaskFinished(TaskFinishedFlowEvent event) {
        if (jobRepository.finishIfNotFinished(event.id()) != 1) {
            log.warn("Job already finished or not found. Task ID: {}", event.id());
        }
    }
}
