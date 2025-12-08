package org.example.novelreader.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dane do rejestracji nowego użytkownika")
public class RegisterRequest {

    @Schema(description = "Nazwa użytkownika", example = "jan_kowalski")
    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi mieć od 3 do 50 znaków")
    private String username;

    @Schema(description = "Adres email", example = "jan@example.com")
    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    private String email;

    @Schema(description = "Hasło (min. 6 znaków)", example = "mojeHaslo123")
    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 6, max = 100, message = "Hasło musi mieć od 6 do 100 znaków")
    private String password;
}

