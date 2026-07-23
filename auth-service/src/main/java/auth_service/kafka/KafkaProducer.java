package auth_service.kafka;

import auth_service.event.user.UserCreatedFlowEvent;
import auth_service.event.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_FLOW_TOPIC = "user-flow";
    private static final String USER_STREAM_TOPIC = "user-stream";

    public void sendUserCreatedFlowEvent(UserCreatedFlowEvent event) {
        kafkaTemplate.send(USER_FLOW_TOPIC, event.id().toString(), event).join();
    }

    public void sendUserStreamEvent(UserStreamEvent event) {
        kafkaTemplate.send(USER_STREAM_TOPIC, event.id().toString(), event).join();
    }
}
