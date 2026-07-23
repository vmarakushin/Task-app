package accounting_service.service;

import accounting_service.config.PaymentProperties;
import accounting_service.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProperties properties;
    private final PaymentEventOutboxProcessor processor;

    @Transactional
    public void processPayments() {
        try {
            int count = paymentRepository.createPayments(
                    properties.taskAmount()
            );
            log.info("Created {} payments", count);
        } catch (Exception e) {
            log.error("An error occurred while processing payday: ", e);
        }
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
