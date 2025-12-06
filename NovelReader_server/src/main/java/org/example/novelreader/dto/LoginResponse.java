package org.example.novelreader.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Odpowiedź po pomyślnym logowaniu")
public class LoginResponse {

    @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Typ tokena", example = "Bearer")
    private String type;

    @Schema(description = "ID użytkownika", example = "1")
    private Long userId;

    @Schema(description = "Nazwa użytkownika", example = "jan_kowalski")
    private String username;

    @Schema(description = "Email użytkownika", example = "jan@example.com")
    private String email;

    public static LoginResponse of(String token, Long userId, String username, String email) {
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(userId)
                .username(username)
                .email(email)
                .build();
    }
}

