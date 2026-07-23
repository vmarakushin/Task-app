package task_service.data.dto;

import search_service.data.model.Status;
import search_service.data.model.Worker;

public record TaskStreamEvent(
        Long id,
        String title,
        Worker executor,
        String description,
        Status status
) {
}
