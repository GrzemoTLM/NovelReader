package org.core.novelreader_client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BookService {
    private static final String BOOKS_URL = "http://localhost:8080/api/v1/books";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BookService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    public CompletableFuture<ObservableList<BookViewModel>> fetchBooks() {
        String token = AuthService.getAuthToken();
        if (token == null || token.isBlank()) {
            CompletableFuture<ObservableList<BookViewModel>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("Brak tokenu uwierzytelniającego"));
            return failed;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BOOKS_URL))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            String body = response.body();
                            if (body == null || body.trim().isEmpty()) {
                                return FXCollections.observableArrayList();
                            }
                            List<BookDto> books = objectMapper.readValue(
                                    body, new TypeReference<>() {}
                            );
                            ObservableList<BookViewModel> observableList = FXCollections.observableArrayList();
                            books.forEach(book -> observableList.add(new BookViewModel(book)));
                            return observableList;
                        } catch (IOException e) {
                            throw new RuntimeException("Nie udało się sparsować danych książek", e);
                        }
                    } else if (response.statusCode() == 403) {
                        throw new RuntimeException("Dostęp do zasobu zabroniony (403). Spróbuj zalogować się ponownie.");
                    } else if (response.statusCode() == 401) {
                        throw new RuntimeException("Sesja wygasła. Zaloguj się ponownie.");
                    }
                    throw new RuntimeException("Błąd pobierania książek: " + response.statusCode() + " - " + response.body());
                });
    }

    public CompletableFuture<BookDto> uploadBook(Path filePath, String title, String author, String description) {
        String token = AuthService.getAuthToken();
        if (token == null || token.isBlank()) {
            CompletableFuture<BookDto> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("Brak tokenu uwierzytelniającego"));
            return failed;
        }

        try {
            byte[] fileContent = Files.readAllBytes(filePath);
            String fileName = filePath.getFileName().toString();
            String boundary = "----FormBoundary" + System.currentTimeMillis();
            byte[] body = buildMultipartBody(boundary, fileContent, fileName, title, author, description);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BOOKS_URL + "/upload"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            try {
                                return objectMapper.readValue(response.body(), BookDto.class);
                            } catch (IOException e) {
                                throw new RuntimeException("Nie udało się sparsować odpowiedzi serwera", e);
                            }
                        } else if (response.statusCode() == 401) {
                            throw new RuntimeException("Sesja wygasła. Zaloguj się ponownie.");
                        } else if (response.statusCode() == 403) {
                            throw new RuntimeException("Dostęp zabroniony.");
                        }
                        throw new RuntimeException("Błąd uploadu: " + response.statusCode() + " - " + response.body());
                    });
        } catch (IOException e) {
            CompletableFuture<BookDto> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("Błąd odczytu pliku", e));
            return failed;
        }
    }

    public CompletableFuture<Void> deleteBook(Long bookId) {
        String token = AuthService.getAuthToken();
        if (token == null || token.isBlank()) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("Brak tokenu uwierzytelniającego"));
            return failed;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BOOKS_URL + "/" + bookId))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(30))
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 204 || response.statusCode() == 200) {
                        return null;
                    } else if (response.statusCode() == 401) {
                        throw new RuntimeException("Sesja wygasła. Zaloguj się ponownie.");
                    } else if (response.statusCode() == 403) {
                        throw new RuntimeException("Dostęp zabroniony.");
                    } else if (response.statusCode() == 404) {
                        throw new RuntimeException("Książka nie znaleziona.");
                    }
                    throw new RuntimeException("Błąd usuwania: " + response.statusCode() + " - " + response.body());
                });
    }

    private byte[] buildMultipartBody(String boundary, byte[] fileContent, String fileName,
                                      String title, String author, String description) throws IOException {
        String CRLF = "\r\n";
        String sb = "";

        sb += "--" + boundary + CRLF;
        sb += "Content-Disposition: form-data; name=\"title\"" + CRLF;
        sb += CRLF;
        sb += title + CRLF;

        sb += "--" + boundary + CRLF;
        sb += "Content-Disposition: form-data; name=\"author\"" + CRLF;
        sb += CRLF;
        sb += author + CRLF;

        sb += "--" + boundary + CRLF;
        sb += "Content-Disposition: form-data; name=\"description\"" + CRLF;
        sb += CRLF;
        sb += description + CRLF;

        sb += "--" + boundary + CRLF;
        sb += "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + CRLF;
        sb += "Content-Type: application/octet-stream" + CRLF;
        sb += CRLF;

        byte[] header = sb.getBytes();
        byte[] footer = (CRLF + "--" + boundary + "--" + CRLF).getBytes();

        byte[] result = new byte[header.length + fileContent.length + footer.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(fileContent, 0, result, header.length, fileContent.length);
        System.arraycopy(footer, 0, result, header.length + fileContent.length, footer.length);

        return result;
    }

    public record BookDto(Long id, String title, String author, String description, String uploadedAt) {}
}

