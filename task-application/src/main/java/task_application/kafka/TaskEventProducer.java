package task_application.kafka;

import task_application.data.dto.TaskCreatedEvent;
import task_application.data.dto.WorkerAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TASK_CREATED_TOPIC = "task.created";
    private static final String TASK_ASSIGNED_TOPIC = "task.assigned";

    public void sendTaskCreated(TaskCreatedEvent event) {
        kafkaTemplate.send(TASK_CREATED_TOPIC, event.taskId().toString(), event);
    }

    public void sendWorkerAssigned(WorkerAssignedEvent event) {
        kafkaTemplate.send(TASK_ASSIGNED_TOPIC, event.taskId().toString(), event);
    }
}
