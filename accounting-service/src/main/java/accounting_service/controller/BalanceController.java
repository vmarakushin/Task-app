package accounting_service.controller;

import accounting_service.data.dto.BalanceResponse;
import accounting_service.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
@Tag(name = "Balances", description = "API получения балансов")
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping
    @Operation(summary = "Получить балансы с пагинацией")
    public Page<BalanceResponse> getPage(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return balanceService.getPage(page, size);
    }

    @GetMapping("/{workerId}")
    @Operation(summary = "Получить баланс по ID работника")
    public BalanceResponse getById(@PathVariable UUID workerId) {
        return balanceService.findById(workerId);
    }
}
