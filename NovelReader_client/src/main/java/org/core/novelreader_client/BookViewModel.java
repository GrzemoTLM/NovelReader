package org.core.novelreader_client;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class BookViewModel {
    private final SimpleLongProperty id = new SimpleLongProperty();
    private final SimpleStringProperty title = new SimpleStringProperty();
    private final SimpleStringProperty author = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final SimpleStringProperty uploadedAt = new SimpleStringProperty();

    public BookViewModel(BookService.BookDto dto) {
        if (dto.id() != null) {
            this.id.set(dto.id());
        }
        this.title.set(dto.title() == null ? "Bez tytułu" : dto.title());
        this.author.set(dto.author() == null ? "Nieznany autor" : dto.author());
        this.description.set(dto.description() == null ? "Brak opisu" : dto.description());
        this.uploadedAt.set(dto.uploadedAt() == null ? "" : dto.uploadedAt());
    }

    public long getId() {
        return id.get();
    }

    public SimpleLongProperty idProperty() {
        return id;
    }

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public String getAuthor() {
        return author.get();
    }

    public SimpleStringProperty authorProperty() {
        return author;
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public String getUploadedAt() {
        return uploadedAt.get();
    }

    public SimpleStringProperty uploadedAtProperty() {
        return uploadedAt;
    }
}

