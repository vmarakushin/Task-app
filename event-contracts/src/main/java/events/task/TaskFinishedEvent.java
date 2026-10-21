package events.task;

import java.time.Instant;
import java.util.UUID;

/**
 * Топик {@link events.Topics#TASK_FINISHED_FLOW}: задача завершена исполнителем workerId.
 */
public record TaskFinishedEvent(
        Long taskId,
        UUID workerId,
        Instant finishedAt
) {
}
