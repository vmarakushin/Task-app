package accounting_service.kafka;

import accounting_service.data.model.Payment;
import events.Topics;
import events.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AccountingEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, PaymentEvent>> sendPaymentProcessed(Payment payment) {
        return kafkaTemplate.send(
                Topics.PAYMENT_STREAM,
                String.valueOf(payment.getId()),
                new PaymentEvent(
                        payment.getId(),
                        payment.getTaskId(),
                        payment.getWorkerId(),
                        payment.getAmount(),
                        payment.getCreatedAt()
                ));
    }
}
