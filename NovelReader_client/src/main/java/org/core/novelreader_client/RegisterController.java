package org.core.novelreader_client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private ProgressIndicator progressIndicator;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        progressIndicator.setVisible(false);
    }

    @FXML
    protected void onRegisterButtonClick() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Proszę wypełnić wszystkie pola");
            return;
        }

        if (username.length() < 3) {
            showError("Nazwa użytkownika musi mieć co najmniej 3 znaki");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Proszę podać prawidłowy adres email");
            return;
        }

        if (password.length() < 6) {
            showError("Hasło musi mieć co najmniej 6 znaków");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Hasła nie są identyczne");
            return;
        }

        setLoading(true);
        hideMessages();

        authService.register(username, email, password)
                .thenAccept(result -> Platform.runLater(() -> {
                    setLoading(false);
                    if (result.isSuccess()) {
                        showSuccess("Rejestracja zakończona pomyślnie! Możesz się teraz zalogować.");
                        clearForm();
                    } else {
                        showError(result.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Błąd połączenia z serwerem: " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    protected void onLoginLinkClick() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Novel Reader - Logowanie");
        } catch (IOException e) {
            showError("Nie można załadować widoku logowania");
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showError(String message) {
        successLabel.setVisible(false);
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        errorLabel.setVisible(false);
        successLabel.setText(message);
        successLabel.setVisible(true);
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    private void clearForm() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        registerButton.setDisable(loading);
        usernameField.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
    }
}

