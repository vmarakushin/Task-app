package task_service.kafka;

import events.Topics;
import events.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import task_service.service.TaskKafkaHandler;

@Component
@RequiredArgsConstructor
public class UserStreamConsumer {

    private final TaskKafkaHandler handler;

    @KafkaListener(topics = Topics.USER_STREAM)
    public void consume(UserStreamEvent event) {
        handler.handleUserStream(event);
    }
}
