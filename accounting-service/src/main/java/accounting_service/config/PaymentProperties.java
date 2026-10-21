package accounting_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
        BigDecimal taskAmount,
        Payout payout,
        Outbox outbox
) {
    /**
     * @param cron       когда проверять, наступил ли день выплаты
     * @param anchorDate первая дата выплаты (yyyy-MM-dd); если пусто - ближайшая пятница после первого старта
     */
    public record Payout(
            String cron,
            String anchorDate
    ) {}

    public record Outbox(
            int batchSize,
            int intervalSec
    ) {}
}
