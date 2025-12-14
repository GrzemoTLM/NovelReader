package org.example.novelreader.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkRequest {

    private Long bookId;

    @Min(value = 0, message = "Indeks rozdziału musi być >= 0")
    private Integer chapterIndex;

    @Min(value = 0, message = "Offset znakowy musi być >= 0")
    private Integer characterOffset;

    private Double progressPercent;

    @Size(max = 255, message = "Tytuł może mieć maksymalnie 255 znaków")
    @JsonAlias("label")
    private String title;

    @Size(max = 1000, message = "Notatka może mieć maksymalnie 1000 znaków")
    private String note;

    @Size(max = 500, message = "Fragment tekstu może mieć maksymalnie 500 znaków")
    private String textSnippet;

    @Size(max = 7, message = "Kolor musi być w formacie hex (np. #FF5733)")
    private String color;
}

