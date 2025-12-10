package org.core.novelreader_client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("BookViewModel - testy jednostkowe")
class BookViewModelTest {
    @Test
    @DisplayName("Powinien poprawnie utworzyć ViewModel z pełnymi danymi")
    void shouldCreateViewModelWithFullData() {
        BookService.BookDto dto = new BookService.BookDto(
                1L, "Wiedźmin", "Andrzej Sapkowski", "Saga o wiedźminie", "2024-01-01"
        );
        BookViewModel viewModel = new BookViewModel(dto);
        assertEquals(1L, viewModel.getId());
        assertEquals("Wiedźmin", viewModel.getTitle());
        assertEquals("Andrzej Sapkowski", viewModel.getAuthor());
        assertEquals("Saga o wiedźminie", viewModel.getDescription());
    }
    @Test
    @DisplayName("Powinien ustawić domyślny tytuł gdy null")
    void shouldSetDefaultTitleWhenNull() {
        BookService.BookDto dto = new BookService.BookDto(1L, null, "Autor", "Opis", null);
        BookViewModel viewModel = new BookViewModel(dto);
        assertEquals("Bez tytułu", viewModel.getTitle());
    }
    @Test
    @DisplayName("Powinien ustawić domyślnego autora gdy null")
    void shouldSetDefaultAuthorWhenNull() {
        BookService.BookDto dto = new BookService.BookDto(1L, "Tytuł", null, "Opis", null);
        BookViewModel viewModel = new BookViewModel(dto);
        assertEquals("Nieznany autor", viewModel.getAuthor());
    }
    @Test
    @DisplayName("Powinien obsłużyć wszystkie pola null")
    void shouldHandleAllNullFields() {
        BookService.BookDto dto = new BookService.BookDto(null, null, null, null, null);
        BookViewModel viewModel = new BookViewModel(dto);
        assertEquals(0L, viewModel.getId());
        assertEquals("Bez tytułu", viewModel.getTitle());
        assertEquals("Nieznany autor", viewModel.getAuthor());
        assertEquals("Brak opisu", viewModel.getDescription());
    }
}
