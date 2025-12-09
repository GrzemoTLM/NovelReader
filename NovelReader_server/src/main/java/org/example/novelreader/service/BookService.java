package org.example.novelreader.service;

import org.example.novelreader.dto.BookRequest;
import org.example.novelreader.dto.BookResponse;
import org.example.novelreader.entity.Book;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    BookResponse uploadBook(Long userId, BookRequest request);
    List<BookResponse> getUserBooks(Long userId);
    void deleteBook(Long userId, Long bookId);
    Book getBookByIdAndUser(Long userId, Long bookId);
}
