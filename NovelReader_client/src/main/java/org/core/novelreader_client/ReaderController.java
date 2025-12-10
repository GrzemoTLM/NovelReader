package org.core.novelreader_client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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

    private BookService bookService;
    private BookViewModel currentBook;
    private List<BookService.ChapterDto> chapters;
    private int currentChapterIndex = 0;
    private int fontSize = 16;
    private boolean darkMode = false;

    private Timer progressTimer;
    private static final long PROGRESS_SAVE_DELAY_MS = 10000;

    @FXML
    public void initialize() {
        bookService = new BookService();
        errorLabel.setVisible(false);
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
        if (chapters == null || chapters.isEmpty()) return;

        BookService.ChapterDto chapter = chapters.get(currentChapterIndex);
        String html = wrapInHtml(chapter.html() != null ? chapter.html() : chapter.text());
        contentWebView.getEngine().loadContent(html);
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
}

