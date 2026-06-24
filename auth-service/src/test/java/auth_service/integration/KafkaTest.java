package auth_service.integration;

import auth_service.AbstractIntegrationTest;
import auth_service.dto.RegisterRequest;
import auth_service.dto.RegisterResponse;
import auth_service.event.user.UserCreatedFlowEvent;
import auth_service.event.user.UserStreamEvent;
import auth_service.service.AuthService;
import auth_service.service.KeycloakClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class KafkaTest extends AbstractIntegrationTest {

    @MockBean
    private KeycloakClient keycloakClient;

    @Autowired
    private AuthService authService;

    private static Consumer<String, UserCreatedFlowEvent> flowConsumer;

    private static Consumer<String, UserStreamEvent> streamConsumer;

    private static String existingEmail = "user@mail.ru";

    private static String strongPassword = "1-vvUN024!#&bnwgJJf";

    private static String uuid = "550e8400-e29b-41d4-a716-446655440000";

    private static String firstName = "Ivan";

    private static String lastName = "Ivanov";

    @BeforeAll
    static void setupConsumers() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafka().getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        flowConsumer = new DefaultKafkaConsumerFactory<String, UserCreatedFlowEvent>(props).createConsumer();
        flowConsumer.subscribe(List.of("user-flow"));

        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        streamConsumer = new DefaultKafkaConsumerFactory<String, UserStreamEvent>(props).createConsumer();
        streamConsumer.subscribe(List.of("user-stream"));
    }

    @Test
    @DisplayName("Проверка отправки в user-flow")
    void register_shouldPushToKafka_userFlow(){
        when(keycloakClient.register(any()))
                .thenReturn(new RegisterResponse(uuid, firstName, lastName));

        authService.register(new RegisterRequest(existingEmail, strongPassword, firstName, lastName));

        ConsumerRecords<String, UserCreatedFlowEvent> records =
                flowConsumer.poll(Duration.ofSeconds(10));

        assertFalse(records.isEmpty());

        UserCreatedFlowEvent event = records.iterator().next().value();

        assertEquals(uuid, event.id());
        assertTrue(Instant.now().isAfter(event.createdAt()));
    }

    @Test
    @DisplayName("Проверка отправки в user-stream")
    void register_shouldPushToKafka_userStream(){
        when(keycloakClient.register(any()))
                .thenReturn(new RegisterResponse(uuid, firstName, lastName));

        authService.register(new RegisterRequest(existingEmail, strongPassword, firstName, lastName));

        ConsumerRecords<String, UserStreamEvent> records =
                streamConsumer.poll(Duration.ofSeconds(10));

        assertFalse(records.isEmpty());

        UserStreamEvent event = records.iterator().next().value();

        assertEquals(uuid, event.id());
        assertEquals(firstName, event.firstName());
        assertEquals(lastName, event.lastName());
    }
}
