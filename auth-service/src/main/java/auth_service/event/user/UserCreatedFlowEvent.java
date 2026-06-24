package auth_service.event.user;

import java.time.Instant;

public record UserCreatedFlowEvent(
        String id,
        Instant createdAt
) {
}
