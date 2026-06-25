package task_service.kafka;

import task_service.data.dto.TaskCreatedFlowEvent;
import task_service.data.dto.TaskStreamEvent;
import task_service.data.dto.WorkerAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TASK_FLOW_TOPIC = "task-flow";
    private static final String TASK_STREAM_TOPIC = "task-stream";
    private static final String TASK_ASSIGNED_TOPIC = "task-assigned";

    public void sendTaskCreated(TaskCreatedFlowEvent event) {
        kafkaTemplate.send(TASK_FLOW_TOPIC, event.id().toString(), event);
    }

    public void sendTaskCreated(TaskStreamEvent event) {
        kafkaTemplate.send(TASK_STREAM_TOPIC, event.id().toString(), event);
    }

    public void sendWorkerAssigned(WorkerAssignedEvent event) {
        kafkaTemplate.send(TASK_ASSIGNED_TOPIC, event.taskId().toString(), event);
    }
}
