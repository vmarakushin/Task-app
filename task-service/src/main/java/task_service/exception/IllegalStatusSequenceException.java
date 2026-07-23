package task_service.exception;

public class IllegalStatusSequenceException extends RuntimeException {
    public IllegalStatusSequenceException(String message) {
        super(message);
    }
}
