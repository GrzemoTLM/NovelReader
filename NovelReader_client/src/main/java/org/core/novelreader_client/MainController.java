package org.core.novelreader_client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MainController {
    @FXML
    private VBox booksContainer;
    @FXML
    private Button addBookButton;

    private final BookService bookService = new BookService();

    @FXML
    public void initialize() {
        loadBooks();
    }

    @FXML
    protected void onAddBookButtonClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dodawanie książki");
        alert.setHeaderText(null);
        alert.setContentText("Funkcja dodawania książek w przygotowaniu.");
        alert.showAndWait();
    }

    @FXML
    protected void onLogoutButtonClick() {
        AuthService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 550);
            Stage stage = (Stage) addBookButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Novel Reader - Logowanie");
        } catch (IOException e) {
            showError("Nie można przełączyć na ekran logowania");
        }
    }

    private void loadBooks() {
        addBookButton.setDisable(true);
        CompletableFuture<ObservableList<BookViewModel>> future = bookService.fetchBooks();
        future.thenAccept(books -> Platform.runLater(() -> {
            displayBooks(books);
            addBookButton.setDisable(false);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                addBookButton.setDisable(false);
                showError("Błąd ładowania książek: " + ex.getMessage());
            });
            return null;
        });
    }

    private void displayBooks(ObservableList<BookViewModel> books) {
        booksContainer.getChildren().clear();

        if (books == null || books.isEmpty()) {
            Label emptyLabel = new Label("Brak dodanych książek. Kliknij 'Dodaj Książkę' aby zacząć.");
            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 14px;");
            emptyLabel.setWrapText(true);
            booksContainer.getChildren().add(emptyLabel);
            return;
        }

        for (BookViewModel book : books) {
            HBox bookCard = createBookCard(book);
            booksContainer.getChildren().add(bookCard);
        }
    }

    private HBox createBookCard(BookViewModel book) {
        HBox card = new HBox(15);
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 15;");
        card.setPrefHeight(100);
        card.setMinHeight(100);

        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-text-fill: #333333;");

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333333;");

        Label authorLabel = new Label("Autor: " + book.getAuthor());
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        Label uploadedLabel = new Label("Dodane: " + book.getUploadedAt());
        uploadedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");

        infoBox.getChildren().addAll(titleLabel, authorLabel, uploadedLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button readButton = new Button("Czytaj");
        readButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5;");
        readButton.setOnAction(e -> onReadBook(book));

        card.getChildren().addAll(infoBox, spacer, readButton);
        return card;
    }

    private void onReadBook(BookViewModel book) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Czytanie");
        alert.setHeaderText("Tytuł: " + book.getTitle());
        alert.setContentText("Autor: " + book.getAuthor() + "\n\nFunkcja czytania książek w przygotowaniu.");
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}

