package com.vunguard.controllers;

import com.vunguard.models.Portfolio;
import com.vunguard.services.PortfolioService;
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
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.io.IOException;

public class PortfolioController {

    @FXML
    private VBox contentArea;

    @FXML
    private TableView<Portfolio> portfolioTable;

    @FXML
    private TableColumn<Portfolio, String> nameColumn;

    @FXML
    private TableColumn<Portfolio, Double> assetValueColumn;

    @FXML
    private TableColumn<Portfolio, Double> cashBalanceColumn;

    @FXML
    private TableColumn<Portfolio, Double> totalValueColumn;

    @FXML
    private TableColumn<Portfolio, Double> returnColumn;

    @FXML
    private TableColumn<Portfolio, Integer> assetsColumn;

    @FXML
    private TableColumn<Portfolio, String> lastUpdatedColumn;

    @FXML
    private TableColumn<Portfolio, Void> actionsColumn;

    @FXML
    private Button createPortfolioButton;

    @FXML
    private VBox investorSelectionSection;

    @FXML
    private ComboBox<String> investorSelectionComboBox;

    @FXML
    private SidebarController sidebarViewController;

    // Portfolio data
    private ObservableList<Portfolio> portfolioList = FXCollections.observableArrayList();
    
    // Services
    private PortfolioService portfolioService;
    private AuthenticationService authService;
    
    // Currently selected investor (for manager/admin view)
    private Integer selectedInvestorId = null;
    private javafx.beans.value.ChangeListener<String> investorSelectionListener;

    // Create Portfolio Dialog Fields - These will be injected when dialog is loaded
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Button createButton;
    @FXML private Button cancelButton;

    private Stage createPortfolioStage;

