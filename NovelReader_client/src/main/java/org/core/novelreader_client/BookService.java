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
                            List<BookDto> books = objectMapper.readValue(body, new TypeReference<>() {});
                            ObservableList<BookViewModel> observableList = FXCollections.observableArrayList();
                            books.forEach(book -> observableList.add(new BookViewModel(book)));
                            return observableList;
                        } catch (IOException e) {
                            throw new RuntimeException("Nie udało się sparsować danych książek", e);
                        }
                    } else if (response.statusCode() == 403) {
                        throw new RuntimeException("Dostęp zabroniony. Zaloguj się ponownie.");
                    } else if (response.statusCode() == 401) {
                        throw new RuntimeException("Sesja wygasła. Zaloguj się ponownie.");
                    }
                    throw new RuntimeException("Błąd pobierania książek: " + response.statusCode());
                });
    }

    public CompletableFuture<MetadataDto> parseMetadata(Path filePath) {
        String token = AuthService.getAuthToken();
        if (token == null || token.isBlank()) {
            CompletableFuture<MetadataDto> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("Brak tokenu"));
            return failed;
        }

        try {
            byte[] fileContent = Files.readAllBytes(filePath);
            String fileName = filePath.getFileName().toString();
            String boundary = "----FormBoundary" + System.currentTimeMillis();

            String CRLF = "\r\n";
            String sb = "--" + boundary + CRLF +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + CRLF +
                    "Content-Type: application/octet-stream" + CRLF + CRLF;

            byte[] header = sb.getBytes();
            byte[] footer = (CRLF + "--" + boundary + "--" + CRLF).getBytes();
            byte[] body = new byte[header.length + fileContent.length + footer.length];
            System.arraycopy(header, 0, body, 0, header.length);
            System.arraycopy(fileContent, 0, body, header.length, fileContent.length);
            System.arraycopy(footer, 0, body, header.length + fileContent.length, footer.length);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BOOKS_URL + "/parse-metadata"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), MetadataDto.class);
                            } catch (IOException e) {
                                throw new RuntimeException("Błąd parsowania odpowiedzi", e);
                            }
                        }
                        throw new RuntimeException("Błąd parsowania metadanych: " + response.statusCode());
                    });
        } catch (IOException e) {
            CompletableFuture<MetadataDto> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("Błąd odczytu pliku", e));
            return failed;
        }
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
                                throw new RuntimeException("Nie udało się sparsować odpowiedzi", e);
                            }
                        } else if (response.statusCode() == 401) {
                            throw new RuntimeException("Sesja wygasła. Zaloguj się ponownie.");
                        } else if (response.statusCode() == 403) {
                            throw new RuntimeException("Dostęp zabroniony.");
                        }
                        throw new RuntimeException("Błąd uploadu: " + response.statusCode());
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
            failed.completeExceptionally(new IllegalStateException("Brak tokenu"));
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
                        throw new RuntimeException("Sesja wygasła.");
                    } else if (response.statusCode() == 404) {
                        throw new RuntimeException("Książka nie znaleziona.");
                    }
                    throw new RuntimeException("Błąd usuwania: " + response.statusCode());
                });
    }

    public CompletableFuture<EpubDto> getParsedBook(Long bookId) {
        String token = AuthService.getAuthToken();
        if (token == null || token.isBlank()) {
            CompletableFuture<EpubDto> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("Brak tokenu"));
            return failed;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BOOKS_URL + "/" + bookId + "/content"))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), EpubDto.class);
                        } catch (IOException e) {
                            throw new RuntimeException("Błąd parsowania odpowiedzi", e);
                        }
                    }
                    throw new RuntimeException("Błąd pobierania książki: " + response.statusCode());
                });
    }

    public CompletableFuture<ProgressDto> getProgress(Long bookId) {
        String token = AuthService.getAuthToken();
        if (token == null || token.isBlank()) {
            return CompletableFuture.completedFuture(new ProgressDto(bookId, 0, 0));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BOOKS_URL + "/" + bookId + "/progress"))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), ProgressDto.class);
                        } catch (IOException e) {
                            return new ProgressDto(bookId, 0, 0);
                        }
                    }
                    return new ProgressDto(bookId, 0, 0);
                })
                .exceptionally(ex -> new ProgressDto(bookId, 0, 0));
    }

    public CompletableFuture<Void> saveProgress(Long bookId, int chapterIndex, int offset) {
        String token = AuthService.getAuthToken();
        if (token == null || token.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        String json = "{\"chapterIndex\":" + chapterIndex + ",\"offsetInChapter\":" + offset + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BOOKS_URL + "/" + bookId + "/progress"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .<Void>thenApply(response -> null)
                .exceptionally(ex -> null);
    }

    private byte[] buildMultipartBody(String boundary, byte[] fileContent, String fileName,
                                      String title, String author, String description) {
        String CRLF = "\r\n";

        String sb = "--" + boundary + CRLF +
                "Content-Disposition: form-data; name=\"title\"" + CRLF + CRLF +
                title + CRLF +
                "--" + boundary + CRLF +
                "Content-Disposition: form-data; name=\"author\"" + CRLF + CRLF +
                author + CRLF +
                "--" + boundary + CRLF +
                "Content-Disposition: form-data; name=\"description\"" + CRLF + CRLF +
                description + CRLF +
                "--" + boundary + CRLF +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + CRLF +
                "Content-Type: application/octet-stream" + CRLF + CRLF;

        byte[] header = sb.getBytes();
        byte[] footer = (CRLF + "--" + boundary + "--" + CRLF).getBytes();

        byte[] result = new byte[header.length + fileContent.length + footer.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(fileContent, 0, result, header.length, fileContent.length);
        System.arraycopy(footer, 0, result, header.length + fileContent.length, footer.length);

        return result;
    }

    public record BookDto(Long id, String title, String author, String description, String uploadedAt) {}
    public record EpubDto(MetadataDto metadata, List<ChapterDto> chapters) {}
    public record ChapterDto(int index, String title, String html, String text) {}
    public record MetadataDto(String title, String author, String language, String identifier, String description) {}
    public record ProgressDto(Long bookId, int chapterIndex, int offsetInChapter) {}
}

