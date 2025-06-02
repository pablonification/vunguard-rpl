package com.vunguard.controllers;

import com.vunguard.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import java.time.LocalDate;
import java.util.Arrays;

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
    }
    
    private void showEditUserDialog(User user) {
        // In a real app, show a dialog to edit user details
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit User");
        alert.setHeaderText("Edit User " + user.getUsername());
        alert.setContentText("This would show a form to edit user details.");
        alert.showAndWait();
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