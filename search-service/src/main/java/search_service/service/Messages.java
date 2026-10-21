package search_service.service;

public enum Messages {
    NO_WORKER("Worker not assigned"),
    WORKER_NOT_SYNCED("Worker data is not synchronized yet"),
    TASK_NOT_FOUND("Task with id %s not found"),
    WORKER_NOT_FOUND("Worker with id %s not found"),
    ILLEGAL_TASK_STATUS_SEQUENCE("Task %s can not be %s, because it is %s"),
    WORKER_SHOULD_BE_ASSIGNED("Task must have an assigned worker before changing status"),
    WORKER_CANNOT_BE_ASSIGNED_IF_TASK_IS_FINISHED("Worker $s can't be assigned because task $s is FINISHED");

    private final String text;

    Messages(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public String format(Object... args) {
        return String.format(text, args);
    }
}
