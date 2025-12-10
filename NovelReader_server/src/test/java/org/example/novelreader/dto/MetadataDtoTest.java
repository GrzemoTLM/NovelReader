package org.example.novelreader.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MetadataDto - Testy jednostkowe")
class MetadataDtoTest {

    @Test
    @DisplayName("givenAllMetadataFields_whenBuildingMetadataDto_thenShouldCreateInstanceWithAllFieldsPopulatedCorrectly")
    void givenAllMetadataFields_whenBuildingMetadataDto_thenShouldCreateInstanceWithAllFieldsPopulatedCorrectly() {
        // given
        String expectedTitle = "Władca Pierścieni";
        String expectedAuthor = "J.R.R. Tolkien";
        String expectedLanguage = "pl";
        String expectedIdentifier = "urn:isbn:978-83-7469-555-5";
        String expectedDescription = "Epicka opowieść o Śródziemiu";

        // when
        MetadataDto metadata = MetadataDto.builder()
                .title(expectedTitle)
                .author(expectedAuthor)
                .language(expectedLanguage)
                .identifier(expectedIdentifier)
                .description(expectedDescription)
                .build();

        // then
        assertNotNull(metadata);
        assertEquals(expectedTitle, metadata.getTitle());
        assertEquals(expectedAuthor, metadata.getAuthor());
        assertEquals(expectedLanguage, metadata.getLanguage());
        assertEquals(expectedIdentifier, metadata.getIdentifier());
        assertEquals(expectedDescription, metadata.getDescription());
    }

    @Test
    @DisplayName("givenNoFields_whenBuildingMetadataDto_thenShouldCreateInstanceWithAllFieldsAsNull")
    void givenNoFields_whenBuildingMetadataDto_thenShouldCreateInstanceWithAllFieldsAsNull() {
        // when
        MetadataDto metadata = MetadataDto.builder().build();

        // then
        assertNotNull(metadata);
        assertNull(metadata.getTitle());
        assertNull(metadata.getAuthor());
        assertNull(metadata.getLanguage());
        assertNull(metadata.getIdentifier());
        assertNull(metadata.getDescription());
    }

    @Test
    @DisplayName("givenMetadataDtoWithPolishCharacters_whenGettingFields_thenShouldPreservePolishDiacritics")
    void givenMetadataDtoWithPolishCharacters_whenGettingFields_thenShouldPreservePolishDiacritics() {
        // given
        String polishTitle = "Żółta łódź podwodna ćwiczy ósemkę";
        String polishAuthor = "Stanisław Lem";

        MetadataDto metadata = MetadataDto.builder()
                .title(polishTitle)
                .author(polishAuthor)
                .build();

        // when & then
        assertEquals(polishTitle, metadata.getTitle());
        assertEquals(polishAuthor, metadata.getAuthor());
    }

    @Test
    @DisplayName("givenMetadataDtoWithIsbnIdentifier_whenGettingIdentifier_thenShouldReturnFormattedIsbn")
    void givenMetadataDtoWithIsbnIdentifier_whenGettingIdentifier_thenShouldReturnFormattedIsbn() {
        // given
        String isbn = "urn:isbn:978-83-7469-555-5";

        MetadataDto metadata = MetadataDto.builder()
                .identifier(isbn)
                .build();

        // when & then
        assertEquals(isbn, metadata.getIdentifier());
        assertTrue(metadata.getIdentifier().startsWith("urn:isbn:"));
    }

    @Test
    @DisplayName("givenMetadataDtoWithUnicodeContent_whenAccessingFields_thenShouldPreserveAllCharacters")
    void givenMetadataDtoWithUnicodeContent_whenAccessingFields_thenShouldPreserveAllCharacters() {
        // given
        String japaneseTitle = "吾輩は猫である";
        String japaneseAuthor = "夏目漱石";

        MetadataDto metadata = MetadataDto.builder()
                .title(japaneseTitle)
                .author(japaneseAuthor)
                .language("ja")
                .build();

        // when & then
        assertEquals(japaneseTitle, metadata.getTitle());
        assertEquals(japaneseAuthor, metadata.getAuthor());
        assertEquals("ja", metadata.getLanguage());
    }
}

