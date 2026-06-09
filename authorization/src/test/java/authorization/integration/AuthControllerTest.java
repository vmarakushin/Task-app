package authorization.integration;

import authorization.AbstractIntegrationTest;
import authorization.entity.User;
import authorization.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String existingEmail = "user@mail.ru";

    private static String newEmail = "anotherone@gmail.com";

    private static String strongPassword = "1-vvUN024!#&bnwgJJf";

    String json = """
                       {
                        "email": "%s",
                        "password": "%s"
                        }
                       """;

    @BeforeEach
    void setUp(){
        userRepository.save(
                User.builder()
                        .email(existingEmail)
                        .password(passwordEncoder.encode(strongPassword))
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Попытка регистрации с уже существующим Email")
    void register_shouldReturn429_whenEmailExists() throws Exception {
        String requestBody = json.formatted(existingEmail, strongPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Проверка успешной регистрации")
    void register_shouldReturn201_created() throws Exception{
        String requestBody = json.formatted(newEmail, strongPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(newEmail));

        assertTrue(userRepository.existsByEmail(newEmail));
    }

    @Test
    @DisplayName("Попытка залогиниться по несуществующим данным")
    void login_shouldReturn401_whenUserNotExist() throws Exception {
        String requestBody = json.formatted(newEmail, strongPassword);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Попытка залогиниться успешно")
    void login_shouldReturn200AndJwt_whenLoginSuccess() throws Exception {
        String requestBody = json.formatted(existingEmail, strongPassword);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
