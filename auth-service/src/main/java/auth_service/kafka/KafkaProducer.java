package auth_service.kafka;

import events.Topics;
import events.user.UserCreatedEvent;
import events.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserCreatedEvent(UserCreatedEvent event) {
        kafkaTemplate.send(Topics.USER_FLOW, event.id().toString(), event).join();
    }

    public void sendUserStreamEvent(UserStreamEvent event) {
        kafkaTemplate.send(Topics.USER_STREAM, event.id().toString(), event).join();
    }
}
