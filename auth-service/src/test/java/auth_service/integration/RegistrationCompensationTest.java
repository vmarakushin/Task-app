package auth_service.integration;

import auth_service.AbstractIntegrationTest;
import auth_service.dto.RegisterResponse;
import auth_service.kafka.KafkaProducer;
import auth_service.service.KeycloakClient;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class RegistrationCompensationTest extends AbstractIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakClient keycloakClient;

    @MockBean
    private KafkaProducer kafkaProducer;

    @Test
    @DisplayName("Kafka недоступна: 503 и пользователь удалён из Keycloak")
    void register_shouldReturn503_andDeleteUser_whenKafkaFails() throws Exception {
        when(keycloakClient.register(any()))
                .thenReturn(new RegisterResponse(USER_ID, "Ivan", "Ivanov", "user@mail.ru"));
        doThrow(new KafkaException("broker is down"))
                .when(kafkaProducer).sendUserRegistered(any(), any());

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@mail.ru",
                                  "password": "1-vvUN024!#&bnwgJJf",
                                  "firstName": "Ivan",
                                  "lastName": "Ivanov"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        verify(keycloakClient).deleteUser(USER_ID);
    }
}
