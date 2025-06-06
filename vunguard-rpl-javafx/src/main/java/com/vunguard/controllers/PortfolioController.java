package com.vunguard.controllers;

import com.vunguard.models.Portfolio;
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
    private SidebarController sidebarViewController;

    // Sample data for demonstration
    private ObservableList<Portfolio> portfolioList = FXCollections.observableArrayList();

    // Create Portfolio Dialog Fields - These will be injected when dialog is loaded
    @FXML private TextField nameField;
    @FXML private TextField assetValueField;  
    @FXML private TextField cashBalanceField;
    @FXML private TextField returnField;
    @FXML private TextField assetsField;
    @FXML private Button createButton;
    @FXML private Button cancelButton;

    private Stage createPortfolioStage;

    @FXML
    private void initialize() {
        System.out.println("PortfolioController initialized");

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

        // Configure columns
        setupTableColumns();

        // Setup button events
        createPortfolioButton.setOnAction(event -> {
            System.out.println("Create Portfolio button clicked");
            showCreatePortfolioDialog();
        });

        // Set the data to the table
        portfolioTable.setItems(portfolioList);
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
            if (nameField == null || assetValueField == null || cashBalanceField == null || 
                returnField == null || assetsField == null || createButton == null || cancelButton == null) {
                System.err.println("FXML injection failed, trying manual lookup...");
                initializeCreatePortfolioDialog(root);
            } else {
                System.out.println("FXML injection successful!");
                setupCreatePortfolioInputValidation();
                createButton.disableProperty().bind(nameField.textProperty().isEmpty());
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
            assetValueField = (TextField) root.lookup("#assetValueField");
            cashBalanceField = (TextField) root.lookup("#cashBalanceField");
            returnField = (TextField) root.lookup("#returnField");
            assetsField = (TextField) root.lookup("#assetsField");
            createButton = (Button) root.lookup("#createButton");
            cancelButton = (Button) root.lookup("#cancelButton");

        // Debug: Check which components were found
        System.out.println("Component lookup results:");
        System.out.println("nameField: " + (nameField != null ? "Found" : "NOT FOUND"));
        System.out.println("assetValueField: " + (assetValueField != null ? "Found" : "NOT FOUND"));
        System.out.println("cashBalanceField: " + (cashBalanceField != null ? "Found" : "NOT FOUND"));
        System.out.println("returnField: " + (returnField != null ? "Found" : "NOT FOUND"));
        System.out.println("assetsField: " + (assetsField != null ? "Found" : "NOT FOUND"));
        System.out.println("createButton: " + (createButton != null ? "Found" : "NOT FOUND"));
        System.out.println("cancelButton: " + (cancelButton != null ? "Found" : "NOT FOUND"));

        // Verify all components were found
        if (nameField == null || assetValueField == null || cashBalanceField == null || 
            returnField == null || assetsField == null || createButton == null || cancelButton == null) {
            throw new RuntimeException("Could not find all required dialog components");
        }

        // Setup input validation
        setupCreatePortfolioInputValidation();
        
        // Enable/disable create button based on name field
        createButton.disableProperty().bind(nameField.textProperty().isEmpty());
        
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
        // Allow only numeric input for amount fields
        assetValueField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                assetValueField.setText(oldValue);
            }
        });
        
        cashBalanceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                cashBalanceField.setText(oldValue);
            }
        });
        
        returnField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("-?\\d*\\.?\\d*")) {
                returnField.setText(oldValue);
            }
        });
        
        assetsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                assetsField.setText(oldValue);
            }
        });
    }

    // FXML Event Handlers for Create Portfolio Dialog
    @FXML
    private void handleCreateAction(ActionEvent event) {
        if (validateCreatePortfolioInput()) {
            Portfolio portfolio = createPortfolioFromInput();
            if (portfolio != null) {
                portfolioList.add(portfolio);
                showSuccessAlert("Portfolio '" + portfolio.getName() + "' created successfully!");
                
                // Log the creation
                System.out.println("Portfolio created: " + portfolio.getName());
                System.out.println("Total Value: $" + String.format("%.2f", portfolio.getTotalValue()));
                
                // Close the dialog
                closeCreatePortfolioDialog();
            }
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        closeCreatePortfolioDialog();
    }

    private boolean validateCreatePortfolioInput() {
        try {
            String name = nameField.getText().trim();
            double assetValue = assetValueField.getText().isEmpty() ? 0.0 : Double.parseDouble(assetValueField.getText());
            double cashBalance = cashBalanceField.getText().isEmpty() ? 0.0 : Double.parseDouble(cashBalanceField.getText());
            double returnPercentage = returnField.getText().isEmpty() ? 0.0 : Double.parseDouble(returnField.getText());
            int numberOfAssets = assetsField.getText().isEmpty() ? 0 : Integer.parseInt(assetsField.getText());
            
            // Validate input
            if (name.isEmpty()) {
                showAlert("Portfolio name is required!", Alert.AlertType.ERROR);
                return false;
            }
            
            if (assetValue < 0 || cashBalance < 0 || numberOfAssets < 0) {
                showAlert("Values cannot be negative!", Alert.AlertType.ERROR);
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
            
        } catch (NumberFormatException e) {
            showAlert("Please enter valid numbers for numeric fields!", Alert.AlertType.ERROR);
            return false;
        }
    }    private Portfolio createPortfolioFromInput() {
        try {
            String name = nameField.getText().trim();
            double assetValue = assetValueField.getText().isEmpty() ? 0.0 : Double.parseDouble(assetValueField.getText());
            double cashBalance = cashBalanceField.getText().isEmpty() ? 0.0 : Double.parseDouble(cashBalanceField.getText());
            double returnPercentage = returnField.getText().isEmpty() ? 0.0 : Double.parseDouble(returnField.getText());
            int numberOfAssets = assetsField.getText().isEmpty() ? 0 : Integer.parseInt(assetsField.getText());
            
            // Generate a unique ID for the new portfolio
            String id = "PF" + String.format("%03d", portfolioList.size() + 1);
            
            return new Portfolio(id, name, assetValue, cashBalance, returnPercentage, numberOfAssets);
            
        } catch (NumberFormatException e) {
            showAlert("Please enter valid numbers for numeric fields!", Alert.AlertType.ERROR);
            return null;
        }
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
}