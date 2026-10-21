package task_service.kafka;

import events.Topics;
import events.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import task_service.service.TaskKafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStreamConsumer {

    private final TaskKafkaHandler handler;

    @KafkaListener(topics = Topics.USER_STREAM)
    public void consume(UserStreamEvent event, Acknowledgment ack) {
        try {
            handler.handleUserStream(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("An error occurred: ", e);
        }
    }
}
