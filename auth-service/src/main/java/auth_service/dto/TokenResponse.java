package auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ после авторизации")
public record TokenResponse(

        @Schema(description = "Токен для доступа", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
        String accessToken,

        @Schema(description = "Токен для обновления", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
        String refreshToken,

        @Schema(description = "Срок истечения токена в секундах", example = "300")
        long expiresIn
) {
}
