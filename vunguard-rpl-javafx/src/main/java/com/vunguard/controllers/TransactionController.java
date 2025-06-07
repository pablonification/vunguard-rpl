package com.vunguard.controllers;

import com.vunguard.models.Transaction;
import com.vunguard.services.TransactionService;
import com.vunguard.services.AuthenticationService;
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
import java.util.List;
import java.util.UUID;

public class TransactionController {

    @FXML
    private VBox contentArea;

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, String> portfolioColumn;

    @FXML
    private TableColumn<Transaction, String> productColumn;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, Integer> quantityColumn;

    @FXML
    private TableColumn<Transaction, Double> priceColumn;

    @FXML
    private TableColumn<Transaction, Double> totalColumn;

    @FXML
    private TableColumn<Transaction, String> dateColumn;

    @FXML
    private TableColumn<Transaction, Void> actionsColumn;

    @FXML
    private Button newTransactionButton;

    @FXML
    private VBox investorSelectionSection;

    @FXML
    private ComboBox<String> investorSelectionComboBox;

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

    // Data for display
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private ObservableList<Transaction> filteredList = FXCollections.observableArrayList();
    
    // Services
    private TransactionService transactionService;
    private AuthenticationService authService;

    // Create Transaction Dialog Fields - These will be injected when dialog is loaded
    @FXML private ComboBox<String> investorComboBox;
    @FXML private ComboBox<String> portfolioComboBox;
    @FXML private ComboBox<String> productComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private TextField totalField;
    @FXML private TextArea notesField;
    @FXML private Button createButton;
    @FXML private Button cancelButton;

    private Stage createTransactionStage;

    @FXML
    private void initialize() {
        System.out.println("TransactionController initialized");

        // Initialize services
        try {
            transactionService = TransactionService.getInstance();
            authService = AuthenticationService.getInstance();
        } catch (Exception e) {
            System.err.println("Failed to initialize services: " + e.getMessage());
            e.printStackTrace();
        }

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

        // Setup role-based UI
        setupRoleBasedUI();

        // Configure columns
        setupTableColumns();
        setupTypeColumnFormatting();

        // Setup investor selection for managers/analysts
        setupInvestorSelection();

        // Setup button events
        newTransactionButton.setOnAction(event -> {
            System.out.println("New Transaction button clicked");
            showCreateTransactionDialog();
        });

        // Only load transactions automatically for regular investors, not for admin/manager/analyst
        if (authService != null && authService.isLoggedIn()) {
            String currentUserRole = authService.getCurrentUserRole();
            boolean isRegularInvestor = "investor".equals(currentUserRole);
            
            if (isRegularInvestor) {
                // Regular investors see their own transactions immediately
                loadTransactionsFromDatabase();
                filteredList.addAll(transactionList);
            } else {
                // Admin/manager/analyst must select an investor first
                System.out.println("Admin/manager/analyst must select an investor to view transactions");
            }
        }

        // Set the data to the table
        transactionTable.setItems(filteredList);
    }

    private void initializeSampleData() {
    }

