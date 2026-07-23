package auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Ответ после регистрации")
public record RegisterResponse(

        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(example = "Иван")
        String firstName,

        @Schema(example = "Иванов")
        String lastName,

        @Schema(example = "user@mail.ru")
        String email
) {
}
