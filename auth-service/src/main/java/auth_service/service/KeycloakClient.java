package auth_service.service;

import auth_service.config.KeycloakProperties;
import auth_service.dto.RegisterRequest;
import auth_service.dto.RegisterResponse;
import auth_service.dto.TokenResponse;
import auth_service.exception.ConflictException;
import auth_service.exception.KeycloakException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycloakClient {

    private final Keycloak keycloak;
    private final KeycloakProperties props;

    public KeycloakClient(KeycloakProperties props, Keycloak keycloak) {
        this.keycloak = keycloak;
        this.props = props;
    }

    public RegisterResponse register(RegisterRequest input) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(input.email());
        user.setEmail(input.email());
        user.setFirstName(input.firstName());
        user.setLastName(input.lastName());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(input.password());
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));

        try (Response response = keycloak.realm(props.realm())
                .users()
                .create(user)) {

            int status = response.getStatus();

            if (status == 201) {
                String location = response.getLocation().toString();
                String id = location.substring(location.lastIndexOf('/') + 1);

                return new RegisterResponse(id, input.firstName(), input.lastName());
            }

            if (status == 409) {
                throw new ConflictException("User already exists");
            }

            throw new KeycloakException("Keycloak error, status: " + status);
        }
    }

    public TokenResponse login(String email, String password) {

        AccessTokenResponse token;
        try (Keycloak kc = KeycloakBuilder.builder()
                .serverUrl(props.url())
                .realm(props.realm())
                .clientId(props.clientId())
                .grantType(OAuth2Constants.PASSWORD)
                .username(email)
                .password(password)
                .build()) {

            token = kc.tokenManager().getAccessToken();
        }

        return new TokenResponse(
                token.getToken(),
                token.getRefreshToken(),
                token.getExpiresIn()
        );
    }
}
