package search_service.exception;

public class FinishedTaskException extends RuntimeException{
    public FinishedTaskException(String message) {
        super(message);
    }
}
