package org.example.novelreader.service;

import org.example.novelreader.dto.BookRequest;
import org.example.novelreader.dto.BookResponse;
import org.example.novelreader.dto.EpubDto;
import org.example.novelreader.entity.Book;

import java.util.List;

public interface BookService {
    BookResponse uploadBook(Long userId, BookRequest request);
    List<BookResponse> getUserBooks(Long userId);
    void deleteBook(Long userId, Long bookId);
    Book getBookByIdAndUser(Long userId, Long bookId);
    EpubDto getParsedBook(Long userId, Long bookId);
    String getBookPreview(Long userId, Long bookId);
}
