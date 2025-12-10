package org.core.novelreader_client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("AuthService - testy jednostkowe")
class AuthServiceTest {
    @BeforeEach
    void setUp() {
        AuthService.logout();
    }
    @Test
    @DisplayName("Token powinien byc null po wylogowaniu")
    void shouldHaveNullTokenAfterLogout() {
        AuthService.logout();
        assertNull(AuthService.getAuthToken());
    }
    @Test
    @DisplayName("AuthResult powinien poprawnie przechowywac sukces")
    void authResultShouldStoreSuccessCorrectly() {
        AuthService.AuthResult result = new AuthService.AuthResult(true, "Zalogowano");
        assertTrue(result.isSuccess());
        assertEquals("Zalogowano", result.getMessage());
    }
    @Test
    @DisplayName("AuthResult powinien poprawnie przechowywac blad")
    void authResultShouldStoreErrorCorrectly() {
        AuthService.AuthResult result = new AuthService.AuthResult(false, "Nieprawidlowe haslo");
        assertFalse(result.isSuccess());
        assertEquals("Nieprawidlowe haslo", result.getMessage());
    }
}
