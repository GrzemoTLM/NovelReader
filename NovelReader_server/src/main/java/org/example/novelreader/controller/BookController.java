package org.example.novelreader.controller;

import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.BookRequest;
import org.example.novelreader.dto.BookResponse;
import org.example.novelreader.entity.User;
import org.example.novelreader.security.CustomUserDetailsService;
import org.example.novelreader.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final CustomUserDetailsService customUserDetailsService;

    @PostMapping("/upload")
    public ResponseEntity<BookResponse> uploadBook(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "description", required = false) String description
    ) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(authentication.getName());
        Long userId = user.getId();

        BookRequest request = new BookRequest();
        request.setFile(file);
        request.setTitle(title);
        request.setAuthor(author);
        request.setDescription(description);

        BookResponse response = bookService.uploadBook(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getUserBooks(Authentication authentication) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(authentication.getName());
        Long userId = user.getId();
        return ResponseEntity.ok(bookService.getUserBooks(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id, Authentication authentication) {
        User user = customUserDetailsService.findUserByUsernameOrEmail(authentication.getName());
        Long userId = user.getId();
        bookService.deleteBook(userId, id);
        return ResponseEntity.noContent().build();
    }
}
