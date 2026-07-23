package api_gateway.filter;

import api_gateway.config.KeycloakProperties;
import api_gateway.dto.KeycloakUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TokenVerificationFilterFactory extends AbstractGatewayFilterFactory<TokenVerificationFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(TokenVerificationFilterFactory.class);
    private final KeycloakProperties properties;
    WebClient keycloakWebClient = WebClient.create();

    public TokenVerificationFilterFactory(KeycloakProperties properties) {
        super(Config.class);
        this.properties = properties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return keycloakWebClient.get()
                    .uri(properties.url())
                    .headers(headers -> headers.setBearerAuth(authHeader.substring(7)))
                    .retrieve()
                    .bodyToMono(KeycloakUserInfo.class)
                    .flatMap(userInfo -> {
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("user-id", userInfo.getSub())
                                .header("user-roles", String.join(",", userInfo.getRoles()))
                                .build();
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        log.error(String.valueOf(e));
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    public static class Config {
    }
}
