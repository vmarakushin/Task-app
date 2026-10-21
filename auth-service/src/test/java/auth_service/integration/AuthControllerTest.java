package auth_service.integration;

import auth_service.AbstractIntegrationTest;
import auth_service.dto.RegisterResponse;
import auth_service.dto.TokenResponse;
import auth_service.exception.ConflictException;
import auth_service.exception.InvalidUserCredentialsException;
import auth_service.service.KeycloakClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakClient keycloakClient;

    private static String existingEmail = "user@mail.ru";

    private static String newEmail = "anotherone@gmail.com";

    private static String strongPassword = "1-vvUN024!#&bnwgJJf";

    private static UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private static String firstName = "Ivan";

    private static String lastName = "Ivanov";

    private static String token = "simple-example-token";

    private static long expiration = 300;

    String registerRequest = """
                       {
                        "email": "%s",
                        "password": "%s",
                        "firstName": "%s",
                        "lastName": "%s"
                        }
                       """;

    String loginRequest =     """
                       {
                        "email": "%s",
                        "password": "%s"
                        }
                       """;

    @Test
    @DisplayName("Попытка регистрации с уже существующим Email")
    void register_shouldReturn409_whenEmailExists() throws Exception {
        String requestBody = registerRequest.formatted(existingEmail, strongPassword, firstName, lastName);

        when(keycloakClient.register(any()))
                .thenThrow(ConflictException.class);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Проверка успешной регистрации")
    void register_shouldReturn201_created() throws Exception {
        String requestBody = registerRequest.formatted(newEmail, strongPassword, firstName, lastName);

        when(keycloakClient.register(any()))
                .thenReturn(new RegisterResponse(uuid, firstName, lastName, newEmail));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(uuid.toString()))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName));
    }

    @Test
    @DisplayName("Попытка залогиниться по несуществующим данным")
    void login_shouldReturn401_whenUserNotExist() throws Exception {
        String requestBody = loginRequest.formatted(newEmail, strongPassword);

        when(keycloakClient.login(newEmail, strongPassword))
                .thenThrow(InvalidUserCredentialsException.class);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Попытка залогиниться успешно")
    void login_shouldReturn200AndJwt_whenLoginSuccess() throws Exception {
        String requestBody = loginRequest.formatted(existingEmail, strongPassword);

        when(keycloakClient.login(any(), any()))
                .thenReturn(new TokenResponse(token, token, expiration));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(token))
                .andExpect(jsonPath("$.refreshToken").value(token))
                .andExpect(jsonPath("$.expiresIn").value(expiration));
    }
}
