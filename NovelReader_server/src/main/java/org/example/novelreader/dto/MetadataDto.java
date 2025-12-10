package org.example.novelreader.dto;


import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class MetadataDto {
    private final String title;
    private final String author;
    private final String language;
    private final String identifier;
    private final String description;
}