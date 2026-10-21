package search_service.kafka;

import events.Topics;
import events.payment.PaymentEvent;
import events.task.TaskStreamEvent;
import events.user.UserCreatedEvent;
import events.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchServiceConsumer {

    private final SearchKafkaHandler handler;

    @KafkaListener(topics = Topics.USER_FLOW)
    public void consume(UserCreatedEvent event) {
        handler.createBalance(event);
    }

    @KafkaListener(topics = Topics.PAYMENT_STREAM)
    public void consume(PaymentEvent event) {
        handler.handlePayment(event);
    }

    @KafkaListener(topics = Topics.USER_STREAM)
    public void consume(UserStreamEvent event) {
        handler.handleUserStream(event);
    }

    @KafkaListener(topics = Topics.TASK_STREAM)
    public void consume(TaskStreamEvent event) {
        handler.handleTaskStream(event);
    }
}
