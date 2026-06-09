package task_application.kafka;

import task_application.data.dto.TaskCreatedEvent;
import task_application.data.dto.WorkerAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final TaskEventProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskCreated(TaskCreatedEvent event) {

        producer.sendTaskCreated(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkerAssigned(WorkerAssignedEvent event) {

        producer.sendWorkerAssigned(event);
    }
}
