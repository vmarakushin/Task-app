package events.user;

import java.util.UUID;

/** Топик {@link events.Topics#USER_STREAM}: актуальное состояние пользователя. */
public record UserStreamEvent(
        UUID id,
        String firstName,
        String lastName,
        String email
) {
}
