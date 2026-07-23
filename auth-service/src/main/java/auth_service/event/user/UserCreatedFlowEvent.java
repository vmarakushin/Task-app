package auth_service.event.user;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedFlowEvent(
        UUID id,
        Instant createdAt
) {
}
