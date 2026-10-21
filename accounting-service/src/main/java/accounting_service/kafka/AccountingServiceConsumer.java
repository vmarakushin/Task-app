package accounting_service.kafka;

import accounting_service.service.AccountingKafkaHandler;
import events.Topics;
import events.task.TaskFinishedEvent;
import events.task.WorkerAssignedEvent;
import events.user.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountingServiceConsumer {

    private final AccountingKafkaHandler handler;

    @KafkaListener(topics = Topics.USER_FLOW)
    public void consume(UserCreatedEvent event) {
        handler.handleUserCreated(event);
    }

    @KafkaListener(topics = Topics.TASK_ASSIGNED)
    public void consume(WorkerAssignedEvent event) {
        handler.handleWorkerAssigned(event);
    }

    @KafkaListener(topics = Topics.TASK_FINISHED_FLOW)
    public void consume(TaskFinishedEvent event) {
        handler.handleTaskFinished(event);
    }
}
