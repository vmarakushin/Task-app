package events.task;

import java.util.UUID;

/**
 * Топик {@link events.Topics#TASK_STREAM}: актуальное состояние задачи.
 * Поля {@code worker*} равны null, пока исполнитель не назначен.
 */
public record TaskStreamEvent(
        Long taskId,
        String title,
        String description,
        TaskStatus status,
        UUID workerId,
        String workerName,
        String workerEmail
) {
}
