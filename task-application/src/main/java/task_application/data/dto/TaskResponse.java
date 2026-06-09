package task_application.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import task_application.data.model.Status;

public record TaskResponse(

        @Schema(
                description = "Id созданной задачи"
        )
        long id,

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
        Long workerId,

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
