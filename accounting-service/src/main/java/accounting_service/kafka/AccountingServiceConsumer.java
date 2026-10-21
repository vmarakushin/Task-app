package accounting_service.kafka;

import accounting_service.service.AccountingKafkaHandler;
import events.Topics;
import events.task.TaskFinishedEvent;
import events.task.WorkerAssignedEvent;
import events.user.UserCreatedEvent;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountingServiceConsumer {

    private final AccountingKafkaHandler handler;

    @KafkaListener(topics = Topics.USER_FLOW)
    public void consume(UserCreatedEvent event, Acknowledgment ack) {
        try {
            handler.handleUserCreated(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = Topics.TASK_ASSIGNED)
    public void consume(WorkerAssignedEvent event, Acknowledgment ack) {
        try {
            handler.handleWorkerAssigned(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = Topics.TASK_FINISHED_FLOW)
    public void consume(TaskFinishedEvent event, Acknowledgment ack) {
        try {
            handler.handleTaskFinished(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }
}
