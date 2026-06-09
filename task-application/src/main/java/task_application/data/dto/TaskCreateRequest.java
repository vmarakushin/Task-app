package task_application.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TaskCreateRequest(

        @Schema(
                description = "Краткое описание задачи, обязательно к заполнению",
                required = true
        )
        String title,

        @Schema(description = "Пояснения к задаче, опционально")
        String description
) {
}
