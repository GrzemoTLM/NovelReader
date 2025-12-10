package org.example.novelreader.controller;

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
public class BookController {

    private final BookService bookService;
    private final BookProgressService progressService;
    private final EpubService epubService;
    private final CustomUserDetailsService customUserDetailsService;

    @PostMapping("/parse-metadata")
    public ResponseEntity<MetadataDto> parseMetadata(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        EpubDto parsed = epubService.parseEpub(file);
        return ResponseEntity.ok(parsed.getMetadata());
    }

    @PostMapping("/upload")
    public ResponseEntity<BookResponse> uploadBook(
            Authentication auth,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "author", required = false) String author,
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

    @GetMapping
    public ResponseEntity<List<BookResponse>> getUserBooks(Authentication auth) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(bookService.getUserBooks(user.getId()));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<EpubDto> getBookContent(
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(bookService.getParsedBook(user.getId(), id));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<String> getBookPreview(
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(bookService.getBookPreview(user.getId(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        bookService.deleteBook(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<BookProgressResponse> getProgress(
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(progressService.getProgress(user.getId(), id));
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<BookProgressResponse> updateProgress(
            @PathVariable Long id,
            Authentication auth,
            @RequestBody BookProgressRequest req
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(auth.getName());
        return ResponseEntity.ok(progressService.updateProgress(user.getId(), id, req));
    }
}
