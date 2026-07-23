package accounting_service.data.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
        Long paymentId,
        Long taskId,
        UUID workerId,
        BigDecimal amount,
        Instant createdAt
) {}
