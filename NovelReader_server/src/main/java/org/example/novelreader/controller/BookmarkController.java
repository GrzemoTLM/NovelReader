package org.example.novelreader.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.BookmarkRequest;
import org.example.novelreader.dto.BookmarkResponse;
import org.example.novelreader.entity.User;
import org.example.novelreader.security.CustomUserDetailsService;
import org.example.novelreader.service.BookmarkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Zakładki", description = "Endpointy do zarządzania zakładkami w książkach")
@SecurityRequirement(name = "bearerAuth")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final CustomUserDetailsService customUserDetailsService;

    @Operation(summary = "Utwórz nową zakładkę",
               description = "Tworzy zakładkę w określonym miejscu rozdziału. " +
                            "characterOffset określa pozycję znaku od początku tekstu rozdziału.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zakładka została utworzona",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "404", description = "Książka nie została znaleziona")
    })
    @PostMapping
    public ResponseEntity<BookmarkResponse> createBookmark(
            Authentication auth,
            @Valid @RequestBody BookmarkRequest request
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        BookmarkResponse response = bookmarkService.createBookmark(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Pobierz zakładkę", description = "Pobiera szczegóły zakładki po jej ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zakładka została znaleziona",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "404", description = "Zakładka nie została znaleziona")
    })
    @GetMapping("/{bookmarkId}")
    public ResponseEntity<BookmarkResponse> getBookmark(
            Authentication auth,
            @Parameter(description = "ID zakładki") @PathVariable Long bookmarkId
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        BookmarkResponse response = bookmarkService.getBookmark(user.getId(), bookmarkId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Pobierz wszystkie zakładki użytkownika",
               description = "Zwraca listę wszystkich zakładek zalogowanego użytkownika, posortowanych od najnowszych")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista zakładek"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji")
    })
    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getAllUserBookmarks(Authentication auth) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        List<BookmarkResponse> bookmarks = bookmarkService.getAllUserBookmarks(user.getId());
        return ResponseEntity.ok(bookmarks);
    }

    @Operation(summary = "Pobierz zakładki dla książki",
               description = "Zwraca wszystkie zakładki użytkownika dla określonej książki, " +
                            "posortowane według rozdziału i pozycji w rozdziale")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista zakładek dla książki"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji")
    })
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<BookmarkResponse>> getBookmarksForBook(
            Authentication auth,
            @Parameter(description = "ID książki") @PathVariable Long bookId
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarksForBook(user.getId(), bookId);
        return ResponseEntity.ok(bookmarks);
    }

    @Operation(summary = "Aktualizuj zakładkę", description = "Aktualizuje istniejącą zakładkę")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zakładka została zaktualizowana",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "404", description = "Zakładka nie została znaleziona")
    })
    @PutMapping("/{bookmarkId}")
    public ResponseEntity<BookmarkResponse> updateBookmark(
            Authentication auth,
            @Parameter(description = "ID zakładki") @PathVariable Long bookmarkId,
            @Valid @RequestBody BookmarkRequest request
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        BookmarkResponse response = bookmarkService.updateBookmark(user.getId(), bookmarkId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Usuń zakładkę", description = "Usuwa zakładkę o podanym ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zakładka została usunięta"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji"),
            @ApiResponse(responseCode = "404", description = "Zakładka nie została znaleziona")
    })
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            Authentication auth,
            @Parameter(description = "ID zakładki") @PathVariable Long bookmarkId
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        bookmarkService.deleteBookmark(user.getId(), bookmarkId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Usuń wszystkie zakładki dla książki",
               description = "Usuwa wszystkie zakładki użytkownika dla określonej książki")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zakładki zostały usunięte"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji")
    })
    @DeleteMapping("/book/{bookId}")
    public ResponseEntity<Void> deleteAllBookmarksForBook(
            Authentication auth,
            @Parameter(description = "ID książki") @PathVariable Long bookId
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        bookmarkService.deleteAllBookmarksForBook(user.getId(), bookId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Sprawdź czy zakładka istnieje",
               description = "Sprawdza czy użytkownik ma już zakładkę w dokładnie tym samym miejscu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wynik sprawdzenia (true/false)"),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji")
    })
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkBookmarkExists(
            Authentication auth,
            @Parameter(description = "ID książki") @RequestParam Long bookId,
            @Parameter(description = "Indeks rozdziału (0-based)") @RequestParam Integer chapterIndex,
            @Parameter(description = "Offset znakowy w rozdziale") @RequestParam Integer characterOffset
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        boolean exists = bookmarkService.bookmarkExists(user.getId(), bookId, chapterIndex, characterOffset);
        return ResponseEntity.ok(exists);
    }
}

