package task_service.data.dto;

import java.util.UUID;

public record WorkerAssignedEvent(

        Long taskId,

        UUID workerId
) {
}
