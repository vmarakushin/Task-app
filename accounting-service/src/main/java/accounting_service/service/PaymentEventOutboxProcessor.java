package accounting_service.service;

import accounting_service.data.dto.PaymentEvent;
import accounting_service.data.model.Payment;
import accounting_service.kafka.AccountingEventProducer;
import accounting_service.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventOutboxProcessor {

    private final PaymentRepository paymentRepository;
    private final AccountingEventProducer producer;

    @Transactional
    int processBatch(int batch) {
        List<Payment> payments = paymentRepository.findTop100UnprocessedOrderByCreatedAtAsc(batch);
        if (payments.isEmpty()) {
            return 0;
        }
        List<CompletableFuture<SendResult<String, PaymentEvent>>> futures = payments.stream()
                .map(producer::sendPaymentProcessed)
                .toList();

        CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[payments.size()])
        ).join();

        List<Long> sentIds = payments.stream()
                .map(Payment::getId)
                .toList();

        int processed = paymentRepository.markProcessed(sentIds);
        if (processed != payments.size()) {
            log.warn(
                    "Expected {} processed payments, but marked {}",
                    payments.size(),
                    processed
            );
        }
        return processed;
    }
}
