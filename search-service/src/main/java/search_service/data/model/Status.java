package search_service.data.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статусы задачи")
public enum Status {
    NEW, IN_PROGRESS, FINISHED, CANCELLED
}
