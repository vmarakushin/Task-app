package auth_service.service;

import auth_service.dto.LoginRequest;
import auth_service.dto.RegisterRequest;
import auth_service.dto.RegisterResponse;
import auth_service.dto.TokenResponse;
import auth_service.exception.ServiceUnavailableException;
import auth_service.kafka.KafkaProducer;
import events.user.UserCreatedEvent;
import events.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakClient client;

    private final KafkaProducer kafka;

    public TokenResponse login(LoginRequest request){
        return client.login(request.email(), request.password());
    }

    /**
     * Регистрация затрагивает две системы - Keycloak и Kafka - без общей транзакции.
     * Поэтому используется компенсация: если события не удалось опубликовать,
     * созданный в Keycloak пользователь удаляется, а клиент получает 503.
     */
    public RegisterResponse register(RegisterRequest request) {
        RegisterResponse user = client.register(request);

        try {
            kafka.sendUserRegistered(
                    new UserCreatedEvent(user.id(), Instant.now()),
                    new UserStreamEvent(user.id(), user.firstName(), user.lastName(), user.email())
            );
        } catch (RuntimeException e) {
            log.error("Failed to publish registration events for user {}, rolling back", user.id(), e);
            compensate(user.id(), e);
            throw new ServiceUnavailableException("Регистрация временно недоступна, попробуйте позже", e);
        }
        return user;
    }

    private void compensate(UUID userId, RuntimeException cause) {
        try {
            client.deleteUser(userId);
        } catch (RuntimeException deleteError) {
            cause.addSuppressed(deleteError);
            // Пользователь остался в Keycloak без событий: в других сервисах его нет.
            log.error("COMPENSATION FAILED: user {} exists in Keycloak without events, manual cleanup required",
                    userId, deleteError);
        }
    }
}
