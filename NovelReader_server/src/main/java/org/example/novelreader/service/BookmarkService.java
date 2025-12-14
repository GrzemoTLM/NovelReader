package org.example.novelreader.service;

import org.example.novelreader.dto.BookmarkRequest;
import org.example.novelreader.dto.BookmarkResponse;

import java.util.List;

public interface BookmarkService {

    BookmarkResponse createBookmark(Long userId, BookmarkRequest request);

    BookmarkResponse updateBookmark(Long userId, Long bookmarkId, BookmarkRequest request);

    BookmarkResponse getBookmark(Long userId, Long bookmarkId);

    List<BookmarkResponse> getBookmarksForBook(Long userId, Long bookId);

    List<BookmarkResponse> getAllUserBookmarks(Long userId);

    void deleteBookmark(Long userId, Long bookmarkId);

    void deleteAllBookmarksForBook(Long userId, Long bookId);

    boolean bookmarkExists(Long userId, Long bookId, Integer chapterIndex, Integer characterOffset);
}

