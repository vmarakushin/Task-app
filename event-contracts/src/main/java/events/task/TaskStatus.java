package events.task;

/**
 * Статус задачи в контракте событий. Правила переходов живут в task-service.
 */
public enum TaskStatus {
    NEW, IN_PROGRESS, FINISHED, CANCELLED
}
