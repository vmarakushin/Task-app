package search_service.data.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Статус задачи в read-модели. Правила переходов живут в task-service,
 * search только отображает то, что пришло в task-stream.
 */
@Schema(description = "Статусы задачи")
public enum Status {
    NEW, IN_PROGRESS, FINISHED, CANCELLED
}
