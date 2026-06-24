package task_service.kafka;

import task_service.data.dto.TaskCreatedFlowEvent;
import task_service.data.dto.TaskStreamEvent;
import task_service.data.dto.WorkerAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final TaskEventProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskCreated(TaskCreatedFlowEvent event) {
        producer.sendTaskCreated(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskCreated(TaskStreamEvent event) {
        producer.sendTaskCreated(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkerAssigned(WorkerAssignedEvent event) {
        producer.sendWorkerAssigned(event);
    }
}
