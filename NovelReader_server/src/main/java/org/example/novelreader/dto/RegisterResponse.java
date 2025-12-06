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
@Schema(description = "Odpowiedź po pomyślnej rejestracji")
public class RegisterResponse {

    @Schema(description = "ID utworzonego użytkownika", example = "1")
    private Long id;

    @Schema(description = "Nazwa użytkownika", example = "jan_kowalski")
    private String username;

    @Schema(description = "Email użytkownika", example = "jan@example.com")
    private String email;

    @Schema(description = "Komunikat", example = "Rejestracja zakończona pomyślnie")
    private String message;
}

