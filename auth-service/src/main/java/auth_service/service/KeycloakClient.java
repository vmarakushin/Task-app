package auth_service.service;

import auth_service.config.KeycloakProperties;
import auth_service.dto.RegisterRequest;
import auth_service.dto.RegisterResponse;
import auth_service.dto.TokenResponse;
import auth_service.exception.ConflictException;
import auth_service.exception.InvalidUserCredentialsException;
import auth_service.exception.KeycloakConfigurationException;
import auth_service.exception.KeycloakException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class KeycloakClient {

    private final Keycloak keycloak;
    private final KeycloakProperties props;
    private final WebClient webClient = WebClient.builder().build();
    private final String tokenUrl;

    public KeycloakClient(KeycloakProperties props, Keycloak keycloak) {
        this.keycloak = keycloak;
        this.props = props;
        this.tokenUrl = props.url() + "/realms/" + props.realm() + "/protocol/openid-connect/token";
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
                String body = response.readEntity(String.class);
                String message = extractErrorMessage(body);
                throw new ConflictException(message);
            }

            String body = response.readEntity(String.class);
            String message = extractErrorMessage(body);
            throw new KeycloakException(message);
        }
    }

    public TokenResponse login(String email, String password) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", props.clientId());
        formData.add("client_secret", props.clientSecret());
        formData.add("username", email);
        formData.add("password", password);
        formData.add("scope", "openid profile email");

        AccessTokenResponse response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().value() == 401) {
                        return Mono.error(new InvalidUserCredentialsException("Неверный логин или пароль"));
                    }
                    return Mono.error(new KeycloakConfigurationException("Ошибка настройки backend клиента"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new KeycloakException("Внутренняя ошибка Keycloak")))
                .bodyToMono(AccessTokenResponse.class)
                .block();

        if (response == null) {
            throw new KeycloakException("Не удалось получить токен от Keycloak");
        }

        return new TokenResponse(
                response.getToken(),
                response.getRefreshToken(),
                response.getExpiresIn()
        );
    }


    private String extractErrorMessage(String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);

            if (node.has("errorMessage")) {
                return node.get("errorMessage").asText();
            }

            if (node.has("error")) {
                return node.get("error").asText();
            }
            return body;

        } catch (Exception e) {
            return body;
        }
    }
}
