package org.core.novelreader_client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("BookService DTOs - testy jednostkowe")
class BookServiceDtoTest {
    @Test
    @DisplayName("BookDto powinien poprawnie przechowywać dane")
    void bookDtoShouldStoreDataCorrectly() {
        BookService.BookDto dto = new BookService.BookDto(
                1L, "Tytuł", "Autor", "Opis", "2024-01-01"
        );
        assertEquals(1L, dto.id());
        assertEquals("Tytuł", dto.title());
        assertEquals("Autor", dto.author());
    }
    @Test
    @DisplayName("ChapterDto powinien poprawnie przechowywać dane rozdziału")
    void chapterDtoShouldStoreDataCorrectly() {
        BookService.ChapterDto chapter = new BookService.ChapterDto(
                0, "Rozdział 1", "<p>Treść HTML</p>", "Treść tekstowa"
        );
        assertEquals(0, chapter.index());
        assertEquals("Rozdział 1", chapter.title());
        assertEquals("<p>Treść HTML</p>", chapter.html());
    }
    @Test
    @DisplayName("ProgressDto powinien przechowywać postęp czytania")
    void progressDtoShouldStoreReadingProgress() {
        BookService.ProgressDto progress = new BookService.ProgressDto(1L, 5, 120);
        assertEquals(1L, progress.bookId());
        assertEquals(5, progress.chapterIndex());
        assertEquals(120, progress.offsetInChapter());
    }
    @Test
    @DisplayName("EpubDto z pustą listą rozdziałów")
    void epubDtoWithEmptyChapters() {
        BookService.MetadataDto metadata = new BookService.MetadataDto(
                "Tytuł", "Autor", "pl", null, null
        );
        BookService.EpubDto epub = new BookService.EpubDto(metadata, List.of());
        assertNotNull(epub.chapters());
        assertTrue(epub.chapters().isEmpty());
    }
}
