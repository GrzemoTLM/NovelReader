package org.core.novelreader_client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReaderController {
    @FXML private Button backButton;
    @FXML private Label titleLabel;
    @FXML private Label fontSizeLabel;
    @FXML private Button themeButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    @FXML private WebView contentWebView;
    @FXML private Button prevChapterButton;
    @FXML private Button nextChapterButton;
    @FXML private Label chapterLabel;
    @FXML private Button bookmarksToggleButton;
    @FXML private VBox bookmarksPanel;
    @FXML private ListView<BookService.BookmarkDto> bookmarksList;

    private BookService bookService;
    private BookViewModel currentBook;
    private List<BookService.ChapterDto> chapters;
    private int currentChapterIndex = 0;
    private int fontSize = 16;
    private boolean darkMode = false;
    private boolean bookmarksPanelVisible = false;
    private ObservableList<BookService.BookmarkDto> bookmarks = FXCollections.observableArrayList();

    private Timer progressTimer;
    private static final long PROGRESS_SAVE_DELAY_MS = 10000;

    @FXML
    public void initialize() {
        bookService = new BookService();
        errorLabel.setVisible(false);

        if (bookmarksPanel != null) {
            bookmarksPanel.setVisible(false);
            bookmarksPanel.setManaged(false);
        }

        if (bookmarksList != null) {
            bookmarksList.setItems(bookmarks);
            bookmarksList.setCellFactory(lv -> new BookmarkCell());
        }
    }

    public void loadBook(BookViewModel book) {
        this.currentBook = book;
        titleLabel.setText(book.getTitle());

        setLoading(true);
        clearError();

        bookService.getProgress(book.getId())
                .thenAccept(progress -> {
                    currentChapterIndex = progress.chapterIndex();
                    loadParsedBook();
                })
                .exceptionally(ex -> {
                    loadParsedBook();
                    return null;
                });
    }

    private void loadParsedBook() {
        bookService.getParsedBook(currentBook.getId())
                .thenAccept(epub -> Platform.runLater(() -> {
                    setLoading(false);
                    this.chapters = epub.chapters();
                    if (chapters == null || chapters.isEmpty()) {
                        showError("Książka nie zawiera rozdziałów");
                        return;
                    }
                    if (currentChapterIndex >= chapters.size()) {
                        currentChapterIndex = 0;
                    }
                    displayCurrentChapter();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Nie udało się załadować książki: " + getErrorMessage(ex));
                    });
                    return null;
                });
    }

    private void displayCurrentChapter() {
        displayCurrentChapter(0.0);
    }

    private void displayCurrentChapter(double scrollPercent) {
        if (chapters == null || chapters.isEmpty()) return;

        BookService.ChapterDto chapter = chapters.get(currentChapterIndex);
        String html = wrapInHtml(chapter.html() != null ? chapter.html() : chapter.text());
        contentWebView.getEngine().loadContent(html);

        if (scrollPercent > 0) {
            contentWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {}
                        scrollToPercent(scrollPercent);
                    });
                }
            });
        }

        updateChapterNavigation();
        scheduleProgressSave();
    }

    private void scheduleProgressSave() {
        if (progressTimer != null) {
            progressTimer.cancel();
        }
        progressTimer = new Timer();
        progressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveProgress();
            }
        }, PROGRESS_SAVE_DELAY_MS);
    }

    private void saveProgress() {
        if (currentBook != null) {
            bookService.saveProgress(currentBook.getId(), currentChapterIndex, 0)
                    .thenAccept(v -> System.out.println("Postęp zapisany: rozdział " + (currentChapterIndex + 1)))
                    .exceptionally(ex -> {
                        System.err.println("Błąd zapisu postępu: " + ex.getMessage());
                        return null;
                    });
        }
    }

    private String wrapInHtml(String content) {
        String bgColor = darkMode ? "#1a1a2e" : "#ffffff";
        String textColor = darkMode ? "#e0e0e0" : "#333333";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Georgia, serif; font-size: " + fontSize + "px; " +
                "line-height: 1.8; padding: 30px 50px; margin: 0; " +
                "background-color: " + bgColor + "; color: " + textColor + "; " +
                "max-width: 800px; margin: 0 auto; }" +
                "p { margin: 1em 0; text-align: justify; }" +
                "h1,h2,h3,h4,h5,h6 { margin-top: 1.5em; }" +
                "img { max-width: 100%; height: auto; }" +
                "</style></head><body>" + content + "</body></html>";
    }

    private void updateChapterNavigation() {
        int total = chapters != null ? chapters.size() : 1;
        String chapterTitle = "";
        if (chapters != null && currentChapterIndex < chapters.size()) {
            chapterTitle = chapters.get(currentChapterIndex).title();
        }
        if (chapterTitle != null && !chapterTitle.isBlank()) {
            chapterLabel.setText(chapterTitle + " (" + (currentChapterIndex + 1) + "/" + total + ")");
        } else {
            chapterLabel.setText("Rozdział " + (currentChapterIndex + 1) + " / " + total);
        }
        prevChapterButton.setDisable(currentChapterIndex <= 0);
        nextChapterButton.setDisable(chapters == null || currentChapterIndex >= chapters.size() - 1);
    }

    @FXML
    protected void onBackClick() {
        saveProgress();
        if (progressTimer != null) {
            progressTimer.cancel();
        }
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Novel Reader - Moje Książki");
        } catch (IOException e) {
            showError("Błąd powrotu: " + e.getMessage());
        }
    }

    @FXML
    protected void onPrevChapter() {
        if (currentChapterIndex > 0) {
            currentChapterIndex--;
            displayCurrentChapter();
        }
    }

    @FXML
    protected void onNextChapter() {
        if (chapters != null && currentChapterIndex < chapters.size() - 1) {
            currentChapterIndex++;
            displayCurrentChapter();
        }
    }

    @FXML
    protected void onIncreaseFontSize() {
        if (fontSize < 32) {
            fontSize += 2;
            fontSizeLabel.setText(fontSize + "px");
            displayCurrentChapter();
        }
    }

    @FXML
    protected void onDecreaseFontSize() {
        if (fontSize > 10) {
            fontSize -= 2;
            fontSizeLabel.setText(fontSize + "px");
            displayCurrentChapter();
        }
    }

    @FXML
    protected void onToggleTheme() {
        darkMode = !darkMode;
        themeButton.setText(darkMode ? "☀" : "🌙");
        displayCurrentChapter();
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        backButton.setDisable(loading);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private String getErrorMessage(Throwable ex) {
        if (ex == null) return "Nieznany błąd";
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String msg = cause.getMessage();
        return (msg == null || msg.isBlank()) ? cause.getClass().getSimpleName() : msg;
    }

    @FXML
    protected void onToggleBookmarks() {
        bookmarksPanelVisible = !bookmarksPanelVisible;
        if (bookmarksPanel != null) {
            bookmarksPanel.setVisible(bookmarksPanelVisible);
            bookmarksPanel.setManaged(bookmarksPanelVisible);
        }
        if (bookmarksToggleButton != null) {
            bookmarksToggleButton.setText(bookmarksPanelVisible ? "📕" : "📖");
        }
        if (bookmarksPanelVisible) {
            loadBookmarks();
        }
    }

    @FXML
    protected void onAddBookmark() {
        if (currentBook == null || chapters == null) return;

        double scrollPercent = getScrollPercent();
        String textSnippet = getTextSnippet();

        String defaultLabel = "Rozdział " + (currentChapterIndex + 1);
        if (currentChapterIndex < chapters.size()) {
            String title = chapters.get(currentChapterIndex).title();
            if (title != null && !title.isBlank()) {
                defaultLabel = title;
            }
        }
        if (scrollPercent > 0) {
            defaultLabel += " (" + String.format("%.0f", scrollPercent) + "%)";
        }

        TextInputDialog dialog = new TextInputDialog(defaultLabel);
        dialog.setTitle("Dodaj zakładkę");
        dialog.setHeaderText("Dodaj zakładkę do rozdziału " + (currentChapterIndex + 1) +
                            (scrollPercent > 0 ? " (pozycja: " + String.format("%.0f", scrollPercent) + "%)" : ""));
        dialog.setContentText("Nazwa zakładki:");

        dialog.showAndWait().ifPresent(label -> {
            if (!label.isBlank()) {
                bookService.addBookmark(currentBook.getId(), currentChapterIndex, label, scrollPercent, textSnippet)
                        .thenAccept(bookmark -> Platform.runLater(() -> {
                            bookmarks.add(0, bookmark);
                            showInfo("Zakładka dodana" + (scrollPercent > 0 ? " (pozycja: " + String.format("%.0f", scrollPercent) + "%)" : ""));
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> showError("Błąd dodawania zakładki: " + getErrorMessage(ex)));
                            return null;
                        });
            }
        });
    }

    private void loadBookmarks() {
        if (currentBook == null) return;

        bookService.getBookmarks(currentBook.getId())
                .thenAccept(list -> Platform.runLater(() -> {
                    bookmarks.clear();
                    bookmarks.addAll(list);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> System.err.println("Błąd ładowania zakładek: " + ex.getMessage()));
                    return null;
                });
    }

    private void goToBookmark(BookService.BookmarkDto bookmark) {
        if (bookmark != null && chapters != null && bookmark.chapterIndex() < chapters.size()) {
            currentChapterIndex = bookmark.chapterIndex();
            double scrollPercent = bookmark.progressPercent() != null ? bookmark.progressPercent() : 0.0;
            displayCurrentChapter(scrollPercent);
        }
    }

    private void deleteBookmark(BookService.BookmarkDto bookmark) {
        if (bookmark == null || currentBook == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Usuń zakładkę");
        confirm.setHeaderText("Czy na pewno chcesz usunąć zakładkę?");
        String displayLabel = bookmark.label() != null ? bookmark.label() :
                             (bookmark.chapterTitle() != null ? bookmark.chapterTitle() : "Rozdział " + (bookmark.chapterIndex() + 1));
        confirm.setContentText(displayLabel);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                bookService.deleteBookmark(currentBook.getId(), bookmark.id())
                        .thenAccept(v -> Platform.runLater(() -> {
                            bookmarks.remove(bookmark);
                            showInfo("Zakładka usunięta");
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> showError("Błąd usuwania zakładki: " + getErrorMessage(ex)));
                            return null;
                        });
            }
        });
    }

    private void showInfo(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #27ae60;");
        errorLabel.setVisible(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> errorLabel.setVisible(false));
            }
        }, 2000);
    }

    private double getScrollPercent() {
        try {
            Object result = contentWebView.getEngine().executeScript(
                "var scrollTop = document.documentElement.scrollTop || document.body.scrollTop;" +
                "var scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight;" +
                "var clientHeight = document.documentElement.clientHeight || window.innerHeight;" +
                "var maxScroll = scrollHeight - clientHeight;" +
                "maxScroll > 0 ? (scrollTop / maxScroll) * 100 : 0;"
            );
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
        } catch (Exception e) {
            System.err.println("Błąd pobierania pozycji scrolla: " + e.getMessage());
        }
        return 0.0;
    }

    private void scrollToPercent(double percent) {
        try {
            contentWebView.getEngine().executeScript(
                "var scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight;" +
                "var clientHeight = document.documentElement.clientHeight || window.innerHeight;" +
                "var maxScroll = scrollHeight - clientHeight;" +
                "var targetScroll = (maxScroll * " + percent + ") / 100;" +
                "window.scrollTo(0, targetScroll);"
            );
        } catch (Exception e) {
            System.err.println("Błąd przewijania: " + e.getMessage());
        }
    }

    private String getTextSnippet() {
        try {
            Object result = contentWebView.getEngine().executeScript(
                "var selection = window.getSelection();" +
                "if (selection && selection.toString().trim().length > 0) {" +
                "    selection.toString().substring(0, 100);" +
                "} else {" +
                "    var scrollTop = document.documentElement.scrollTop || document.body.scrollTop;" +
                "    var elements = document.body.getElementsByTagName('*');" +
                "    var nearestText = '';" +
                "    for (var i = 0; i < elements.length; i++) {" +
                "        var el = elements[i];" +
                "        if (el.offsetTop >= scrollTop && el.innerText) {" +
                "            nearestText = el.innerText.substring(0, 100);" +
                "            break;" +
                "        }" +
                "    }" +
                "    nearestText || '';" +
                "}"
            );
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private class BookmarkCell extends ListCell<BookService.BookmarkDto> {
        @Override
        protected void updateItem(BookService.BookmarkDto item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                HBox hbox = new HBox(8);
                hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                String labelText = item.label();
                if (labelText == null || labelText.isBlank()) {
                    labelText = item.chapterTitle() != null ? item.chapterTitle() : "Rozdział " + (item.chapterIndex() + 1);
                }
                Label label = new Label(labelText);
                label.setStyle("-fx-font-size: 12px;");
                HBox.setHgrow(label, Priority.ALWAYS);
                label.setMaxWidth(Double.MAX_VALUE);

                String positionInfo = "R." + (item.chapterIndex() + 1);
                if (item.progressPercent() != null && item.progressPercent() > 0) {
                    positionInfo += " (" + String.format("%.0f", item.progressPercent()) + "%)";
                }
                Label chapterInfo = new Label(positionInfo);
                chapterInfo.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");

                Button goButton = new Button("→");
                goButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6;");
                goButton.setOnAction(e -> goToBookmark(item));

                Button deleteButton = new Button("×");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6;");
                deleteButton.setOnAction(e -> deleteBookmark(item));

                hbox.getChildren().addAll(label, chapterInfo, goButton, deleteButton);
                setGraphic(hbox);
            }
        }
    }
}

