package com.vunguard.controllers;

import java.io.IOException;
import com.vunguard.Main;
import com.vunguard.models.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class RegistrationController {
    
    // Static list to store users (shared across all instances)
    private static List<User> registeredUsers = new ArrayList<>();

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    // Role selection buttons
    @FXML
    private Button investorButton;

    @FXML
    private Button managerButton;

    @FXML
    private Button analystButton;

    // Current selected role
    private String selectedRole = "Investor"; // Default role

    @FXML
    private void initialize() {
        System.out.println("RegistrationController initialized");
        
        // Set initial selected role (Investor is default)
        setSelectedRole("Investor");
    }

    @FXML
    private void handleInvestorRole(ActionEvent event) {
        setSelectedRole("Investor");
    }

    @FXML
    private void handleManagerRole(ActionEvent event) {
        setSelectedRole("Manager");
    }

    @FXML
    private void handleAnalystRole(ActionEvent event) {
        setSelectedRole("Analyst");
    }

    private void setSelectedRole(String role) {
        selectedRole = role;
        
        // Reset all buttons to inactive state
        String inactiveStyle = "-fx-background-color: #3A3A4A; -fx-text-fill: #B0B0B0; -fx-font-family: 'Inter 18pt'; -fx-font-size: 12px; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 6 12;";
        
        investorButton.setStyle(inactiveStyle);
        managerButton.setStyle(inactiveStyle);
        analystButton.setStyle(inactiveStyle);
        
        // Set active style for selected button
        
        
        switch (role) {
            case "Investor":
                String activeStyle1 = "-fx-background-color: #3B82F6; -fx-text-fill: #12121A; -fx-font-family: 'Inter 18pt'; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 6 12;";
                investorButton.setStyle(activeStyle1);
                break;
            case "Manager":
                String activeStyle2 = "-fx-background-color: #6366F1; -fx-text-fill: #12121A; -fx-font-family: 'Inter 18pt'; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 6 12;";
                managerButton.setStyle(activeStyle2);
                break;
            case "Analyst":
                String activeStyle3 = "-fx-background-color: #8B5CF6; -fx-text-fill: #12121A; -fx-font-family: 'Inter 18pt'; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 6 12;";
                analystButton.setStyle(activeStyle3);
                break;
        }
        
        System.out.println("Selected role: " + selectedRole);
    }

    @FXML
    private void handleRegisterAction(ActionEvent event) {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("All fields are required!", AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Passwords do not match!", AlertType.ERROR);
            return;
        }

        // Check if username already exists
        if (isUsernameExists(username)) {
            showAlert("Username already exists! Please choose a different username.", AlertType.ERROR);
            return;
        }

        // Check if email already exists
        if (isEmailExists(email)) {
            showAlert("Email already registered! Please use a different email.", AlertType.ERROR);
            return;
        }

        // Save the user data with selected role
        User newUser = new User(username, fullName, email, selectedRole, LocalDate.now());
        registeredUsers.add(newUser);
        
        System.out.println("User registered: " + newUser.getUsername() + " as " + selectedRole);
        System.out.println("Total registered users: " + registeredUsers.size());

        showAlert("Registration successful for " + username + " as " + selectedRole + "!\nYou can now login with your credentials.", AlertType.INFORMATION);
        
        // Clear form fields
        clearFields();
        
        // Navigate back to login
        try {
            Main.loadLoginScene();
        } catch (IOException e) {
            System.err.println("Error loading Login scene: " + e.getMessage());
        }
    }

    private boolean isUsernameExists(String username) {
        return registeredUsers.stream()
                .anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
    }

    private boolean isEmailExists(String email) {
        return registeredUsers.stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    private void clearFields() {
        fullNameField.clear();
        emailField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        
        // Reset role selection to default
        setSelectedRole("Investor");
    }

    // Static method to get registered users (can be used by LoginController)
    public static List<User> getRegisteredUsers() {
        return new ArrayList<>(registeredUsers);
    }

    @FXML
    private void handleLoginAction(ActionEvent event) {
        try {
            Main.loadLoginScene();
        } catch (IOException e) {
            System.err.println("Error loading Login scene: " + e.getMessage());
        }
    }

    private void showAlert(String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Registration");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}