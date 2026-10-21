package events.user;

import java.time.Instant;
import java.util.UUID;

/** Топик {@link events.Topics#USER_FLOW}: пользователь зарегистрирован. */
public record UserCreatedEvent(
        UUID id,
        Instant createdAt
) {
}
