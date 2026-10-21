package auth_service.exception;

public class KeycloakConfigurationException extends RuntimeException {
    public KeycloakConfigurationException(String message) {
        super(message);
    }
}
