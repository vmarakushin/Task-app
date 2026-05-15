package task_application.data.dto;

public record WorkerAssignedEvent(

        Long taskId,

        Long workerId
) {
}
