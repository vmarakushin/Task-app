package api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param issuer    ожидаемое значение claim "iss" в токене
 * @param jwkSetUri откуда брать публичные ключи Keycloak для проверки подписи
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String issuer,
        String jwkSetUri
) {}
