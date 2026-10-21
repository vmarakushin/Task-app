package search_service.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import search_service.data.model.Status;

import java.util.UUID;

public record TaskResponse(

        @Schema(
                description = "Id созданной задачи"
        )
        Long id,

        @Schema(
                description = "Краткое описание задачи"
        )
        String title,

        @Schema(
                description = "Пояснения к задаче"
        )
        String description,

        @Schema(
                description = "Статус задачи"
        )
        Status status,

        @Schema(
                description = "(При наличии) Id исполнителя"
        )
        UUID workerId,

        @Schema(
                description = "(При наличии) Имя исполнителя"
        )
        String workerName,

        @Schema(
                description = "(При наличии) Электронная почта исполнителя"
        )
        String workerEmail
) {
}
