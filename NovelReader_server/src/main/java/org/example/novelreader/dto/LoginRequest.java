package org.example.novelreader.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Login lub email jest wymagany")
    private String usernameOrEmail;

    @NotBlank(message = "Hasło jest wymagane")
    private String password;
}

