package task_service.data.dto;

public record WorkerAssignedEvent(

        Long taskId,

        Long workerId
) {
}
