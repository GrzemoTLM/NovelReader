package org.example.novelreader.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private Integer chapterIndex;
    private String chapterTitle;
    private Integer characterOffset;
    private Double progressPercent;
    private String label;
    private String note;
    private String textSnippet;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

