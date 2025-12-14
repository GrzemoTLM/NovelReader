package org.example.novelreader.service;

import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.BookmarkRequest;
import org.example.novelreader.dto.BookmarkResponse;
import org.example.novelreader.dto.ChapterDto;
import org.example.novelreader.dto.EpubDto;
import org.example.novelreader.entity.Book;
import org.example.novelreader.entity.Bookmark;
import org.example.novelreader.entity.User;
import org.example.novelreader.exception.ResourceNotFoundException;
import org.example.novelreader.repository.BookRepository;
import org.example.novelreader.repository.BookmarkRepository;
import org.example.novelreader.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final EpubService epubService;

    @Override
    public BookmarkResponse createBookmark(Long userId, BookmarkRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik", userId));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Książka", request.getBookId()));

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .book(book)
                .chapterIndex(request.getChapterIndex())
                .characterOffset(request.getCharacterOffset())
                .progressPercent(request.getProgressPercent())
                .title(request.getTitle())
                .note(request.getNote())
                .textSnippet(request.getTextSnippet())
                .color(request.getColor())
                .build();

        Bookmark saved = bookmarkRepository.save(bookmark);
        return mapToResponse(saved);
    }

    @Override
    public BookmarkResponse updateBookmark(Long userId, Long bookmarkId, BookmarkRequest request) {
        Bookmark bookmark = bookmarkRepository.findByIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Zakładka", bookmarkId));

        bookmark.setChapterIndex(request.getChapterIndex());
        bookmark.setCharacterOffset(request.getCharacterOffset());
        bookmark.setProgressPercent(request.getProgressPercent());
        bookmark.setTitle(request.getTitle());
        bookmark.setNote(request.getNote());
        bookmark.setTextSnippet(request.getTextSnippet());
        bookmark.setColor(request.getColor());

        Bookmark saved = bookmarkRepository.save(bookmark);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookmarkResponse getBookmark(Long userId, Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findByIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Zakładka", bookmarkId));
        return mapToResponse(bookmark);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getBookmarksForBook(Long userId, Long bookId) {
        return bookmarkRepository.findByUserIdAndBookIdOrderByChapterIndexAscCharacterOffsetAsc(userId, bookId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getAllUserBookmarks(Long userId) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBookmark(Long userId, Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findByIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Zakładka", bookmarkId));
        bookmarkRepository.delete(bookmark);
    }

    @Override
    public void deleteAllBookmarksForBook(Long userId, Long bookId) {
        bookmarkRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean bookmarkExists(Long userId, Long bookId, Integer chapterIndex, Integer characterOffset) {
        return bookmarkRepository.existsByUserIdAndBookIdAndChapterIndexAndCharacterOffset(
                userId, bookId, chapterIndex, characterOffset);
    }

    private BookmarkResponse mapToResponse(Bookmark bookmark) {
        String chapterTitle = null;

        try {
            Book book = bookmark.getBook();
            if (book.getFilePath() != null) {
                EpubDto epub = epubService.parseEpubFromFilePath(book.getFilePath());
                List<ChapterDto> chapters = epub.getChapters();
                if (bookmark.getChapterIndex() < chapters.size()) {
                    chapterTitle = chapters.get(bookmark.getChapterIndex()).getTitle();
                }
            }
        } catch (IOException e) {
        }

        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .bookId(bookmark.getBook().getId())
                .bookTitle(bookmark.getBook().getTitle())
                .chapterIndex(bookmark.getChapterIndex())
                .chapterTitle(chapterTitle)
                .characterOffset(bookmark.getCharacterOffset())
                .progressPercent(bookmark.getProgressPercent())
                .label(bookmark.getTitle())
                .note(bookmark.getNote())
                .textSnippet(bookmark.getTextSnippet())
                .color(bookmark.getColor())
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .build();
    }
}

