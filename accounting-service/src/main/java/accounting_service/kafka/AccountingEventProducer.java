package accounting_service.kafka;

import accounting_service.data.dto.PaymentEvent;
import accounting_service.data.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AccountingEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private final String PAYMENT_STREAM_TOPIC = "payment-stream";

    public CompletableFuture<SendResult<String, PaymentEvent>> sendPaymentProcessed(Payment payment) {
        return kafkaTemplate.send(
                PAYMENT_STREAM_TOPIC,
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
