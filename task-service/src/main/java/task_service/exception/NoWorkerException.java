package task_service.exception;

public class NoWorkerException extends RuntimeException{
    public NoWorkerException(String message) {
        super(message);
    }
}
