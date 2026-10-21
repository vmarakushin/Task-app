package search_service.kafka;

import events.Topics;
import events.payment.PaymentEvent;
import events.task.TaskStreamEvent;
import events.user.UserCreatedEvent;
import events.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchServiceConsumer {

    private final SearchKafkaHandler handler;

    @KafkaListener(topics = Topics.USER_FLOW)
    public void consume(UserCreatedEvent event, Acknowledgment ack) {
        try {
            handler.createBalance(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = Topics.PAYMENT_STREAM)
    public void consume(PaymentEvent event, Acknowledgment ack) {
        try {
            handler.handleSavePaymentToInbox(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = Topics.USER_STREAM)
    public void consume(UserStreamEvent event, Acknowledgment ack) {
        try {
            handler.handleUserStream(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }

    @KafkaListener(topics = Topics.TASK_STREAM)
    public void consume(TaskStreamEvent event, Acknowledgment ack) {
        try {
            handler.handleTaskStream(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }
}