    @FXML
    private void initialize() {
        System.out.println("PortfolioController initialized - Current selectedInvestorId: " + selectedInvestorId);

        // Initialize services
        try {
            portfolioService = PortfolioService.getInstance();
            authService = AuthenticationService.getInstance();
        } catch (Exception e) {
            System.err.println("Failed to initialize services: " + e.getMessage());
            e.printStackTrace();
        }

        if (sidebarViewController != null) {
            System.out.println("SidebarView Controller injected into PortfolioController");
            // Highlight Portfolio button in sidebar
            try {
                sidebarViewController.setActiveButton(sidebarViewController.getPortfoliosButton());
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

        // Setup button events
        createPortfolioButton.setOnAction(event -> {
            System.out.println("Create Portfolio button clicked");
            showCreatePortfolioDialog();
        });

        // Setup investor selection for managers/analysts
        setupInvestorSelection();

        // Set the data to the table
        portfolioTable.setItems(portfolioList);
        
        // Load portfolios from database
        loadPortfoliosFromDatabase();
        
        System.out.println("PortfolioController initialization complete - Final selectedInvestorId: " + selectedInvestorId);
    }



    private void setupTableColumns() {
        // Setup cell value factories
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Asset Value column with currency formatting
        assetValueColumn.setCellValueFactory(new PropertyValueFactory<>("assetValue"));
        assetValueColumn.setCellFactory(column -> new TableCell<Portfolio, Double>() {
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
        
        // Cash Balance column with currency formatting
        cashBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("cashBalance"));
        cashBalanceColumn.setCellFactory(column -> new TableCell<Portfolio, Double>() {
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
        
        // Total Value column with currency formatting
        totalValueColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        totalValueColumn.setCellFactory(column -> new TableCell<Portfolio, Double>() {
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
        
        // Return column with percentage formatting
        returnColumn.setCellValueFactory(new PropertyValueFactory<>("returnPercentage"));
        returnColumn.setCellFactory(column -> new TableCell<Portfolio, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText("");
                } else {
                    setStyle(value >= 0 ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #F44336;");
                    setText(String.format("%+.2f%%", value));
                }
            }
        });
        
        assetsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfAssets"));
        lastUpdatedColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdatedFormatted"));
        
        // Setup the actions column with buttons
        setupActionsColumn();
    }
    
    private void setupActionsColumn() {
        // Set column properties
        actionsColumn.setPrefWidth(100);
        actionsColumn.setMinWidth(100);
        
        // Center the column header text
        actionsColumn.setStyle("-fx-alignment: CENTER;");
        
        actionsColumn.setCellFactory(new Callback<TableColumn<Portfolio, Void>, TableCell<Portfolio, Void>>() {
            @Override
            public TableCell<Portfolio, Void> call(TableColumn<Portfolio, Void> param) {
                return new TableCell<Portfolio, Void>() {
                    private final Button viewButton = new Button("View");
                    
                    {
                        // Configure view button
                        viewButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                        viewButton.setPrefWidth(70);
                        viewButton.setPrefHeight(25);
                        viewButton.setOnAction(event -> {
                            Portfolio portfolio = getTableView().getItems().get(getIndex());
                            System.out.println("View button clicked for: " + portfolio.getName());
                            showViewPortfolioDialog(portfolio);
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
                            buttonContainer.setPrefWidth(100); // Match column width
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
    
    private void showCreatePortfolioDialog() {
        try {
            // Load the CreatePortfolioView FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vunguard/views/CreatePortfolioView.fxml"));
            
            // Set this controller as the controller for the FXML
            loader.setController(this);
            
            Parent root = loader.load();
            
            // The @FXML fields should now be automatically injected
            // Let's verify they were injected properly
            if (nameField == null || descriptionField == null || createButton == null || cancelButton == null) {
                System.err.println("FXML injection failed, trying manual lookup...");
                initializeCreatePortfolioDialog(root);
            } else {
                System.out.println("FXML injection successful!");
                setupCreatePortfolioInputValidation();
                // Create a binding that checks for trimmed empty text
                BooleanBinding nameEmptyBinding = Bindings.createBooleanBinding(
                    () -> nameField.getText().trim().isEmpty(),
                    nameField.textProperty()
                );
                createButton.disableProperty().bind(nameEmptyBinding);
                javafx.application.Platform.runLater(() -> nameField.requestFocus());
            }
            
            // Create and show the dialog
            createPortfolioStage = new Stage();
            createPortfolioStage.setTitle("Create New Portfolio");
            createPortfolioStage.initModality(Modality.WINDOW_MODAL);
            createPortfolioStage.initOwner(createPortfolioButton.getScene().getWindow());
            
            // Set icon
            try {
                createPortfolioStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/vunguard/assets/logo_white.png")));
            } catch (Exception e) {
                System.err.println("Could not load dialog icon: " + e.getMessage());
            }
            
            Scene scene = new Scene(root, 600, 500);
            
            // Load CSS
            try {
                String css = getClass().getResource("/com/vunguard/styles/application.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (NullPointerException e) {
                System.err.println("Could not load CSS file: " + e.getMessage());
            }
            
            createPortfolioStage.setScene(scene);
            createPortfolioStage.setResizable(false);
            createPortfolioStage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("Error loading CreatePortfolioView: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error opening create portfolio dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Unexpected error opening dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void initializeCreatePortfolioDialog(Parent root) {
        try {
            System.out.println("Starting dialog component initialization...");
            
            // Debug: Print all nodes with IDs
            debugPrintAllNodes(root, 0);
            
            // Find and initialize dialog components
            nameField = (TextField) root.lookup("#nameField");
            descriptionField = (TextArea) root.lookup("#descriptionField");
            createButton = (Button) root.lookup("#createButton");
            cancelButton = (Button) root.lookup("#cancelButton");

        // Debug: Check which components were found
        System.out.println("Component lookup results:");
        System.out.println("nameField: " + (nameField != null ? "Found" : "NOT FOUND"));
        System.out.println("descriptionField: " + (descriptionField != null ? "Found" : "NOT FOUND"));
        System.out.println("createButton: " + (createButton != null ? "Found" : "NOT FOUND"));
        System.out.println("cancelButton: " + (cancelButton != null ? "Found" : "NOT FOUND"));

        // Verify all components were found
        if (nameField == null || descriptionField == null || createButton == null || cancelButton == null) {
            throw new RuntimeException("Could not find all required dialog components");
        }

        // Setup input validation
        setupCreatePortfolioInputValidation();
        
        // Enable/disable create button based on name field (trimmed)
        BooleanBinding nameEmptyBinding = Bindings.createBooleanBinding(
            () -> nameField.getText().trim().isEmpty(),
            nameField.textProperty()
        );
        createButton.disableProperty().bind(nameEmptyBinding);
        
        // Focus on name field
        javafx.application.Platform.runLater(() -> nameField.requestFocus());

        System.out.println("Create Portfolio dialog initialized successfully");
        
    } catch (Exception e) {
        System.err.println("Error initializing create portfolio dialog: " + e.getMessage());
        e.printStackTrace();
        throw e;
    }
}

// Debug helper method
private void debugPrintAllNodes(javafx.scene.Node node, int depth) {
    String indent = "  ".repeat(depth);
    String nodeInfo = indent + node.getClass().getSimpleName();
    
    if (node.getId() != null && !node.getId().isEmpty()) {
        nodeInfo += " [ID: " + node.getId() + "]";
    }
    
    System.out.println(nodeInfo);
    
    if (node instanceof javafx.scene.Parent) {
        javafx.scene.Parent parent = (javafx.scene.Parent) node;
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            debugPrintAllNodes(child, depth + 1);
        }
    }
}
    private void setupCreatePortfolioInputValidation() {
        // Simple validation for portfolio name
        if (nameField != null) {
            nameField.textProperty().addListener((observable, oldValue, newValue) -> {
                // Note: createButton disable state is already handled by binding in showCreatePortfolioDialog()
                // No manual setDisable() needed here since it's bound to nameField.textProperty().isEmpty()
            });
        }
    }

    // FXML Event Handlers for Create Portfolio Dialog
    @FXML
    private void handleCreateAction(ActionEvent event) {
        if (validateCreatePortfolioInput()) {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            double initialCash = 0.0; // Default to 0, could be made configurable
            
            if (portfolioService != null) {
                boolean success;
                
                // Check if we should create for selected investor or current user
                if (selectedInvestorId != null && authService != null && authService.isLoggedIn()) {
                    String currentUserRole = authService.getCurrentUserRole();
                    boolean canCreateForOthers = "manager".equals(currentUserRole) || 
                                               "analyst".equals(currentUserRole) || 
                                               "admin".equals(currentUserRole);
                    
                    if (canCreateForOthers) {
                        success = portfolioService.createPortfolioForUser(name, description, initialCash, selectedInvestorId);
                    } else {
                        success = portfolioService.createPortfolio(name, description, initialCash);
                    }
                } else {
                    success = portfolioService.createPortfolio(name, description, initialCash);
                }
                
                if (success) {
                    // Show success message
                    String investorName = selectedInvestorId != null ? 
                        investorSelectionComboBox.getValue() : "your account";
                    showSuccessAlert("Portfolio '" + name + "' created successfully for " + investorName + "!");
                    
                    // Reload portfolios - use specific investor if selected, otherwise general reload
                    if (selectedInvestorId != null && investorSelectionComboBox != null && investorSelectionComboBox.getValue() != null) {
                        // Reload for the specific selected investor
                        loadPortfoliosForInvestor(investorSelectionComboBox.getValue());
                    } else {
                        // Reload current user's portfolios
                        loadPortfoliosFromDatabase();
                    }
                    
                    // Close the dialog
                    closeCreatePortfolioDialog();
                } else {
                    showAlert("Failed to create portfolio. Please try again.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Portfolio service not available. Please try again.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        closeCreatePortfolioDialog();
    }

    private boolean validateCreatePortfolioInput() {
        String name = nameField.getText().trim();
        
        // Validate portfolio name
        if (name.isEmpty()) {
            showAlert("Portfolio name is required!", Alert.AlertType.ERROR);
            return false;
        }
        
        // Check for duplicate portfolio name
        for (Portfolio existing : portfolioList) {
            if (existing.getName().equalsIgnoreCase(name)) {
                showAlert("Portfolio name already exists! Please choose a different name.", Alert.AlertType.ERROR);
                return false;
            }
        }
        
        return true;
    }

    private void closeCreatePortfolioDialog() {
        if (createPortfolioStage != null) {
            createPortfolioStage.close();
        }
    }
    
    private void showViewPortfolioDialog(Portfolio portfolio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Portfolio Details");
        alert.setHeaderText(portfolio.getName());
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        String content = String.format(
            "Asset Value: %s\n" +
            "Cash Balance: %s\n" +
            "Total Value: %s\n" +
            "Return: %+.2f%%\n" +
            "Number of Assets: %d\n" +
            "Last Updated: %s",
            currencyFormat.format(portfolio.getAssetValue()),
            currencyFormat.format(portfolio.getCashBalance()),
            currencyFormat.format(portfolio.getTotalValue()),
            portfolio.getReturnPercentage(),
            portfolio.getNumberOfAssets(),
            portfolio.getLastUpdatedFormatted()
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
        alert.setTitle("Portfolio Management");
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
            okButton.setStyle("-fx-background-color: #4ADE80; -fx-text-fill: #0F172A; -fx-font-weight: bold; -fx-background-radius: 5;");
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
        }
        
        // Show investor selection section only for managers, analysts, and admins
        investorSelectionSection.setVisible(canViewOtherInvestors);
        investorSelectionSection.setManaged(canViewOtherInvestors);
        
        if (!canViewOtherInvestors) {
            System.out.println("Current user can only view their own portfolios");
        }
    }
    
    private void setupInvestorSelection() {
        if (investorSelectionComboBox != null) {
            // Store current selection if any
            String currentSelection = investorSelectionComboBox.getValue();
            
            // Clear existing items to prevent duplicates
            investorSelectionComboBox.getItems().clear();
            
            // Sample investors for demonstration
            investorSelectionComboBox.getItems().addAll(
                "John Doe (johndoe@email.com)",
                "Jane Smith (janesmith@email.com)", 
                "Bob Johnson (bobjohnson@email.com)",
                "Alice Brown (alicebrown@email.com)",
                "Charlie Wilson (charliewilson@email.com)"
            );
            
            // Restore previous selection or set default
            if (currentSelection != null && investorSelectionComboBox.getItems().contains(currentSelection)) {
                // Restore previous selection
                investorSelectionComboBox.setValue(currentSelection);
                selectedInvestorId = getInvestorIdFromSelection(currentSelection);
                System.out.println("Restored investor selection: " + currentSelection);
            } else if (investorSelectionComboBox.getValue() == null) {
                // Set default selection to first investor
                String defaultSelection = "John Doe (johndoe@email.com)";
                investorSelectionComboBox.setValue(defaultSelection);
                selectedInvestorId = getInvestorIdFromSelection(defaultSelection);
                System.out.println("Set default investor selection: " + defaultSelection);
            }
            
            // Clear existing listeners to prevent multiple listeners
            if (investorSelectionListener != null) {
                investorSelectionComboBox.valueProperty().removeListener(investorSelectionListener);
            }
            
            // Add listener for selection changes
            investorSelectionListener = (obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.equals(oldVal)) {
                    System.out.println("Investor selection changed from: " + oldVal + " to: " + newVal);
                    selectedInvestorId = getInvestorIdFromSelection(newVal);
                    loadPortfoliosForInvestor(newVal);
                }
            };
            investorSelectionComboBox.valueProperty().addListener(investorSelectionListener);
        }
    }
    
    /**
     * Get investor ID from selection string
     * Queries the database to get the actual user ID
     */
    private Integer getInvestorIdFromSelection(String selection) {
        if (selection == null) return null;
        
        try {
            // Extract email from selection string (format: "Name (email)")
            int startIdx = selection.indexOf('(');
            int endIdx = selection.indexOf(')');
            if (startIdx > 0 && endIdx > startIdx) {
                String email = selection.substring(startIdx + 1, endIdx);
                
                // Query database to get user ID by email
                return getUserIdByEmail(email);
            }
        } catch (Exception e) {
            System.err.println("Error parsing investor selection: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get user ID from email by querying the database
     */
    private Integer getUserIdByEmail(String email) {
        if (authService == null) return null;
        
        try {
            // For now, use a simple mapping based on what we know from schema
            // In a real app, this would be a proper database query
            switch (email) {
                case "johndoe@email.com": return 4; // john_doe will be ID 4 (after admin, analyst, manager)
                case "janesmith@email.com": return 5; // jane_smith will be ID 5
                case "bobjohnson@email.com": return 6; // bob_johnson will be ID 6
                case "alicebrown@email.com": return 7; // alice_brown will be ID 7
                case "charliewilson@email.com": return 8; // charlie_wilson will be ID 8
                default: return null;
            }
        } catch (Exception e) {
            System.err.println("Error getting user ID by email: " + e.getMessage());
            return null;
        }
    }
    
    private void loadPortfoliosForInvestor(String investor) {
        if (portfolioService == null) {
            System.err.println("PortfolioService not initialized, using empty data");
            return;
        }
        
        try {
            List<Portfolio> portfolios;
            
            if (selectedInvestorId != null && authService != null && authService.isLoggedIn()) {
                String currentUserRole = authService.getCurrentUserRole();
                boolean canViewOthers = "manager".equals(currentUserRole) || 
                                       "analyst".equals(currentUserRole) || 
                                       "admin".equals(currentUserRole);
                
                if (canViewOthers) {
                    // Load portfolios for selected investor
                    portfolios = portfolioService.getPortfoliosForUser(selectedInvestorId);
                    System.out.println("Loaded " + portfolios.size() + " portfolios for investor: " + investor);
                } else {
                    // Fall back to current user's portfolios
                    portfolios = portfolioService.getUserPortfolios();
                    System.out.println("Loading current user's portfolios (no permission for others)");
                }
            } else {
                // Load current user's portfolios
                portfolios = portfolioService.getUserPortfolios();
                System.out.println("Loading current user's portfolios");
            }
            
            portfolioList.clear();
            portfolioList.addAll(portfolios);
            
        } catch (Exception e) {
            System.err.println("Failed to load portfolios for investor: " + e.getMessage());
            e.printStackTrace();
            
            // Show error to user
            showAlert("Failed to load portfolios. Please check your connection.", 
                     Alert.AlertType.ERROR);
        }
    }
    
    private void loadPortfoliosFromDatabase() {
        // Use the current investor selection if available
        if (selectedInvestorId != null && investorSelectionComboBox != null) {
            loadPortfoliosForInvestor(investorSelectionComboBox.getValue());
            return;
        }
        
        if (portfolioService == null) {
            System.err.println("PortfolioService not initialized, using empty data");
            return;
        }
        
        try {
            List<Portfolio> portfolios = portfolioService.getUserPortfolios();
            portfolioList.clear();
            portfolioList.addAll(portfolios);
            System.out.println("Loaded " + portfolios.size() + " portfolios from database");
        } catch (Exception e) {
            System.err.println("Failed to load portfolios from database: " + e.getMessage());
            e.printStackTrace();
            
            // Show error to user
            showAlert("Failed to load portfolios from database. Please check your connection.", 
                     Alert.AlertType.ERROR);
        }
    }
}