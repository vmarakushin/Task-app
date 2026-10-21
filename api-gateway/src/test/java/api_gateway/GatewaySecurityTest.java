package api_gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@Import({GatewaySecurityTest.EchoRoute.class, GatewaySecurityTest.EchoController.class})
class GatewaySecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Без токена - 401")
    void withoutToken_shouldReturn401() {
        webTestClient.get().uri("/api/tasks")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Токен без роли worker - 403")
    void withoutWorkerRole_shouldReturn403() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_guest")))
                .get().uri("/api/tasks")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Регистрация и логин доступны без токена")
    void registration_shouldNotRequireToken() {
        webTestClient.post().uri("/registration")
                .exchange()
                .expectStatus().value(status -> {
                    assertNotEquals(401, status);
                    assertNotEquals(403, status);
                });
    }

    @Test
    @DisplayName("user-id берётся из токена, подделанный клиентом заголовок отбрасывается")
    void userHeaders_shouldComeFromToken() {
        webTestClient.mutateWith(mockJwt()
                        .jwt(jwt -> jwt.subject("real-user"))
                        .authorities(new SimpleGrantedAuthority("ROLE_worker")))
                .get().uri("/api/echo")
                .header("user-id", "someone-else")
                .header("user-roles", "admin")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user-id").isEqualTo("real-user")
                .jsonPath("$.user-roles").isEqualTo("worker");
    }

    @TestConfiguration
    static class EchoRoute {
        @Bean
        RouteLocator echoRoute(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("echo", route -> route.path("/api/echo").uri("forward:/echo-headers"))
                    .build();
        }
    }

    @RestController
    static class EchoController {
        @GetMapping("/echo-headers")
        Map<String, String> echo(@RequestHeader HttpHeaders headers) {
            Map<String, String> result = new HashMap<>();
            result.put("user-id", headers.getFirst("user-id"));
            result.put("user-roles", headers.getFirst("user-roles"));
            return result;
        }
    }
}
