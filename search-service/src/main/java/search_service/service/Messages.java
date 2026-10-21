package search_service.service;

public enum Messages {
    NO_WORKER("Worker not assigned"),
    WORKER_NOT_SYNCED("Worker data is not synchronized yet"),
    TASK_NOT_FOUND("Task with id %s not found"),
    WORKER_NOT_FOUND("Worker with id %s not found");

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
