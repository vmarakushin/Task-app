package auth_service.unit;

import auth_service.dto.RegisterRequest;
import auth_service.dto.RegisterResponse;
import auth_service.exception.KeycloakException;
import auth_service.exception.ServiceUnavailableException;
import auth_service.kafka.KafkaProducer;
import auth_service.service.AuthService;
import auth_service.service.KeycloakClient;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private static final RegisterRequest REQUEST =
            new RegisterRequest("user@mail.ru", "1-vvUN024!#&bnwgJJf", "Ivan", "Ivanov");

    private static final RegisterResponse CREATED =
            new RegisterResponse(USER_ID, "Ivan", "Ivanov", "user@mail.ru");

    @Mock
    private KeycloakClient keycloakClient;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Успешная регистрация: события отправлены, пользователь не удаляется")
    void register_shouldPublishEvents() {
        when(keycloakClient.register(REQUEST)).thenReturn(CREATED);

        RegisterResponse result = authService.register(REQUEST);

        assertEquals(USER_ID, result.id());
        verify(kafkaProducer).sendUserRegistered(any(), any());
        verify(keycloakClient, never()).deleteUser(any());
    }

    @Test
    @DisplayName("Kafka недоступна: пользователь удаляется из Keycloak, клиент получает 503")
    void register_shouldCompensate_whenKafkaFails() {
        when(keycloakClient.register(REQUEST)).thenReturn(CREATED);
        doThrow(new KafkaException("broker is down"))
                .when(kafkaProducer).sendUserRegistered(any(), any());

        ServiceUnavailableException ex =
                assertThrows(ServiceUnavailableException.class, () -> authService.register(REQUEST));

        assertInstanceOf(KafkaException.class, ex.getCause());
        verify(keycloakClient).deleteUser(USER_ID);
    }

    @Test
    @DisplayName("Компенсация тоже упала: всё равно 503, ошибка удаления не теряется")
    void register_shouldStillFail_whenCompensationFails() {
        when(keycloakClient.register(REQUEST)).thenReturn(CREATED);
        doThrow(new KafkaException("broker is down"))
                .when(kafkaProducer).sendUserRegistered(any(), any());
        doThrow(new KeycloakException("keycloak is down"))
                .when(keycloakClient).deleteUser(USER_ID);

        ServiceUnavailableException ex =
                assertThrows(ServiceUnavailableException.class, () -> authService.register(REQUEST));

        assertInstanceOf(KeycloakException.class, ex.getCause().getSuppressed()[0]);
    }

    @Test
    @DisplayName("Keycloak отказал в регистрации: в Kafka ничего не отправляется")
    void register_shouldNotPublish_whenKeycloakFails() {
        when(keycloakClient.register(REQUEST)).thenThrow(new KeycloakException("bad request"));

        assertThrows(KeycloakException.class, () -> authService.register(REQUEST));

        verify(kafkaProducer, never()).sendUserRegistered(any(), any());
    }
}
