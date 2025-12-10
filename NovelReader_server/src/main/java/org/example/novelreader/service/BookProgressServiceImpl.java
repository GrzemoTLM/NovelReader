package org.example.novelreader.service;

import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.BookProgressRequest;
import org.example.novelreader.dto.BookProgressResponse;
import org.example.novelreader.entity.Book;
import org.example.novelreader.entity.BookProgress;
import org.example.novelreader.entity.User;
import org.example.novelreader.repository.BookProgressRepository;
import org.example.novelreader.repository.BookRepository;
import org.example.novelreader.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookProgressServiceImpl implements BookProgressService {

    private final BookProgressRepository repo;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    public BookProgressResponse getProgress(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return repo.findByUserAndBook(user, book)
                .map(p -> BookProgressResponse.builder()
                        .bookId(bookId)
                        .chapterIndex(p.getChapterIndex())
                        .offsetInChapter(p.getOffsetInChapter())
                        .build())
                .orElse(BookProgressResponse.builder()
                        .bookId(bookId)
                        .chapterIndex(0)
                        .offsetInChapter(0)
                        .build());
    }

    @Override
    public BookProgressResponse updateProgress(Long userId, Long bookId, BookProgressRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        BookProgress progress = repo.findByUserAndBook(user, book)
                .orElse(BookProgress.builder()
                        .user(user)
                        .book(book)
                        .chapterIndex(0)
                        .offsetInChapter(0)
                        .build());

        progress.setChapterIndex(req.getChapterIndex());
        progress.setOffsetInChapter(req.getOffsetInChapter());

        repo.save(progress);

        return BookProgressResponse.builder()
                .bookId(bookId)
                .chapterIndex(progress.getChapterIndex())
                .offsetInChapter(progress.getOffsetInChapter())
                .build();
    }
}
