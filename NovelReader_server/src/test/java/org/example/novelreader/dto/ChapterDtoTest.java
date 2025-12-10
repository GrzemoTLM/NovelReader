package org.example.novelreader.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChapterDto - Testy jednostkowe")
class ChapterDtoTest {

    @Test
    @DisplayName("givenAllChapterFields_whenBuildingChapterDto_thenShouldCreateInstanceWithAllFieldsPopulatedCorrectly")
    void givenAllChapterFields_whenBuildingChapterDto_thenShouldCreateInstanceWithAllFieldsPopulatedCorrectly() {
        // given
        int expectedIndex = 0;
        String expectedTitle = "Rozdział pierwszy: Początek przygody";
        String expectedHtml = "<div class=\"chapter\"><h1>Rozdział pierwszy</h1><p>Była ciemna i burzliwa noc...</p></div>";
        String expectedText = "Rozdział pierwszy Była ciemna i burzliwa noc...";

        // when
        ChapterDto chapter = ChapterDto.builder()
                .index(expectedIndex)
                .title(expectedTitle)
                .html(expectedHtml)
                .text(expectedText)
                .build();

        // then
        assertNotNull(chapter);
        assertEquals(expectedIndex, chapter.getIndex());
        assertEquals(expectedTitle, chapter.getTitle());
        assertEquals(expectedHtml, chapter.getHtml());
        assertEquals(expectedText, chapter.getText());
    }

    @Test
    @DisplayName("givenNoFields_whenBuildingChapterDto_thenShouldCreateInstanceWithDefaultValues")
    void givenNoFields_whenBuildingChapterDto_thenShouldCreateInstanceWithDefaultValues() {
        // when
        ChapterDto chapter = ChapterDto.builder().build();

        // then
        assertNotNull(chapter);
        assertEquals(0, chapter.getIndex());
        assertNull(chapter.getTitle());
        assertNull(chapter.getHtml());
        assertNull(chapter.getText());
    }

    @Test
    @DisplayName("givenChapterDtoWithPolishDiacritics_whenGettingFields_thenShouldPreservePolishCharacters")
    void givenChapterDtoWithPolishDiacritics_whenGettingFields_thenShouldPreservePolishCharacters() {
        // given
        String polishTitle = "Żółta żaba skacze przez płot - ćwiczenie ósmego rozdziału";

        ChapterDto chapter = ChapterDto.builder()
                .index(7)
                .title(polishTitle)
                .build();

        // when & then
        assertEquals(polishTitle, chapter.getTitle());
    }

    @Test
    @DisplayName("givenChapterDtoWithHtmlContent_whenGettingHtml_thenShouldPreserveAllHtmlTags")
    void givenChapterDtoWithHtmlContent_whenGettingHtml_thenShouldPreserveAllHtmlTags() {
        // given
        String html = "<h1>Tytuł</h1><p>To jest <strong>ważny</strong> tekst.</p>";
        String text = "Tytuł To jest ważny tekst.";

        ChapterDto chapter = ChapterDto.builder()
                .index(0)
                .title("Rozdział")
                .html(html)
                .text(text)
                .build();

        // when & then
        assertEquals(html, chapter.getHtml());
        assertEquals(text, chapter.getText());
        assertTrue(chapter.getHtml().contains("<strong>"));
        assertFalse(chapter.getText().contains("<"));
    }

    @Test
    @DisplayName("givenChapterDtoWithUnicodeContent_whenAccessingFields_thenShouldPreserveAllCharacters")
    void givenChapterDtoWithUnicodeContent_whenAccessingFields_thenShouldPreserveAllCharacters() {
        // given
        String mixedTitle = "Chapter 1 - Rozdział 1 - Глава 1 - 第一章";
        String mixedText = "English. Polski. Русский. 中文. 📚";

        ChapterDto chapter = ChapterDto.builder()
                .index(0)
                .title(mixedTitle)
                .text(mixedText)
                .build();

        // when & then
        assertEquals(mixedTitle, chapter.getTitle());
        assertEquals(mixedText, chapter.getText());
    }
}

