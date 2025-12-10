package org.example.novelreader.dto;


import lombok.Builder;
import lombok.Getter;


import java.util.List;


@Getter
@Builder
public class EpubDto {
    private final MetadataDto metadata;
    private final List<ChapterDto> chapters;
}