package search_service.kafka;

import accounting_service.data.dto.PaymentEvent;
import auth_service.event.user.UserCreatedFlowEvent;
import auth_service.event.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import task_service.data.dto.TaskStreamEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchServiceConsumer {

    private final SearchKafkaHandler handler;

    @KafkaListener(topics = "user-flow")
    public void consume(UserCreatedFlowEvent event, Acknowledgment ack) {
        try {
            handler.createBalance(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = "payment-stream")
    public void consume(PaymentEvent event, Acknowledgment ack) {
        try {
            handler.handleSavePaymentToInbox(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = "user-stream")
    public void consume(UserStreamEvent event, Acknowledgment ack) {
        try {
            handler.handleUserStream(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = "task-stream")
    public void consume(TaskStreamEvent event, Acknowledgment ack) {
        try {
            handler.handleTaskStream(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }
}
