package com.vunguard.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.vunguard.Main;
import com.vunguard.models.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Labeled;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
    }    @FXML
    private void handleSignInAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        System.out.println("Sign In Attempt:");
        System.out.println("Username: " + username);

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter both username and password!", AlertType.WARNING);
            return;
        }

        // Get registered users
        List<User> registeredUsers = RegistrationController.getRegisteredUsers();
        
        // Check if any user matches the credentials
        // Note: In a real application, passwords should be hashed and compared securely
        boolean loginSuccessful = false;
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username)) {
                // For demo purposes, we'll accept any password for registered users
                // In a real app, you'd validate the actual password
                loginSuccessful = true;
                break;
            }
        }
        
        // Also check for admin account
        if (username.equals("admin") && password.equals("password")) {
            loginSuccessful = true;
        }

        if (loginSuccessful) {
            System.out.println("Login successful");
            try {
                Main.loadDashboardScene();
            } catch (IOException e) {
                System.err.println("Error loading Dashboard scene: " + e.getMessage());
                showAlert("Error loading dashboard. Please try again.", AlertType.ERROR);
            }
        } else {
            System.out.println("Login failed");
            showAlert(
                "Login failed!\n\n" +
                "Invalid username or password. Please check your credentials and try again.\n" +
                "If you don't have an account, please register first.",
                AlertType.ERROR
            );
        }
    }@FXML
    private void handleForgotPasswordAction(ActionEvent event) {
        System.out.println("Forgot Password link clicked");
        showForgotPasswordDialog();
    }    private void showForgotPasswordDialog() {
        // Create a text input dialog for email
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Password Recovery");
        dialog.setContentText("Enter your email address:");
        
        // Get the dialog pane for styling
        DialogPane dialogPane = dialog.getDialogPane();
        
        // Apply comprehensive styling to the dialog
        dialogPane.setStyle(
            "-fx-background-color: #1E1E2C; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Inter', sans-serif; " +
            "-fx-font-size: 14px;"
        );
        
        // Use Platform.runLater to ensure all nodes are loaded before styling
        Platform.runLater(() -> {
            // Style the header panel
            Node headerPanel = dialogPane.lookup(".header-panel");
            if (headerPanel != null) {
                headerPanel.setStyle(
                    "-fx-background-color: #1E1E2C; " +
                    "-fx-text-fill: white;"
                );
            }
            
            // Style the content panel
            Node contentPanel = dialogPane.lookup(".content");
            if (contentPanel != null) {
                contentPanel.setStyle(
                    "-fx-background-color: #1E1E2C; " +
                    "-fx-text-fill: white;"
                );
            }
            
            // Style all labels comprehensively
            dialogPane.lookupAll(".label").forEach(node -> {
                if (node instanceof Labeled) {
                    node.setStyle(
                        "-fx-text-fill: white !important; " +
                        "-fx-font-family: 'Inter', sans-serif; " +
                        "-fx-font-size: 14px;"
                    );
                }
            });
            
            // Style all text nodes
            dialogPane.lookupAll(".text").forEach(node -> {
                node.setStyle(
                    "-fx-fill: white !important; " +
                    "-fx-font-family: 'Inter', sans-serif; " +
                    "-fx-font-size: 14px;"
                );
            });
            
            // Style the input field
            dialog.getEditor().setStyle(
                "-fx-background-color: #2A2A3A; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #3A3A4A; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px; " +
                "-fx-font-family: 'Inter', sans-serif; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 8px;"
            );
            
            // Style all buttons
            dialogPane.lookupAll(".button").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #6EE7B7; " +
                    "-fx-text-fill: #12121A !important; " +
                    "-fx-font-family: 'Inter', sans-serif; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 5px; " +
                    "-fx-border-radius: 5px;"
                );
            });
        });
        
        // Show the dialog and capture the result
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String email = result.get().trim();
            
            if (email.isEmpty()) {
                showAlert("Email is required!", AlertType.WARNING);
                return;
            }
            
            if (!isValidEmail(email)) {
                showAlert("Please enter a valid email address!", AlertType.WARNING);
                return;
            }
            
            // Look up user by email
            User foundUser = findUserByEmail(email);
            
            if (foundUser != null) {
                // In a real application, this would send an email with a password reset link
                // For demo purposes, we'll show the username
                showAlert(
                    "Password recovery information sent!\n\n" +
                    "Your username is: " + foundUser.getUsername() + "\n" +
                    "In a real application, you would receive an email with " +
                    "instructions to reset your password.",
                    AlertType.INFORMATION
                );
            } else {
                showAlert(
                    "Email address not found!\n\n" +
                    "No account is associated with this email address. " +
                    "Please check your email or register for a new account.",
                    AlertType.ERROR
                );
            }
        }
    }
    
    private User findUserByEmail(String email) {
        List<User> registeredUsers = RegistrationController.getRegisteredUsers();
        return registeredUsers.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }
      private void showAlert(String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Forgot Password");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert to match the application theme
        alert.getDialogPane().setStyle(
            "-fx-background-color: #1E1E2C; " +
            "-fx-text-fill: white;"
        );
        
        // Additional styling for all text elements in the alert
        alert.getDialogPane().lookup(".content").setStyle("-fx-text-fill: white;");
        alert.getDialogPane().lookup(".content .label").setStyle("-fx-text-fill: white;");
        
        alert.showAndWait();
    }

    @FXML
    private void handleRegisterAction(ActionEvent event) {
        System.out.println("Register link clicked");
        try {
            Main.loadRegistrationScene();
    } catch (IOException e) {
        System.err.println("Error loading Registration scene: " + e.getMessage());
    }
}
} 