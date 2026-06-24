package auth_service.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Bean
    public Keycloak keycloakAdminClient(KeycloakProperties props) {
        return KeycloakBuilder.builder()
                .serverUrl(props.url())
                .realm(props.realm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(props.clientId())
                .clientSecret(props.clientSecret())
                .build();
    }
}
