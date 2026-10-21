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

    /** Оба события публикуются в одной Kafka-транзакции. */
    public void sendUserRegistered(UserCreatedEvent created, UserStreamEvent stream) {
        String key = created.id().toString();

        kafkaTemplate.executeInTransaction(operations -> {
            operations.send(Topics.USER_FLOW, key, created);
            operations.send(Topics.USER_STREAM, key, stream);
            return null;
        });
    }
}
