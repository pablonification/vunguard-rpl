package com.vunguard.controllers;

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
    private TableColumn<User, Void> actionsColumn;    @FXML
    private SidebarController sidebarViewController;

    // Sample data for users
    private ObservableList<User> userList = FXCollections.observableArrayList();

    // Edit dialog fields
    private TextField editUsernameField;
    private TextField editFullNameField;
    private TextField editEmailField;
    private ComboBox<String> editRoleComboBox;
    private Stage editDialogStage;
    private User currentEditingUser;

    @FXML
    private void initialize() {
        System.out.println("AccountsController initialized");

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

        // Add sample users
        addSampleUsers();

        // Configure table columns
        setupTableColumns();

        // Set the data to the table
        accountsTable.setItems(userList);
    }

    private void addSampleUsers() {
        userList.addAll(RegistrationController.getRegisteredUsers());
        // Sample data matching the screenshot
        userList.addAll(Arrays.asList(
            new User("arqilasp_investor", "Arqila Surya Putra", "investor@gmail.com", "Investor", LocalDate.of(2025, 5, 12)),
            new User("arqilasp13", "Arqila Surya Putra", "arqilasp12@gmail.com", "Admin", LocalDate.of(2025, 5, 12)),
            new User("arqilasp12", "Arqila Surya Putra", "arqilasp@gmail.com", "Investor", LocalDate.of(2025, 5, 12)),
            new User("budi_analist", "Budi Analist", "budianalist@gmail.com", "Analyst", LocalDate.of(2025, 5, 11)),
            new User("budi_manager", "Budi Manager", "budimanager@gmail.com", "Manager", LocalDate.of(2025, 5, 11)),
            new User("admin0912", "Arqila Surya Putra", "budi12@gmail.com", "Admin", LocalDate.of(2025, 5, 11)),
            new User("arqilasp", "Arqila Surya Putra", "budi@gmail.com", "Admin", LocalDate.of(2025, 5, 11)),
            new User("investor1", "John Investor", "investor1@example.com", "Investor", LocalDate.of(2025, 5, 11)),
            new User("manager1", "Robert Manager", "manager1@example.com", "Manager", LocalDate.of(2025, 5, 11)),
            new User("analyst1", "Sarah Analyst", "analyst1@example.com", "Analyst", LocalDate.of(2025, 5, 11)),
            new User("admin1", "Admin User", "admin1@example.com", "Admin", LocalDate.of(2025, 5, 11)),
            new User("investor2", "Jane Investor", "investor2@example.com", "Investor", LocalDate.of(2025, 5, 11))
        ));
    }

    private void setupTableColumns() {
        // Setup cell value factories
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
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
        actionsColumn.setPrefWidth(150);
        actionsColumn.setMinWidth(150);
        
        actionsColumn.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button();
                    
                    {
                        // Configure edit button
                        editButton.setStyle("-fx-background-color: transparent; -fx-border-color: #6B7280; " +
                                            "-fx-border-radius: 4; -fx-text-fill: #E5E7EB; -fx-font-size: 11px;");
                        editButton.setPrefWidth(60);
                        editButton.setPrefHeight(25);
                        editButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            System.out.println("Edit button clicked for user: " + user.getUsername());
                            // Handle edit action
                            showEditUserDialog(user);
                        });
                        
                        // Configure delete button
                        deleteButton.setStyle("-fx-background-color: transparent; -fx-shape: " + 
                                  "\"M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z\"; " +
                                  "-fx-background-color: #F87171; -fx-min-width: 20; -fx-min-height: 20; " + 
                                  "-fx-max-width: 20; -fx-max-height: 20;");
                        deleteButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            System.out.println("Delete button clicked for user: " + user.getUsername());
                            // Handle delete action
                            showDeleteConfirmation(user);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(8);
                            buttons.setAlignment(Pos.CENTER_LEFT);
                            buttons.getChildren().addAll(editButton, deleteButton);
                            buttons.setPadding(new Insets(2, 0, 2, 0));
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }        private void showEditUserDialog(User user) {
        try {
            currentEditingUser = user;
            
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vunguard/views/EditUserView.fxml"));
            ScrollPane editDialogRoot = loader.load();
            
            // Try different approaches to find the elements
            // First, try direct lookup on the scene root
            editUsernameField = (TextField) editDialogRoot.lookup("#usernameField");
            editFullNameField = (TextField) editDialogRoot.lookup("#fullNameField");
            editEmailField = (TextField) editDialogRoot.lookup("#emailField");
            editRoleComboBox = (ComboBox<String>) editDialogRoot.lookup("#roleComboBox");
            
            // If direct lookup failed, try looking within the content
            if (editUsernameField == null) {
                VBox contentVBox = (VBox) editDialogRoot.getContent();
                if (contentVBox != null) {
                    editUsernameField = (TextField) contentVBox.lookup("#usernameField");
                    editFullNameField = (TextField) contentVBox.lookup("#fullNameField");
                    editEmailField = (TextField) contentVBox.lookup("#emailField");
                    editRoleComboBox = (ComboBox<String>) contentVBox.lookup("#roleComboBox");
                }
            }
            
            // Debug: Check if elements are found
            System.out.println("Username field found: " + (editUsernameField != null));
            System.out.println("Full name field found: " + (editFullNameField != null));
            System.out.println("Email field found: " + (editEmailField != null));
            System.out.println("Role ComboBox found: " + (editRoleComboBox != null));
            
            // Check if all required elements were found
            if (editUsernameField == null || editFullNameField == null || 
                editEmailField == null || editRoleComboBox == null) {
                throw new RuntimeException("Could not find all required form elements in FXML");
            }
            
            // Set up role ComboBox
            editRoleComboBox.setItems(FXCollections.observableArrayList("Admin", "Investor", "Manager", "Analyst"));
            
            // Populate fields with current user data
            editUsernameField.setText(user.getUsername());
            editFullNameField.setText(user.getFullName());
            editEmailField.setText(user.getEmail());
            editRoleComboBox.setValue(user.getRole());
            
            // Set up button actions
            Button cancelButton = (Button) editDialogRoot.lookup("#cancelButton");
            Button saveButton = (Button) editDialogRoot.lookup("#saveButton");
            
            if (cancelButton == null) {
                VBox contentVBox = (VBox) editDialogRoot.getContent();
                if (contentVBox != null) {
                    cancelButton = (Button) contentVBox.lookup("#cancelButton");
                    saveButton = (Button) contentVBox.lookup("#saveButton");
                }
            }
            
            if (cancelButton != null && saveButton != null) {
                cancelButton.setOnAction(this::handleCancelEditAction);
                saveButton.setOnAction(this::handleSaveEditAction);
            }
            
            // Create and show the dialog
            editDialogStage = new Stage();
            editDialogStage.setTitle("Edit User - " + user.getUsername());
            editDialogStage.initModality(Modality.APPLICATION_MODAL);
            editDialogStage.setScene(new Scene(editDialogRoot));
            editDialogStage.setResizable(false);
            
            // Center the dialog
            editDialogStage.centerOnScreen();
            editDialogStage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("Error loading edit user dialog: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple alert if FXML loading fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load edit dialog");
            alert.setContentText("Could not load the edit user interface. Please try again.");
            alert.showAndWait();
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
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete User " + user.getUsername());
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Delete the user
                userList.remove(user);
            }
        });
    }
} 