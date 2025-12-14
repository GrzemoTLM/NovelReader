package org.example.novelreader.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.LoginRequest;
import org.example.novelreader.dto.LoginResponse;
import org.example.novelreader.dto.RegisterRequest;
import org.example.novelreader.dto.RegisterResponse;
import org.example.novelreader.service.AuthService;
import org.example.novelreader.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// login i rejestracja - reszta idzie do serwisów (SRP - Single Responsibility)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autoryzacja", description = "Endpointy do rejestracji i logowania użytkowników")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Rejestracja nowego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Użytkownik zarejestrowany pomyślnie",
                    content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych"),
            @ApiResponse(responseCode = "409", description = "Użytkownik o podanej nazwie lub emailu już istnieje")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Logowanie użytkownika", description = "Logowanie po nazwie użytkownika lub emailu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zalogowano pomyślnie, zwraca token JWT",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych"),
            @ApiResponse(responseCode = "401", description = "Nieprawidłowy login lub hasło")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
