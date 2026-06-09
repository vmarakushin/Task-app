package authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ после регистрации")
public record RegisterResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "user@example.com")
        String email
) {
}
