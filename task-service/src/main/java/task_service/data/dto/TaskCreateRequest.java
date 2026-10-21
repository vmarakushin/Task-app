package task_service.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCreateRequest(

        @Schema(
                description = "Краткое описание задачи, обязательно к заполнению",
                required = true
        )
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "Пояснения к задаче, опционально")
        @Size(max = 5000)
        String description
) {
}
