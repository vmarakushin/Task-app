package task_service.data.dto;

import java.time.Instant;

public record TaskCreatedFlowEvent(
        Long id,
        Instant createdAt
) {
}
