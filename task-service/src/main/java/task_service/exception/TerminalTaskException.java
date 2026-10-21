package task_service.exception;

/**
 * Попытка изменить задачу в конечном статусе (FINISHED или CANCELLED).
 */
public class TerminalTaskException extends RuntimeException {
    public TerminalTaskException(String message) {
        super(message);
    }
}
