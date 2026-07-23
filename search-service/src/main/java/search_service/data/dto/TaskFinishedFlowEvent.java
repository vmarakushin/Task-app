package search_service.data.dto;

import java.time.Instant;

public record TaskFinishedFlowEvent(
        Long id,
        Instant finishedAt
) {
}
