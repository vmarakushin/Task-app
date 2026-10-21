package events.task;

import java.util.UUID;

/** Топик {@link events.Topics#TASK_ASSIGNED}: задаче назначен исполнитель. */
public record WorkerAssignedEvent(
        Long taskId,
        UUID workerId
) {
}
