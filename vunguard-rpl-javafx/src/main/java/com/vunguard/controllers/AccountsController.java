package com.vunguard.controllers;

import com.vunguard.dao.UserDAO;
import com.vunguard.models.User;
import com.vunguard.controllers.RegistrationController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.util.Callback;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.util.Arrays;
import java.io.IOException;
import java.util.Optional;

public class AccountsController {

    @FXML
    private VBox contentArea;

    @FXML
    private TableView<User> accountsTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> createdColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;    
    
    @FXML
    private SidebarController sidebarViewController;

    // Sample data for users
    private ObservableList<User> userList = FXCollections.observableArrayList();

    // Edit dialog fields
    private TextField editUsernameField;
    private TextField editFullNameField;
    private TextField editEmailField;
    private ComboBox<String> editRoleComboBox;
    private UserDAO userDAO;
    private Stage editDialogStage;
    private User currentEditingUser;

    @FXML
    private void initialize() {
        System.out.println("AccountsController initialized");
        this.userDAO = new UserDAO();

        if (sidebarViewController != null) {
            System.out.println("SidebarView Controller injected into AccountsController");
            // Set the accounts button as active in the sidebar
            try {
                sidebarViewController.setActiveButton(sidebarViewController.getAccountsButton());
            } catch (Exception e) {
                System.err.println("Error setting active button: " + e.getMessage());
            }
        } else {
            System.out.println("SidebarView Controller NOT injected.");
        }

        // Configure table columns
        setupTableColumns();

        // Set the data to the table
        accountsTable.setItems(userList);

        loadAccountsFromDB();
    }

    private void loadAccountsFromDB() {
        ObservableList<User> userList = userDAO.getAccounts();
        accountsTable.setItems(userList);
        System.out.println("Loaded " + userList.size() + " accounts from the database.");
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdDateFormatted"));
        
        // Role column with custom styling (badges)
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                
                if (empty || role == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                
                Label roleLabel = new Label(role);
                roleLabel.setPadding(new Insets(2, 8, 2, 8));
                roleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4;");
                
                // Set different colors based on role
                switch (role) {
                    case "Admin":
                        roleLabel.setStyle(roleLabel.getStyle() + "-fx-background-color: #10B981; -fx-text-fill: #111827;");
                        break;
                    case "Investor":
                        roleLabel.setStyle(roleLabel.getStyle() + "-fx-background-color: #3B82F6; -fx-text-fill: #111827;");
                        break;
                    case "Manager":
                        roleLabel.setStyle(roleLabel.getStyle() + "-fx-background-color: #6366F1; -fx-text-fill: #111827;");
                        break;
                    case "Analyst":
                        roleLabel.setStyle(roleLabel.getStyle() + "-fx-background-color: #8B5CF6; -fx-text-fill: #111827;");
                        break;
                    default:
                        roleLabel.setStyle(roleLabel.getStyle() + "-fx-background-color: #6B7280; -fx-text-fill: #111827;");
                }
                
                setGraphic(roleLabel);
                setText(null);
            }
        });
        
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdDateFormatted"));
        
        // Setup the actions column with edit and delete buttons
        setupActionsColumn();
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-font-size: 11px;");
                deleteButton.setStyle("-fx-background-color: #F87171; -fx-text-fill: white; -fx-font-size: 11px;");

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    showEditUserDialog(user);
                });
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
    }
    private void showEditUserDialog(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit User Account");
        dialog.setHeaderText("Editing details for: " + user.getUsername());

        // Siapkan tombol Save dan Cancel
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Buat layout untuk form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField(user.getUsername());
        TextField fullNameField = new TextField(user.getFullName());
        TextField emailField = new TextField(user.getEmail());
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("admin", "manager", "analyst", "investor");
        roleComboBox.setValue(user.getRole());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Leave empty to keep current password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleComboBox, 1, 3);
        grid.add(new Label("New Password:"), 0, 4);
        grid.add(passwordField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Validasi agar tombol Save hanya aktif jika input valid
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false); // Awalnya aktif

        // Tampilkan dialog dan proses hasilnya
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            // Update objek User dengan data baru dari form
            user.setUsername(usernameField.getText());
            user.setFullName(fullNameField.getText());
            user.setEmail(emailField.getText());
            user.setRole(roleComboBox.getValue());
            
            // Panggil DAO untuk update ke database
            boolean success = userDAO.updateAccount(user, passwordField.getText());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account updated successfully.");
                loadAccountsFromDB(); // Muat ulang data tabel
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update account.");
            }
        }
    }
    
    private void handleCancelEditAction(ActionEvent event) {
        if (editDialogStage != null) {
            editDialogStage.close();
        }
    }
    
    private void handleSaveEditAction(ActionEvent event) {
        if (currentEditingUser == null) {
            return;
        }
        
        // Get form values
        String username = editUsernameField.getText().trim();
        String fullName = editFullNameField.getText().trim();
        String email = editEmailField.getText().trim();
        String role = editRoleComboBox.getValue();
        
        // Validate input
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || role == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Missing Required Fields");
            alert.setContentText("Please fill in all required fields.");
            alert.showAndWait();
            return;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Email");
            alert.setContentText("Please enter a valid email address.");
            alert.showAndWait();
            return;
        }
        
        // Check if username is taken by another user
        boolean usernameExists = userList.stream()
            .anyMatch(u -> !u.equals(currentEditingUser) && u.getUsername().equalsIgnoreCase(username));
        
        if (usernameExists) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Username Already Exists");
            alert.setContentText("This username is already taken by another user.");
            alert.showAndWait();
            return;
        }
        
        // Check if email is taken by another user
        boolean emailExists = userList.stream()
            .anyMatch(u -> !u.equals(currentEditingUser) && u.getEmail().equalsIgnoreCase(email));
        
        if (emailExists) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Email Already Exists");
            alert.setContentText("This email is already used by another user.");
            alert.showAndWait();
            return;
        }
        
        // Update user data
        currentEditingUser.setUsername(username);
        currentEditingUser.setFullName(fullName);
        currentEditingUser.setEmail(email);
        currentEditingUser.setRole(role);
        
        // Refresh the table to show updated data
        accountsTable.refresh();
        
        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("User Updated");
        alert.setContentText("User information has been successfully updated.");
        alert.showAndWait();
        
        // Close the dialog
        if (editDialogStage != null) {
            editDialogStage.close();
        }
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }
    
    private void showDeleteConfirmation(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Are you sure you want to delete " + user.getUsername() + "?");
        alert.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userDAO.deleteAccount(user.getId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User has been deleted.");
                loadAccountsFromDB(); // Refresh tabel
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete user.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Account Management");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 