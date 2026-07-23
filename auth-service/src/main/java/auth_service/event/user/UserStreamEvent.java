package auth_service.event.user;

import java.util.UUID;

public record UserStreamEvent(
        UUID id,
        String firstName,
        String lastName,
        String email
) {
}
