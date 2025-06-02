package com.vunguard.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Hyperlink registerLink;

    @FXML
    private void initialize() {
        System.out.println("LoginController initialized");
        // Anda bisa menambahkan listener atau binding di sini jika perlu
        // Contoh: Mengubah style tombol saat di-hover
        signInButton.setOnMouseEntered(e -> signInButton.setStyle("-fx-background-color: #58D6A0; -fx-text-fill: #101018; -fx-font-family: 'Inter 18pt'; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 5;"));
        signInButton.setOnMouseExited(e -> signInButton.setStyle("-fx-background-color: #6EE7B7; -fx-text-fill: #12121A; -fx-font-family: 'Inter 18pt'; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 5;"));
    }

    @FXML
    private void handleSignInAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("Sign In Attempt:");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // TODO: Implementasikan logika autentikasi di sini
        // Misalnya, validasi input, panggil service autentikasi, dll.

        if (username.equals("admin") && password.equals("password")) {
            System.out.println("Login successful (dummy)");
            // TODO: Navigasi ke halaman berikutnya setelah login berhasil
        } else {
            System.out.println("Login failed (dummy)");
            // TODO: Tampilkan pesan error di UI
        }
    }

    @FXML
    private void handleForgotPasswordAction(ActionEvent event) {
        System.out.println("Forgot Password link clicked");
        // TODO: Implementasikan logika untuk "Forgot Password"
    }

    @FXML
    private void handleRegisterAction(ActionEvent event) {
        System.out.println("Register link clicked");
        // TODO: Implementasikan logika untuk navigasi ke halaman registrasi
    }
} 