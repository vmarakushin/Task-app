package auth_service.exception;

public class KeycloakException extends RuntimeException {
    public KeycloakException(String message) {
        super(message);
    }

    public KeycloakException(Throwable e) {
        super(e);
    }
}
