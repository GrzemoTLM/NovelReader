package org.example.novelreader.dto;

import lombok.Data;

@Data
public class BookProgressRequest {
    private int chapterIndex;
    private int offsetInChapter;
}
