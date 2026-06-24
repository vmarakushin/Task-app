package auth_service.event.user;

public record UserStreamEvent(
        String id,
        String firstName,
        String lastName
) {
}
