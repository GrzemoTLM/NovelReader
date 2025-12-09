package org.core.novelreader_client;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
public class AuthService {
    private static final String BASE_URL = "http://localhost:8080/api/v1/auth";
    private final HttpClient httpClient;
    private static String authToken;
    public AuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    public CompletableFuture<AuthResult> login(String username, String password) {
        String jsonBody = String.format(
                "{\"usernameOrEmail\":\"%s\",\"password\":\"%s\"}",
                escapeJson(username),
                escapeJson(password)
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        String body = response.body();
                        authToken = extractToken(body);
                        return new AuthResult(true, "Zalogowano pomyslnie");
                    } else if (response.statusCode() == 401) {
                        return new AuthResult(false, "Nieprawidlowa nazwa uzytkownika lub haslo");
                    } else {
                        return new AuthResult(false, "Blad serwera: " + response.statusCode());
                    }
                });
    }
    public CompletableFuture<AuthResult> register(String username, String email, String password) {
        String jsonBody = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                escapeJson(username),
                escapeJson(email),
                escapeJson(password)
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        return new AuthResult(true, "Rejestracja zakonczona pomyslnie");
                    } else if (response.statusCode() == 409) {
                        return new AuthResult(false, "Uzytkownik o tej nazwie juz istnieje");
                    } else {
                        return new AuthResult(false, "Blad rejestracji: " + response.statusCode());
                    }
                });
    }
    public static String getAuthToken() {
        return authToken;
    }
    public static void logout() {
        authToken = null;
    }
    private String extractToken(String responseBody) {
        if (responseBody.contains("\"token\"")) {
            int start = responseBody.indexOf("\"token\":\"") + 9;
            int end = responseBody.indexOf("\"", start);
            if (start > 8 && end > start) {
                return responseBody.substring(start, end);
            }
        }
        return null;
    }
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    public static class AuthResult {
        private final boolean success;
        private final String message;
        public AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public boolean isSuccess() {
            return success;
        }
        public String getMessage() {
            return message;
        }
    }
}
