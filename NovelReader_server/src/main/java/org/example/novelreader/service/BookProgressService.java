package org.example.novelreader.service;

import org.example.novelreader.dto.BookProgressRequest;
import org.example.novelreader.dto.BookProgressResponse;

public interface BookProgressService {
    BookProgressResponse getProgress(Long userId, Long bookId);
    BookProgressResponse updateProgress(Long userId, Long bookId, BookProgressRequest request);
}
