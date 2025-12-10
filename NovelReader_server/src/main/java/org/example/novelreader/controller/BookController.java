package org.example.novelreader.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.*;
import org.example.novelreader.entity.User;
import org.example.novelreader.security.CustomUserDetailsService;
import org.example.novelreader.service.BookService;
import org.example.novelreader.service.BookProgressService;
import org.example.novelreader.service.EpubService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Książki", description = "Endpointy do zarządzania książkami użytkownika")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;
    private final BookProgressService progressService;
    private final EpubService epubService;
    private final CustomUserDetailsService customUserDetailsService;

    @Operation(summary = "Parsowanie metadanych EPUB", description = "Przesyła plik EPUB i zwraca jego metadane (tytuł, autor, opis itp.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadane zostały pomyślnie sparsowane",
                    content = @Content(schema = @Schema(implementation = MetadataDto.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy format pliku"),
            @ApiResponse(responseCode = "500", description = "Błąd podczas parsowania pliku")
    })
    @PostMapping("/parse-metadata")
    public ResponseEntity<MetadataDto> parseMetadata(
            @Parameter(description = "Plik EPUB do sparsowania", required = true)
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        EpubDto parsed = epubService.parseEpub(file);
        return ResponseEntity.ok(parsed.getMetadata());
    }

    @Operation(summary = "Przesłanie nowej książki", description = "Przesyła plik EPUB i zapisuje książkę w bibliotece użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka została pomyślnie przesłana",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "500", description = "Błąd podczas zapisywania książki")
    })
    @PostMapping("/upload")
    public ResponseEntity<BookResponse> uploadBook(
            Authentication auth,
            @Parameter(description = "Plik EPUB do przesłania", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Tytuł książki", required = true)
            @RequestParam("title") String title,
            @Parameter(description = "Autor książki")
            @RequestParam(value = "author", required = false) String author,
            @Parameter(description = "Opis książki")
            @RequestParam(value = "description", required = false) String description
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        BookRequest req = BookRequest.builder()
                .file(file)
                .title(title)
                .author(author)
                .description(description)
                .build();

        return ResponseEntity.ok(bookService.uploadBook(user.getId(), req));
    }

    @Operation(summary = "Pobranie listy książek użytkownika", description = "Zwraca wszystkie książki należące do zalogowanego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista książek została pobrana pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji")
    })
    @GetMapping
    public ResponseEntity<List<BookResponse>> getUserBooks(Authentication auth) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(bookService.getUserBooks(user.getId()));
    }

    @Operation(summary = "Pobranie pełnej treści książki", description = "Zwraca sparsowaną zawartość książki EPUB (metadane + rozdziały)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treść książki została pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = EpubDto.class))),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tej książki"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @GetMapping("/{id}/content")
    public ResponseEntity<EpubDto> getBookContent(
            @Parameter(description = "ID książki", required = true)
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(bookService.getParsedBook(user.getId(), id));
    }

    @Operation(summary = "Pobranie podglądu książki", description = "Zwraca krótki podgląd tekstowy zawartości książki")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Podgląd książki został pobrany pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tej książki"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @GetMapping("/{id}/preview")
    public ResponseEntity<String> getBookPreview(
            @Parameter(description = "ID książki", required = true)
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(bookService.getBookPreview(user.getId(), id));
    }

    @Operation(summary = "Usunięcie książki", description = "Usuwa książkę z biblioteki użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Książka została pomyślnie usunięta"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tej książki"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID książki", required = true)
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        bookService.deleteBook(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Pobranie postępu czytania", description = "Zwraca aktualny postęp czytania książki przez użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Postęp czytania został pobrany pomyślnie",
                    content = @Content(schema = @Schema(implementation = BookProgressResponse.class))),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tej książki"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @GetMapping("/{id}/progress")
    public ResponseEntity<BookProgressResponse> getProgress(
            @Parameter(description = "ID książki", required = true)
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(progressService.getProgress(user.getId(), id));
    }

    @Operation(summary = "Aktualizacja postępu czytania", description = "Zapisuje aktualny postęp czytania książki (numer rozdziału, pozycja itp.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Postęp czytania został zaktualizowany pomyślnie",
                    content = @Content(schema = @Schema(implementation = BookProgressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tej książki"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @PostMapping("/{id}/progress")
    public ResponseEntity<BookProgressResponse> updateProgress(
            @Parameter(description = "ID książki", required = true)
            @PathVariable Long id,
            Authentication auth,
            @RequestBody BookProgressRequest req
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(progressService.updateProgress(user.getId(), id, req));
    }
}
