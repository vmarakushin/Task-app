package search_service.data.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статусы задачи")
public enum Status {
    NEW, IN_PROGRESS, FINISHED, CANCELLED;

    public boolean canMoveTo(Status next) {

        if (this == FINISHED || this == CANCELLED) {
            return false;
        }

        return switch (this) {
            case NEW -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == FINISHED || next == CANCELLED;
            default -> false;
        };
    }
}
