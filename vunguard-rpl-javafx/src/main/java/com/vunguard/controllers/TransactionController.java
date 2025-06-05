package com.vunguard.controllers;

import com.vunguard.models.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionController {

    @FXML
    private VBox contentArea;

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, String> idColumn;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, String> descriptionColumn;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private TableColumn<Transaction, String> portfolioColumn;

    @FXML
    private TableColumn<Transaction, String> dateColumn;

    @FXML
    private TableColumn<Transaction, String> statusColumn;

    @FXML
    private TableColumn<Transaction, Void> actionsColumn;

    @FXML
    private Button newTransactionButton;

    // Updated field names to match your FXML
    @FXML
    private ComboBox<String> typeFilter;

    @FXML
    private ComboBox<String> portfolioFilter;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private Button clearFiltersButton;

    @FXML
    private SidebarController sidebarViewController;

    // Sample data for demonstration
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private ObservableList<Transaction> filteredList = FXCollections.observableArrayList();

    // Create Transaction Dialog Fields - These will be injected when dialog is loaded
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> portfolioComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField assetField;
    @FXML private TextField sharesField;
    @FXML private Button createButton;
    @FXML private Button cancelButton;

    private Stage createTransactionStage;

    @FXML
    private void initialize() {
        System.out.println("TransactionController initialized");

        if (sidebarViewController != null) {
            System.out.println("SidebarView Controller injected into TransactionController");
            // Highlight Transactions button in sidebar
            try {
                sidebarViewController.setActiveButton(sidebarViewController.getTransactionsButton());
            } catch (Exception e) {
                System.err.println("Error setting active button: " + e.getMessage());
            }
        } else {
            System.out.println("SidebarView Controller NOT injected.");
        }

        // Initialize sample data
        initializeSampleData();

        // Configure columns
        setupTableColumns();

        // Setup filters
        setupFilters();

        // Setup button events
        newTransactionButton.setOnAction(event -> {
            System.out.println("New Transaction button clicked");
            showCreateTransactionDialog();
        });

        // Setup clear filters button
        clearFiltersButton.setOnAction(event -> {
            System.out.println("Clear Filters button clicked");
            clearAllFilters();
        });

        // Set the data to the table
        filteredList.addAll(transactionList);
        transactionTable.setItems(filteredList);
    }

    private void initializeSampleData() {
    }

    private void setupTableColumns() {
        // Setup cell value factories
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        portfolioColumn.setCellValueFactory(new PropertyValueFactory<>("portfolio"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateFormatted"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Amount column with currency formatting
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText("");
                } else {
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                    setText(currencyFormat.format(value));
                }
            }
        });

        // Type column with color coding
        typeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(value);
                    switch (value) {
                        case "Buy":
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            break;
                        case "Sell":
                            setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                            break;
                        case "Deposit":
                            setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                            break;
                        case "Withdrawal":
                            setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                            break;
                        case "Dividend":
                            setStyle("-fx-text-fill: #9C27B0; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        // Status column with color coding
        statusColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(value);
                    switch (value) {
                        case "Completed":
                            setStyle("-fx-text-fill: #4CAF50;");
                            break;
                        case "Processing":
                            setStyle("-fx-text-fill: #FF9800;");
                            break;
                        case "Pending":
                            setStyle("-fx-text-fill: #F44336;");
                            break;
                        default:
                            setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        // Setup the actions column with buttons
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        // Set column properties
        actionsColumn.setPrefWidth(150);
        actionsColumn.setMinWidth(150);

        // Center the column header text
        actionsColumn.setStyle("-fx-alignment: CENTER;");

        actionsColumn.setCellFactory(new Callback<TableColumn<Transaction, Void>, TableCell<Transaction, Void>>() {
            @Override
            public TableCell<Transaction, Void> call(TableColumn<Transaction, Void> param) {
                return new TableCell<Transaction, Void>() {
                    private final Button viewButton = new Button("View");

                    {
                        // Configure view button
                        viewButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                        viewButton.setPrefWidth(70);
                        viewButton.setPrefHeight(25);
                        viewButton.setOnAction(event -> {
                            Transaction transaction = getTableView().getItems().get(getIndex());
                            System.out.println("View button clicked for: " + transaction.getId());
                            showViewTransactionDialog(transaction);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Create container for centering
                            HBox buttonContainer = new HBox();
                            buttonContainer.setAlignment(Pos.CENTER);
                            buttonContainer.setPrefWidth(150); // Match column width
                            buttonContainer.getChildren().add(viewButton);

                            // Center the cell content
                            setAlignment(Pos.CENTER);
                            setGraphic(buttonContainer);
                        }
                    }
                };
            }
        });
    }

    private void setupFilters() {
        // Setup filter combo boxes with updated field names
        typeFilter.getItems().addAll("All Types", "Buy", "Sell", "Deposit", "Withdrawal", "Dividend");
        typeFilter.setValue("All Types");

        portfolioFilter.getItems().addAll("All Portfolios", "Conservative Portfolio", "Growth Portfolio", "Income Portfolio");
        portfolioFilter.setValue("All Portfolios");

        statusFilter.getItems().addAll("All Statuses", "Completed", "Processing", "Pending");
        statusFilter.setValue("All Statuses");

        // Add listeners for filters
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        portfolioFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        filteredList.clear();
        
        String typeFilterValue = typeFilter.getValue();
        String portfolioFilterValue = portfolioFilter.getValue();
        String statusFilterValue = statusFilter.getValue();

        for (Transaction transaction : transactionList) {
            boolean typeMatch = typeFilterValue.equals("All Types") || transaction.getType().equals(typeFilterValue);
            boolean portfolioMatch = portfolioFilterValue.equals("All Portfolios") || transaction.getPortfolio().equals(portfolioFilterValue);
            boolean statusMatch = statusFilterValue.equals("All Statuses") || transaction.getStatus().equals(statusFilterValue);

            if (typeMatch && portfolioMatch && statusMatch) {
                filteredList.add(transaction);
            }
        }
    }

    private void clearAllFilters() {
        typeFilter.setValue("All Types");
        portfolioFilter.setValue("All Portfolios");
        statusFilter.setValue("All Statuses");
        // applyFilters() will be called automatically due to the listeners
    }

    private void showCreateTransactionDialog() {
        try {
            // Load the CreateTransactionView FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vunguard/views/CreateTransactionView.fxml"));
            
            // Set this controller as the controller for the FXML
            loader.setController(this);
            
            Parent root = loader.load();
            
            // The @FXML fields should now be automatically injected
            // Let's verify they were injected properly
            if (typeComboBox == null || descriptionField == null || amountField == null || 
                portfolioComboBox == null || statusComboBox == null || createButton == null || cancelButton == null) {
                System.err.println("FXML injection failed, trying manual lookup...");
                initializeCreateTransactionDialog(root);
            } else {
                System.out.println("FXML injection successful!");
                setupCreateTransactionDialog();
            }
            
            // Create and show the dialog
            createTransactionStage = new Stage();
            createTransactionStage.setTitle("Create New Transaction");
            createTransactionStage.initModality(Modality.WINDOW_MODAL);
            createTransactionStage.initOwner(newTransactionButton.getScene().getWindow());
            
            // Set icon
            try {
                createTransactionStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/vunguard/assets/logo_white.png")));
            } catch (Exception e) {
                System.err.println("Could not load dialog icon: " + e.getMessage());
            }
            
            Scene scene = new Scene(root, 500, 600);
            
            // Load CSS
            try {
                String css = getClass().getResource("/com/vunguard/styles/application.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (NullPointerException e) {
                System.err.println("Could not load CSS file: " + e.getMessage());
            }
            
            createTransactionStage.setScene(scene);
            createTransactionStage.setResizable(false);
            createTransactionStage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("Error loading CreateTransactionView: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error opening create transaction dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Unexpected error opening dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void initializeCreateTransactionDialog(Parent root) {
        try {
            System.out.println("Starting dialog component initialization...");
            
            // Find and initialize dialog components
            typeComboBox = (ComboBox<String>) root.lookup("#typeComboBox");
            descriptionField = (TextField) root.lookup("#descriptionField");
            amountField = (TextField) root.lookup("#amountField");
            portfolioComboBox = (ComboBox<String>) root.lookup("#portfolioComboBox");
            statusComboBox = (ComboBox<String>) root.lookup("#statusComboBox");
            assetField = (TextField) root.lookup("#assetField");
            sharesField = (TextField) root.lookup("#sharesField");
            createButton = (Button) root.lookup("#createButton");
            cancelButton = (Button) root.lookup("#cancelButton");

            // Debug: Check which components were found
            System.out.println("Component lookup results:");
            System.out.println("typeComboBox: " + (typeComboBox != null ? "Found" : "NOT FOUND"));
            System.out.println("descriptionField: " + (descriptionField != null ? "Found" : "NOT FOUND"));
            System.out.println("amountField: " + (amountField != null ? "Found" : "NOT FOUND"));
            System.out.println("portfolioComboBox: " + (portfolioComboBox != null ? "Found" : "NOT FOUND"));
            System.out.println("statusComboBox: " + (statusComboBox != null ? "Found" : "NOT FOUND"));
            System.out.println("createButton: " + (createButton != null ? "Found" : "NOT FOUND"));
            System.out.println("cancelButton: " + (cancelButton != null ? "Found" : "NOT FOUND"));

            // Verify all components were found
            if (typeComboBox == null || descriptionField == null || amountField == null || 
                portfolioComboBox == null || statusComboBox == null || createButton == null || cancelButton == null) {
                throw new RuntimeException("Could not find all required dialog components");
            }

            // Setup the dialog
            setupCreateTransactionDialog();

            System.out.println("Create Transaction dialog initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Error initializing create transaction dialog: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void setupCreateTransactionDialog() {
        // Setup ComboBoxes with options
        typeComboBox.getItems().clear();
        typeComboBox.getItems().addAll("Buy", "Sell", "Deposit", "Withdrawal", "Dividend");
        typeComboBox.setValue("Buy");
        
        portfolioComboBox.getItems().clear();
        portfolioComboBox.getItems().addAll("Conservative Portfolio", "Growth Portfolio", "Income Portfolio");
        portfolioComboBox.setValue("Conservative Portfolio");
        
        statusComboBox.getItems().clear();
        statusComboBox.getItems().addAll("Completed", "Processing", "Pending");
        statusComboBox.setValue("Completed");

        // Add CSS styling for ComboBox dropdowns
        String comboBoxCss = """
        .combo-box-popup > .list-view {
            -fx-background-color: #2A2A3A;
        }
        .combo-box-popup > .list-view > .virtual-flow > .clipped-container > .sheet > .list-cell {
            -fx-text-fill: white;
            -fx-background-color: #2A2A3A;
        }
        .combo-box-popup > .list-view > .virtual-flow > .clipped-container > .sheet > .list-cell:hover {
            -fx-background-color: #3A3A4A;
        }
        .combo-box-popup > .list-view > .virtual-flow > .clipped-container > .sheet > .list-cell:selected {
            -fx-background-color: #0A84FF;
        }
        """;
    
        // Apply CSS to each ComboBox
        typeComboBox.setStyle(typeComboBox.getStyle() + "; " + comboBoxCss);
        portfolioComboBox.setStyle(portfolioComboBox.getStyle() + "; " + comboBoxCss);
        statusComboBox.setStyle(statusComboBox.getStyle() + "; " + comboBoxCss);

        // Setup input validation
        setupCreateTransactionInputValidation();
        
        // Setup auto-description updates
        setupAutoDescription();
        
        // Setup button states
        setupButtonStates();
    }

    private void setupCreateTransactionInputValidation() {
        // Only allow numeric input for amount field
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                amountField.setText(oldValue);
            }
        });
        
        // Only allow numeric input for shares field (if exists)
        if (sharesField != null) {
            sharesField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    sharesField.setText(oldValue);
                }
            });
        }
    }

    private void setupAutoDescription() {
        // Auto-update description based on type and asset
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> 
            updateDescriptionBasedOnType(newVal, assetField != null ? assetField.getText() : "", descriptionField));
        
        if (assetField != null) {
            assetField.textProperty().addListener((obs, oldVal, newVal) -> 
                updateDescriptionBasedOnType(typeComboBox.getValue(), newVal, descriptionField));
        }
    }

    private void setupButtonStates() {
        // Enable/disable create button based on required fields
        createButton.disableProperty().bind(
            descriptionField.textProperty().isEmpty()
            .or(amountField.textProperty().isEmpty())
            .or(typeComboBox.valueProperty().isNull())
            .or(portfolioComboBox.valueProperty().isNull())
        );
        
        // Focus on description field when dialog opens
        javafx.application.Platform.runLater(() -> descriptionField.requestFocus());
    }

    private void updateDescriptionBasedOnType(String type, String asset, TextField descriptionField) {
        if (type != null && !descriptionField.getText().isEmpty() && descriptionField.isFocused()) {
            return; // Don't auto-update if user has manually entered description
        }
        
        if (type != null) {
            String baseDescription = "";
            switch (type) {
                case "Buy":
                    baseDescription = asset.isEmpty() ? "Purchase of stock" : "Purchase of " + asset.toUpperCase() + " stock";
                    break;
                case "Sell":
                    baseDescription = asset.isEmpty() ? "Sale of stock" : "Sale of " + asset.toUpperCase() + " stock";
                    break;
                case "Deposit":
                    baseDescription = "Cash deposit";
                    break;
                case "Withdrawal":
                    baseDescription = "Cash withdrawal";
                    break;
                case "Dividend":
                    baseDescription = asset.isEmpty() ? "Dividend payment" : "Dividend payment from " + asset.toUpperCase();
                    break;
            }
            
            if (!descriptionField.isFocused()) {
                descriptionField.setText(baseDescription);
            }
        }
    }

    // FXML Event Handlers for Create Transaction Dialog
    @FXML
    private void handleCreateAction(ActionEvent event) {
        if (validateCreateTransactionInput()) {
            Transaction transaction = createTransactionFromInput();
            if (transaction != null) {
                transactionList.add(transaction);
                applyFilters(); // Reapply filters to ensure new transaction shows if it meets criteria
                showSuccessAlert("Transaction '" + transaction.getId() + "' created successfully!");
                
                // Log the creation
                System.out.println("Transaction created: " + transaction.getId());
                System.out.println("Type: " + transaction.getType() + ", Amount: $" + String.format("%.2f", transaction.getAmount()));
                
                // Close the dialog
                closeCreateTransactionDialog();
            }
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        closeCreateTransactionDialog();
    }

    private boolean validateCreateTransactionInput() {
        // Check required fields
        if (descriptionField.getText().trim().isEmpty()) {
            showAlert("Transaction description is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (amountField.getText().trim().isEmpty()) {
            showAlert("Amount is required!", Alert.AlertType.ERROR);
            return false;
        }

        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showAlert("Amount must be greater than zero!", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid amount!", Alert.AlertType.ERROR);
            return false;
        }

        if (typeComboBox.getValue() == null) {
            showAlert("Please select a transaction type!", Alert.AlertType.ERROR);
            return false;
        }

        if (portfolioComboBox.getValue() == null) {
            showAlert("Please select a portfolio!", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private Transaction createTransactionFromInput() {
        try {
            String type = typeComboBox.getValue();
            String description = descriptionField.getText().trim();
            double amount = Double.parseDouble(amountField.getText());
            String portfolio = portfolioComboBox.getValue();
            String status = statusComboBox.getValue();
            
            // Enhance description with asset info if provided
            String finalDescription = description;
            if (assetField != null && !assetField.getText().trim().isEmpty()) {
                finalDescription = description + " (" + assetField.getText().trim().toUpperCase() + ")";
                if (sharesField != null && !sharesField.getText().trim().isEmpty()) {
                    finalDescription += " - " + sharesField.getText().trim() + " shares";
                }
            }
            
            return new Transaction(
                "TRX-" + UUID.randomUUID().toString().substring(0, 8),
                type,
                finalDescription,
                amount,
                portfolio,
                LocalDateTime.now(),
                status
            );
            
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid amount!", Alert.AlertType.ERROR);
            return null;
        }
    }

    private void closeCreateTransactionDialog() {
        if (createTransactionStage != null) {
            createTransactionStage.close();
        }
    }

    private void showViewTransactionDialog(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction Details");
        alert.setHeaderText(transaction.getId());
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        String content = String.format(
            "Type: %s\n" +
            "Description: %s\n" +
            "Amount: %s\n" +
            "Portfolio: %s\n" +
            "Date: %s\n" +
            "Status: %s",
            transaction.getType(),
            transaction.getDescription(),
            currencyFormat.format(transaction.getAmount()),
            transaction.getPortfolio(),
            transaction.getDateFormatted(),
            transaction.getStatus()
        );
        
        alert.setContentText(content);
        
        // Style the alert dialog
        alert.getDialogPane().setStyle("-fx-background-color: #1E1E2C;");
        
        // Get button and style it
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        }
        
        alert.showAndWait();
    }
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Transaction Management");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert dialog
        alert.getDialogPane().setStyle("-fx-background-color: #1E1E2C;");
        
        // Get button and style it
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            if (type == Alert.AlertType.ERROR) {
                okButton.setStyle("-fx-background-color: #F87171; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            } else {
                okButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            }
        }
        
        alert.showAndWait();
    }
    
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert dialog
        alert.getDialogPane().setStyle("-fx-background-color: #1E1E2C;");
        
        // Get button and style it
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: #4ADE80; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        }
        
        alert.showAndWait();
    }
}