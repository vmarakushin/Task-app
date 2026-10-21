package task_service.data.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статусы задачи")
public enum Status {
    NEW, IN_PROGRESS, FINISHED, CANCELLED;

    public boolean isTerminal() {
        return this == FINISHED || this == CANCELLED;
    }

    public boolean requiresWorker() {
        return this == IN_PROGRESS || this == FINISHED;
    }

    public boolean canMoveTo(Status next) {
        return switch (this) {
            case NEW -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == FINISHED || next == CANCELLED;
            case FINISHED, CANCELLED -> false;
        };
    }
}
