package org.example.novelreader.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EpubDto - Testy jednostkowe")
class EpubDtoTest {

    @Test
    @DisplayName("givenValidMetadataAndChapters_whenBuildingEpubDto_thenShouldCreateInstanceWithAllFieldsPopulatedCorrectly")
    void givenValidMetadataAndChapters_whenBuildingEpubDto_thenShouldCreateInstanceWithAllFieldsPopulatedCorrectly() {
        // given
        MetadataDto metadata = MetadataDto.builder()
                .title("Baśnie Andersena")
                .author("Hans Christian Andersen")
                .language("pl")
                .build();

        ChapterDto chapter1 = ChapterDto.builder()
                .index(0)
                .title("Brzydkie kaczątko")
                .html("<p>Dawno, dawno temu...</p>")
                .text("Dawno, dawno temu...")
                .build();

        ChapterDto chapter2 = ChapterDto.builder()
                .index(1)
                .title("Królowa Śniegu")
                .html("<p>W wielkim mieście...</p>")
                .text("W wielkim mieście...")
                .build();

        List<ChapterDto> chapters = Arrays.asList(chapter1, chapter2);

        // when
        EpubDto epubDto = EpubDto.builder()
                .metadata(metadata)
                .chapters(chapters)
                .build();

        // then
        assertNotNull(epubDto);
        assertEquals(metadata, epubDto.getMetadata());
        assertEquals(chapters, epubDto.getChapters());
        assertEquals(2, epubDto.getChapters().size());
    }

    @Test
    @DisplayName("givenEmptyChaptersList_whenBuildingEpubDto_thenShouldCreateInstanceWithEmptyChaptersCollection")
    void givenEmptyChaptersList_whenBuildingEpubDto_thenShouldCreateInstanceWithEmptyChaptersCollection() {
        // given
        MetadataDto metadata = MetadataDto.builder()
                .title("Pusta książka")
                .build();

        // when
        EpubDto epubDto = EpubDto.builder()
                .metadata(metadata)
                .chapters(Collections.emptyList())
                .build();

        // then
        assertNotNull(epubDto);
        assertNotNull(epubDto.getChapters());
        assertTrue(epubDto.getChapters().isEmpty());
    }

    @Test
    @DisplayName("givenNullMetadataAndChapters_whenBuildingEpubDto_thenShouldCreateInstanceWithNullFields")
    void givenNullMetadataAndChapters_whenBuildingEpubDto_thenShouldCreateInstanceWithNullFields() {
        // when
        EpubDto epubDto = EpubDto.builder()
                .metadata(null)
                .chapters(null)
                .build();

        // then
        assertNotNull(epubDto);
        assertNull(epubDto.getMetadata());
        assertNull(epubDto.getChapters());
    }

    @Test
    @DisplayName("givenEpubDtoWithMultipleChapters_whenAccessingChaptersByIndex_thenShouldReturnChaptersInCorrectOrder")
    void givenEpubDtoWithMultipleChapters_whenAccessingChaptersByIndex_thenShouldReturnChaptersInCorrectOrder() {
        // given
        ChapterDto chapter1 = ChapterDto.builder().index(0).title("Wstęp").build();
        ChapterDto chapter2 = ChapterDto.builder().index(1).title("Rozdział 1").build();
        ChapterDto chapter3 = ChapterDto.builder().index(2).title("Epilog").build();

        EpubDto epubDto = EpubDto.builder()
                .metadata(MetadataDto.builder().title("Książka").build())
                .chapters(Arrays.asList(chapter1, chapter2, chapter3))
                .build();

        // when & then
        assertEquals(3, epubDto.getChapters().size());
        assertEquals("Wstęp", epubDto.getChapters().get(0).getTitle());
        assertEquals("Rozdział 1", epubDto.getChapters().get(1).getTitle());
        assertEquals("Epilog", epubDto.getChapters().get(2).getTitle());
    }

    @Test
    @DisplayName("givenEpubDtoWithPolishContent_whenAccessingFields_thenShouldPreservePolishCharacters")
    void givenEpubDtoWithPolishContent_whenAccessingFields_thenShouldPreservePolishCharacters() {
        // given
        String polishTitle = "Zażółć gęślą jaźń";
        String polishChapterTitle = "Żółta łódź";

        MetadataDto metadata = MetadataDto.builder()
                .title(polishTitle)
                .build();

        ChapterDto chapter = ChapterDto.builder()
                .index(0)
                .title(polishChapterTitle)
                .build();

        EpubDto epubDto = EpubDto.builder()
                .metadata(metadata)
                .chapters(List.of(chapter))
                .build();

        // when & then
        assertEquals(polishTitle, epubDto.getMetadata().getTitle());
        assertEquals(polishChapterTitle, epubDto.getChapters().get(0).getTitle());
    }
}

