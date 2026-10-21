package task_service.kafka;

import events.task.TaskCreatedEvent;
import events.task.TaskFinishedEvent;
import events.task.TaskStreamEvent;
import events.task.WorkerAssignedEvent;
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
    public void onTaskEvent(TaskStreamEvent event) {
        producer.sendTaskStream(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkerAssigned(WorkerAssignedEvent event) {
        producer.sendWorkerAssigned(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskFinished(TaskFinishedEvent event) {
        producer.sendTaskFinished(event);
    }
}