    private void setupTableColumns() {
        // Setup cell value factories
        portfolioColumn.setCellValueFactory(new PropertyValueFactory<>("portfolio"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("product"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateFormatted"));

        // Price column with currency formatting
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
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

        // Total column with currency formatting
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
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

        // Setup actions column with buttons
        setupActionsColumn();
    }

    // Type column with color coding (will be called separately)
    private void setupTypeColumnFormatting() {
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
        typeFilter.getItems().addAll("All Types", "BUY", "SELL", "DEPOSIT", "WITHDRAWAL");
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
        
        // Safe null checking for filter values
        String typeFilterValue = (typeFilter != null && typeFilter.getValue() != null) ? typeFilter.getValue() : "All Types";
        String portfolioFilterValue = (portfolioFilter != null && portfolioFilter.getValue() != null) ? portfolioFilter.getValue() : "All Portfolios";
        String statusFilterValue = (statusFilter != null && statusFilter.getValue() != null) ? statusFilter.getValue() : "All Statuses";

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
            if (investorComboBox == null || portfolioComboBox == null || productComboBox == null ||
                typeComboBox == null || quantityField == null || priceField == null || 
                totalField == null || notesField == null || createButton == null || cancelButton == null) {
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
            investorComboBox = (ComboBox<String>) root.lookup("#investorComboBox");
            portfolioComboBox = (ComboBox<String>) root.lookup("#portfolioComboBox");
            productComboBox = (ComboBox<String>) root.lookup("#productComboBox");
            typeComboBox = (ComboBox<String>) root.lookup("#typeComboBox");
            quantityField = (TextField) root.lookup("#quantityField");
            priceField = (TextField) root.lookup("#priceField");
            totalField = (TextField) root.lookup("#totalField");
            notesField = (TextArea) root.lookup("#notesField");
            createButton = (Button) root.lookup("#createButton");
            cancelButton = (Button) root.lookup("#cancelButton");

            // Debug: Check which components were found
            System.out.println("Component lookup results:");
            System.out.println("investorComboBox: " + (investorComboBox != null ? "Found" : "NOT FOUND"));
            System.out.println("portfolioComboBox: " + (portfolioComboBox != null ? "Found" : "NOT FOUND"));
            System.out.println("productComboBox: " + (productComboBox != null ? "Found" : "NOT FOUND"));
            System.out.println("typeComboBox: " + (typeComboBox != null ? "Found" : "NOT FOUND"));
            System.out.println("quantityField: " + (quantityField != null ? "Found" : "NOT FOUND"));
            System.out.println("priceField: " + (priceField != null ? "Found" : "NOT FOUND"));
            System.out.println("totalField: " + (totalField != null ? "Found" : "NOT FOUND"));
            System.out.println("notesField: " + (notesField != null ? "Found" : "NOT FOUND"));
            System.out.println("createButton: " + (createButton != null ? "Found" : "NOT FOUND"));
            System.out.println("cancelButton: " + (cancelButton != null ? "Found" : "NOT FOUND"));

            // Verify all components were found
            if (investorComboBox == null || portfolioComboBox == null || productComboBox == null ||
                typeComboBox == null || quantityField == null || priceField == null || 
                totalField == null || notesField == null || createButton == null || cancelButton == null) {
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
        // Setup investor ComboBox
        investorComboBox.getItems().addAll(
            "John Doe (johndoe@email.com)",
            "Jane Smith (janesmith@email.com)", 
            "Bob Johnson (bobjohnson@email.com)",
            "Alice Brown (alicebrown@email.com)",
            "Charlie Wilson (charliewilson@email.com)"
        );
        
        // Setup Type ComboBox
        typeComboBox.getItems().clear();
        typeComboBox.getItems().addAll("BUY", "SELL");
        typeComboBox.setValue("BUY");

        // Setup cascading dropdowns
        setupCascadingDropdowns();
        
        // Setup input validation and calculation
        setupNewInputValidation();
        
        // Setup button states
        setupNewButtonStates();
    }

    private void setupCascadingDropdowns() {
        // When investor is selected, enable and populate portfolio dropdown
        investorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                portfolioComboBox.setDisable(false);
                portfolioComboBox.getItems().clear();
                portfolioComboBox.getItems().addAll(
                    "Conservative Growth",
                    "Tech Aggressive", 
                    "Balanced Fund"
                );
                portfolioComboBox.setPromptText("Select a portfolio");
                
                // Reset dependent dropdowns
                productComboBox.setDisable(true);
                productComboBox.getItems().clear();
                productComboBox.setPromptText("Select a portfolio first");
            }
        });

        // When portfolio is selected, enable and populate product dropdown
        portfolioComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                productComboBox.setDisable(false);
                productComboBox.getItems().clear();
                productComboBox.getItems().addAll(
                    "Tech Growth Fund",
                    "Global Bond Fund",
                    "Emerging Markets Fund",
                    "Real Estate Fund"
                );
                productComboBox.setPromptText("Select a product");
            }
        });
    }

    private void setupNewInputValidation() {
        // Only allow numeric input for quantity and price fields
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantityField.setText(oldValue);
            }
            calculateTotal();
        });
        
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
            calculateTotal();
        });
    }

    private void calculateTotal() {
        try {
            if (!quantityField.getText().isEmpty() && !priceField.getText().isEmpty()) {
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());
                double total = quantity * price;
                totalField.setText(String.format("%.2f", total));
            } else {
                totalField.setText("0.00");
            }
        } catch (NumberFormatException e) {
            totalField.setText("0.00");
        }
    }

    private void setupNewButtonStates() {
        // Enable/disable create button based on required fields
        createButton.disableProperty().bind(
            investorComboBox.valueProperty().isNull()
            .or(portfolioComboBox.valueProperty().isNull())
            .or(productComboBox.valueProperty().isNull())
            .or(typeComboBox.valueProperty().isNull())
            .or(quantityField.textProperty().isEmpty())
            .or(priceField.textProperty().isEmpty())
        );
        
        // Focus on investor field when dialog opens
        javafx.application.Platform.runLater(() -> investorComboBox.requestFocus());
    }

    // FXML Event Handlers for Create Transaction Dialog
    @FXML
    private void handleCreateAction(ActionEvent event) {
        if (validateCreateTransactionInput()) {
            // Create transaction using TransactionService
            try {
                String selectedInvestor = investorComboBox.getValue();
                String type = typeComboBox.getValue();
                String portfolio = portfolioComboBox.getValue();
                String product = productComboBox.getValue();
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());
                String notes = notesField.getText().trim();
                
                String description = type + " " + quantity + " shares of " + product + " at $" + price;
                if (!notes.isEmpty()) {
                    description += " - " + notes;
                }
                
                // Extract email from investor selection (e.g., "John Doe (johndoe@email.com)" -> "johndoe@email.com")
                String investorEmail = selectedInvestor.substring(selectedInvestor.indexOf('(') + 1, selectedInvestor.indexOf(')'));
                
                boolean success = transactionService.createTransactionForUser(investorEmail, type, portfolio, product, quantity, price, description);
                
                if (success) {
                    showSuccessAlert("Transaction created successfully!");
                    
                    // Reload transactions from database
                    loadTransactionsFromDatabase();
                    
                    // Refresh the filtered list
                    filteredList.clear();
                    filteredList.addAll(transactionList);
                    
                    // Log the creation
                    System.out.println("Transaction created: " + description);
                    
                    // Close the dialog
                    closeCreateTransactionDialog();
                } else {
                    showAlert("Failed to create transaction. Please try again.", Alert.AlertType.ERROR);
                }
                
            } catch (Exception e) {
                System.err.println("Error creating transaction: " + e.getMessage());
                showAlert("Error creating transaction: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        closeCreateTransactionDialog();
    }

    private boolean validateCreateTransactionInput() {
        // Check required fields
        if (investorComboBox.getValue() == null) {
            showAlert("Please select an investor!", Alert.AlertType.ERROR);
            return false;
        }

        if (portfolioComboBox.getValue() == null) {
            showAlert("Please select a portfolio!", Alert.AlertType.ERROR);
            return false;
        }

        if (productComboBox.getValue() == null) {
            showAlert("Please select a product!", Alert.AlertType.ERROR);
            return false;
        }

        if (quantityField.getText().trim().isEmpty()) {
            showAlert("Quantity is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (priceField.getText().trim().isEmpty()) {
            showAlert("Price per unit is required!", Alert.AlertType.ERROR);
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) {
                showAlert("Quantity must be greater than zero!", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid quantity!", Alert.AlertType.ERROR);
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText());
            if (price <= 0) {
                showAlert("Price must be greater than zero!", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid price!", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private Transaction createTransactionFromInput() {
        try {
            String type = typeComboBox.getValue();
            String portfolio = portfolioComboBox.getValue();
            String product = productComboBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());
            String notes = notesField.getText().trim();
            
            return new Transaction(
                "TRX-" + UUID.randomUUID().toString().substring(0, 8),
                type,
                portfolio,
                product,
                quantity,
                price,
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
            );
            
        } catch (NumberFormatException e) {
            showAlert("Please enter valid numbers!", Alert.AlertType.ERROR);
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
    
    private void setupRoleBasedUI() {
        // Check if user is logged in and get their role
        boolean canViewOtherInvestors = false;
        
        if (authService != null && authService.isLoggedIn()) {
            String currentUserRole = authService.getCurrentUserRole();
            canViewOtherInvestors = "manager".equals(currentUserRole) || 
                                   "analyst".equals(currentUserRole) || 
                                   "admin".equals(currentUserRole);
            System.out.println("Current user role: " + currentUserRole);
        }
        
        // Show investor selection section only for managers, analysts, and admins
        investorSelectionSection.setVisible(canViewOtherInvestors);
        investorSelectionSection.setManaged(canViewOtherInvestors);
        
        if (!canViewOtherInvestors) {
            System.out.println("Current user can only view their own transactions");
        }
    }
    
    private void setupInvestorSelection() {
        if (investorSelectionComboBox != null) {
            // Sample investors for demonstration
            investorSelectionComboBox.getItems().addAll(
                "John Doe (johndoe@email.com)",
                "Jane Smith (janesmith@email.com)", 
                "Bob Johnson (bobjohnson@email.com)",
                "Alice Brown (alicebrown@email.com)",
                "Charlie Wilson (charliewilson@email.com)"
            );
            
            // Set prompt text instead of default selection
            investorSelectionComboBox.setPromptText("Select an investor to view their transactions");
            
            // Add listener for selection changes
            investorSelectionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    System.out.println("Selected investor: " + newVal);
                    loadTransactionsForInvestor(newVal);
                } else {
                    // If selection is cleared, show empty table
                    transactionList.clear();
                    filteredList.clear();
                    System.out.println("No investor selected, showing empty table");
                }
            });
        }
    }
    
    private void loadTransactionsForInvestor(String investor) {
        if (transactionService == null) {
            System.err.println("TransactionService not initialized, cannot load transactions for investor");
            return;
        }
        
        // Clear current transactions
        transactionList.clear();
        filteredList.clear();
        
        try {
            // Extract username from display string (e.g., "John Doe (johndoe@email.com)" -> "johndoe@email.com")
            String email = investor.substring(investor.indexOf('(') + 1, investor.indexOf(')'));
            
            // Get transactions for this investor from database
            List<Transaction> transactions = transactionService.getTransactionsForUser(email);
            transactionList.addAll(transactions);
            
            // Update table
            filteredList.addAll(transactionList);
            System.out.println("Loaded " + transactionList.size() + " transactions for " + investor);
            
        } catch (Exception e) {
            System.err.println("Failed to load transactions for investor " + investor + ": " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to showing empty table
            System.out.println("Loaded 0 transactions for " + investor + " (error occurred)");
        }
    }
    
    private void loadTransactionsFromDatabase() {
        if (transactionService == null) {
            System.err.println("TransactionService not initialized, using empty data");
            return;
        }
        
        try {
            List<Transaction> transactions = transactionService.getUserTransactions();
            transactionList.clear();
            transactionList.addAll(transactions);
            System.out.println("Loaded " + transactions.size() + " transactions from database");
        } catch (Exception e) {
            System.err.println("Failed to load transactions from database: " + e.getMessage());
            e.printStackTrace();
            
            // Show error to user
            showAlert("Failed to load transactions from database. Please check your connection.", 
                     Alert.AlertType.ERROR);
        }
    }

    private void loadSampleTransactions() {
        transactionList.clear();
        
        transactionList.addAll(
            new Transaction("TRX001", "Sell", "Retirement Fund", "Tech Growth Fund", 100, 10.00, "May 12, 2025"),
            new Transaction("TRX002", "Buy", "Retirement Fund", "Tech Growth Fund", 10, 100.00, "May 12, 2025"),
            new Transaction("TRX003", "Sell", "Retirement Fund", "Global Bond Fund", 1, 1000.00, "May 12, 2025")
        );
        
        System.out.println("Sample transactions loaded. Total count: " + transactionList.size());
    }
}