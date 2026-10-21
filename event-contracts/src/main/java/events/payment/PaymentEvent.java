package events.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Топик {@link events.Topics#PAYMENT_STREAM}: начислена выплата за задачу.
 */
public record PaymentEvent(
        Long paymentId,
        Long taskId,
        UUID workerId,
        BigDecimal amount,
        Instant createdAt
) {
}
