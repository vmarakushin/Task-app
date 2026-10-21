package auth_service.service;

import auth_service.dto.LoginRequest;
import auth_service.dto.RegisterRequest;
import auth_service.dto.RegisterResponse;
import auth_service.dto.TokenResponse;
import auth_service.kafka.KafkaProducer;
import events.user.UserCreatedEvent;
import events.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakClient client;

    private final KafkaProducer kafka;

    public TokenResponse login(LoginRequest request){
        return client.login(request.email(), request.password());
    }

    public RegisterResponse register(RegisterRequest request) {
        RegisterResponse response = client.register(request);

        kafka.sendUserCreatedEvent(
                new UserCreatedEvent(response.id(), Instant.now())
        );
        kafka.sendUserStreamEvent(
                new UserStreamEvent(response.id(), response.firstName(), response.lastName(), response.email())
        );
        return response;
    }
}
