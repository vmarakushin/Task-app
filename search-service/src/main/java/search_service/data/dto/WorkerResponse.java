package search_service.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record WorkerResponse(

        @Schema(description = "Id работника")
        UUID id,
        @Schema(description = "Имя работника")
        String name,
        @Schema(description = "Электронная почта работника")
        String email
) {
}
