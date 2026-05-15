package task_application.data.dto;

public record TaskCreatedEvent(

        Long taskId,

        String title,

        String description
) {
}
