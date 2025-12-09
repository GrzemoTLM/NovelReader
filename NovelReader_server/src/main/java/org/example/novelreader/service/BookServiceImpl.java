package org.example.novelreader.service;

import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.BookRequest;
import org.example.novelreader.dto.BookResponse;
import org.example.novelreader.entity.Book;
import org.example.novelreader.entity.User;
import org.example.novelreader.repository.BookRepository;
import org.example.novelreader.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Value("${book.storage.path}")
    private String storagePath;

    @Override
    public BookResponse uploadBook(Long userId, BookRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String userDirPath = storagePath + "/" + userId;
        new File(userDirPath).mkdirs();
        String filePath = userDirPath + "/" + System.currentTimeMillis() + "_" + filename;

        try {
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        Book book = Book.builder()
                .owner(user)
                .title(request.getTitle())
                .author(request.getAuthor())
                .description(request.getDescription())
                .filePath(filePath)
                .uploadedAt(LocalDateTime.now())
                .build();

        bookRepository.save(book);

        return mapToResponse(book);
    }

    @Override
    public List<BookResponse> getUserBooks(Long userId) {
        return bookRepository.findByOwnerId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBook(Long userId, Long bookId) {
        Book book = getBookByIdAndUser(userId, bookId);
        File file = new File(book.getFilePath());
        if (file.exists()) file.delete();
        bookRepository.delete(book);
    }

    @Override
    public Book getBookByIdAndUser(Long userId, Long bookId) {
        return bookRepository.findById(bookId)
                .filter(b -> b.getOwner().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Book not found or access denied"));
    }

    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .uploadedAt(book.getUploadedAt())
                .build();
    }
}
