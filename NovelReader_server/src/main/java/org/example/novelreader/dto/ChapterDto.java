package org.example.novelreader.dto;


import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class ChapterDto {
    private final int index; // 0-based
    private final String title;
    private final String html; // original or sanitized HTML
    private final String text; // plain text extracted from html
}