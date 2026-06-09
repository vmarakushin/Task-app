package authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ после авторизации")
public record AuthResponse(

        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
        String token
) {
}
