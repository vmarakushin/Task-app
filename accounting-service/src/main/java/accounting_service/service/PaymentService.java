package accounting_service.service;

import accounting_service.config.PaymentProperties;
import accounting_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProperties properties;
    private final PaymentEventOutboxProcessor processor;

    @Transactional
    public void processPayments() {
        int count = paymentRepository.createPayments(properties.taskAmount());
        log.info("Created {} payments", count);
    }

    public void sendPaymentsToKafka() {
        try {
            if (!paymentRepository.existsByProcessedFalse()) {
                return;
            }
            while (true) {
                int processed = processor.processBatch(properties.outbox().batchSize());
                if (processed == 0) break;
                log.info("Sent {} payment events to Kafka", processed);
            }
        } catch (Exception e) {
            log.error("An error occurred while sending payment events: ", e);
        }
    }
}
