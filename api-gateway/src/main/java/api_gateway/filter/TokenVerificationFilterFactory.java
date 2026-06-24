package api_gateway.filter;

import api_gateway.dto.KeycloakUserInfo;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TokenVerificationFilterFactory extends AbstractGatewayFilterFactory<TokenVerificationFilterFactory.Config> {

    private final WebClient keycloakWebClient;

    public TokenVerificationFilterFactory(WebClient keycloakWebClient) {
        super(Config.class);
        this.keycloakWebClient = keycloakWebClient;
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
                    .uri("/userinfo")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
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
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    public static class Config {
    }
}
