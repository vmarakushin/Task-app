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
     * @param cron       когда проверять, не наступил ли день выплаты (по умолчанию каждую пятницу в 00:00)
     * @param anchorDate необязательная первая дата выплаты (yyyy-MM-dd); если пусто - ближайшая пятница
     *                   после первого старта сервиса, сохраняется в таблице payout_schedule
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
