package org.example.novelreader.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String description;
    private LocalDateTime uploadedAt;
}
