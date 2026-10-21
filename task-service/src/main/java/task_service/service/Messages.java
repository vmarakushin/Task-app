package task_service.service;

public enum Messages {
    NO_WORKER("Worker not assigned"),
    TASK_NOT_FOUND("Task with id %s not found"),
    WORKER_NOT_FOUND("Worker with id %s not found"),
    ILLEGAL_TASK_STATUS_SEQUENCE("Task %s can not be %s, because it is %s"),
    WORKER_SHOULD_BE_ASSIGNED("Task must have an assigned worker to become %s"),
    WORKER_CANNOT_BE_ASSIGNED_TO_CLOSED_TASK("Worker %s can't be assigned because task %s is %s"),
    CONCURRENT_MODIFICATION("Task was modified by another request, reload it and try again");

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
