package task_service.kafka;

import events.Topics;
import events.task.TaskCreatedEvent;
import events.task.TaskFinishedEvent;
import events.task.TaskStreamEvent;
import events.task.WorkerAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTaskCreated(TaskCreatedEvent event) {
        kafkaTemplate.send(Topics.TASK_FLOW, event.taskId().toString(), event).join();
    }

    public void sendTaskStream(TaskStreamEvent event) {
        kafkaTemplate.send(Topics.TASK_STREAM, event.taskId().toString(), event).join();
    }

    public void sendWorkerAssigned(WorkerAssignedEvent event) {
        kafkaTemplate.send(Topics.TASK_ASSIGNED, event.taskId().toString(), event).join();
    }

    public void sendTaskFinished(TaskFinishedEvent event) {
        kafkaTemplate.send(Topics.TASK_FINISHED_FLOW, event.taskId().toString(), event).join();
    }
}
