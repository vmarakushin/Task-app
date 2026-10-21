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

    /**
     * Оба события уходят в одной Kafka-транзакции: консьюмеры с isolation.level=read_committed
     * увидят либо оба, либо ни одного. Метод возвращается только после commit,
     * любая ошибка (в т.ч. недоступность брокера) приводит к abort и пробрасывается.
     */
    public void sendUserRegistered(UserCreatedEvent created, UserStreamEvent stream) {
        String key = created.id().toString();

        kafkaTemplate.executeInTransaction(operations -> {
            operations.send(Topics.USER_FLOW, key, created);
            operations.send(Topics.USER_STREAM, key, stream);
            return null;
        });
    }
}
