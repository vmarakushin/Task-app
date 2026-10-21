package events.task;

import java.time.Instant;

/** Топик {@link events.Topics#TASK_FLOW}: задача создана. */
public record TaskCreatedEvent(
        Long taskId,
        Instant createdAt
) {
}
