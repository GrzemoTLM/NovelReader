package org.example.novelreader.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dane do logowania")
public class LoginRequest {

    @Schema(description = "Nazwa użytkownika lub email", example = "jan_kowalski")
    @NotBlank(message = "Login lub email jest wymagany")
    private String usernameOrEmail;

    @Schema(description = "Hasło", example = "mojeHaslo123")
    @NotBlank(message = "Hasło jest wymagane")
    private String password;
}

