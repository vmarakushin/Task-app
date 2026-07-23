package accounting_service.kafka;

import accounting_service.service.AccountingKafkaHandler;
import auth_service.event.user.UserCreatedFlowEvent;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import task_service.data.dto.TaskFinishedFlowEvent;
import task_service.data.dto.WorkerAssignedEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountingServiceConsumer {

    private final AccountingKafkaHandler handler;

    @KafkaListener(topics = "user-flow")
    public void consume(UserCreatedFlowEvent event, Acknowledgment ack) {
        try {
            handler.handleUserCreated(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = "task-assigned")
    public void consume(WorkerAssignedEvent event, Acknowledgment ack) {
        try {
            handler.handleWorkerAssigned(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = "task-finished-flow")
    public void consume(TaskFinishedFlowEvent event, Acknowledgment ack) {
        try {
            handler.handleTaskFinished(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }
}
