package org.core.novelreader_client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.nio.file.Path;
import java.io.IOException;

public class HelloController {
    @FXML
    private VBox booksContainer;
    @FXML
    private Button addBookButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    private BookService bookService;

    @FXML
    public void initialize() {
        statusLabel.setText("");
        try {
            bookService = new BookService();
            loadBooks();
        } catch (Exception e) {
            showError("Błąd inicjalizacji: " + e.getMessage());
        }
    }

    @FXML
    protected void onAddBookClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik książki");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Wszystkie pliki", "*.*"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("EPUB", "*.epub"),
                new FileChooser.ExtensionFilter("TXT", "*.txt")
        );

        java.io.File selectedFile = fileChooser.showOpenDialog(addBookButton.getScene().getWindow());
        if (selectedFile != null) {
            showBookMetadataDialog(selectedFile.toPath());
        }
    }

    @FXML
    protected void onRefreshClick() {
        loadBooks();
    }

    @FXML
    protected void onLogoutClick() throws IOException {
        AuthService.logout();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load(), 400, 550);
        Stage stage = (Stage) addBookButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Novel Reader - Logowanie");
    }

    private void loadBooks() {
        if (bookService == null) {
            showError("Serwis książek niedostępny");
            return;
        }

        setLoading(true);
        clearError();

        bookService.fetchBooks()
                .thenAccept(books -> Platform.runLater(() -> {
                    setLoading(false);
                    displayBooks(books);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Nie udało się pobrać książek: " + getErrorMessage(ex));
                        displayEmptyState();
                    });
                    return null;
                });
    }

    private void displayBooks(ObservableList<BookViewModel> books) {
        booksContainer.getChildren().clear();

        if (books == null || books.isEmpty()) {
            displayEmptyState();
            return;
        }

        for (BookViewModel book : books) {
            try {
                HBox card = createBookCard(book);
                booksContainer.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("Błąd podczas tworzenia karty książki: " + e.getMessage());
            }
        }
    }

    private void displayEmptyState() {
        booksContainer.getChildren().clear();
        Label emptyLabel = new Label("Brak książek. Kliknij 'Dodaj Książkę' aby dodać pierwszą.");
        emptyLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        emptyLabel.setWrapText(true);
        booksContainer.getChildren().add(emptyLabel);
    }

    private HBox createBookCard(BookViewModel book) {
        HBox card = new HBox(15);
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: #fafafa; -fx-padding: 12;");

        VBox info = new VBox(3);
        Label title = new Label(book.getTitle() != null ? book.getTitle() : "Bez tytułu");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label author = new Label("Autor: " + (book.getAuthor() != null ? book.getAuthor() : "Nieznany"));
        author.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        info.getChildren().addAll(title, author);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button readBtn = new Button("Czytaj");
        readBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;");
        readBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(book.getTitle());
            alert.setHeaderText("Autor: " + book.getAuthor());
            alert.setContentText("Funkcja czytania w przygotowaniu.");
            alert.showAndWait();
        });

        Button deleteBtn = new Button("Usuń");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;");
        deleteBtn.setOnAction(e -> deleteBook(book.getId(), () -> {
            statusLabel.setText("Książka usunięta pomyślnie!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
        }));

        card.getChildren().addAll(info, spacer, readBtn, deleteBtn);
        return card;
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        addBookButton.setDisable(loading);
        refreshButton.setDisable(loading);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void clearError() {
        statusLabel.setText("");
    }

    private String getErrorMessage(Throwable ex) {
        if (ex == null) return "Nieznany błąd";
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String msg = cause.getMessage();
        if (msg == null || msg.isBlank()) {
            return cause.getClass().getSimpleName();
        }
        return msg;
    }

    private void showBookMetadataDialog(Path filePath) {
        Dialog<BookMetadata> dialog = new Dialog<>();
        dialog.setTitle("Metadane książki");
        dialog.setHeaderText("Podaj informacje o książce:");

        Label titleLabel = new Label("Tytuł:");
        TextField titleField = new TextField();
        titleField.setPromptText("Wpisz tytuł...");

        Label authorLabel = new Label("Autor:");
        TextField authorField = new TextField();
        authorField.setPromptText("Wpisz autora...");

        Label descriptionLabel = new Label("Opis:");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Wpisz opis...");
        descriptionField.setWrapText(true);
        descriptionField.setPrefHeight(80);

        tryExtractEpubMetadata(filePath, titleField, authorField, descriptionField);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(
                titleLabel, titleField,
                authorLabel, authorField,
                descriptionLabel, descriptionField
        );

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new BookMetadata(
                        titleField.getText().trim(),
                        authorField.getText().trim(),
                        descriptionField.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(metadata -> {
            if (metadata.title().isEmpty()) {
                showError("Tytuł nie może być pusty!");
                return;
            }
            uploadBook(filePath, metadata);
        });
    }

    private void uploadBook(Path filePath, BookMetadata metadata) {
        setLoading(true);
        clearError();

        bookService.uploadBook(filePath, metadata.title(), metadata.author(), metadata.description())
                .thenAccept(book -> Platform.runLater(() -> {
                    setLoading(false);
                    showError("");
                    statusLabel.setText("Książka dodana pomyślnie!");
                    statusLabel.setStyle("-fx-text-fill: #27ae60;");
                    loadBooks();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Błąd podczas dodawania książki: " + getErrorMessage(ex));
                    });
                    return null;
                });
    }

    private void tryExtractEpubMetadata(Path filePath, TextField titleField, TextField authorField, TextArea descriptionField) {
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".epub")) {
            try {
                EpubMetadataExtractor.BookMetadataInfo metadata = EpubMetadataExtractor.extractMetadata(filePath);

                if (!metadata.title.isEmpty()) {
                    titleField.setText(metadata.title);
                }
                if (!metadata.author.isEmpty()) {
                    authorField.setText(metadata.author);
                }
                if (!metadata.description.isEmpty()) {
                    descriptionField.setText(metadata.description);
                }
            } catch (Exception e) {
                System.out.println("Nie udało się wyciągnąć metadanych z EPUB: " + e.getMessage());
            }
        }
    }

    private void deleteBook(Long bookId, Runnable onSuccess) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć tę książkę?");
        confirmDialog.setContentText("Tej operacji nie można cofnąć.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                setLoading(true);
                clearError();

                bookService.deleteBook(bookId)
                        .thenAccept(v -> Platform.runLater(() -> {
                            setLoading(false);
                            onSuccess.run();
                            loadBooks();
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                setLoading(false);
                                showError("Błąd podczas usuwania książki: " + getErrorMessage(ex));
                            });
                            return null;
                        });
            }
        });
    }

    private static class BookMetadata {
        private final String title;
        private final String author;
        private final String description;

        BookMetadata(String title, String author, String description) {
            this.title = title;
            this.author = author;
            this.description = description;
        }

        String title() {
            return title;
        }

        String author() {
            return author;
        }

        String description() {
            return description;
        }
    }
}
