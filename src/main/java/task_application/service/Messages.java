package task_application.service;

public enum Messages {
    NO_WORKER("Worker not assigned"),
    TASK_NOT_FOUND("Task with id %s not found"),
    WORKER_NOT_FOUND("Worker with id %s not found"),
    ILLEGAL_TASK_STATUS_SEQUENCE("Task %s can not be %s, because it is %s");

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
