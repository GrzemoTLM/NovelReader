package org.example.novelreader.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookProgressResponse {
    private Long bookId;
    private int chapterIndex;
    private int offsetInChapter;
}
