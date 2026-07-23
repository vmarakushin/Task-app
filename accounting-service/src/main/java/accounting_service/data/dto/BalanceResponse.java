package accounting_service.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(

        @Schema(description = "Id работника")
        UUID workerId,
        @Schema(description = "Баланс счета")
        BigDecimal amount
) {
}
