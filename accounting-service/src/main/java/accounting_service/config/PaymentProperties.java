package accounting_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.math.BigDecimal;

@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
        BigDecimal taskAmount,
        String cron,
        Outbox outbox
) {
    public record Outbox(
            int batchSize,
            int intervalSec
    ) {}
}
