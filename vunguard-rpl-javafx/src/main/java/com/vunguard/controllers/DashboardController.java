package com.vunguard.controllers;

import com.vunguard.models.Portfolio;
import com.vunguard.models.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Locale;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class DashboardController {

    @FXML
    private VBox contentArea; // This will be referenced from the FXML
    
    @FXML
    private ScrollPane scrollPane; // Reference to the ScrollPane    @FXML
    private SidebarController sidebarViewController;    // Sample portfolio data for demonstration
    private final ObservableList<Portfolio> portfolioList = FXCollections.observableArrayList();

    // Top Up Dialog Fields - These will be injected when dialog is loaded
    @FXML private ComboBox<String> portfolioComboBox;
    @FXML private TextField amountField;
    @FXML private TextField descriptionField;
    @FXML private VBox portfolioInfoSection;
    @FXML private Label currentCashLabel;
    @FXML private Label newCashLabel;
    @FXML private Button topUpButton;
    @FXML private Button cancelButton;

    private Stage topUpStage;    @FXML
    private void initialize() {
        System.out.println("DashboardController initialized");
        if (sidebarViewController != null) {
            System.out.println("SidebarView Controller injected into DashboardController");
            // Set the dashboard button as active in the sidebar
            try {
                sidebarViewController.setActiveButton(sidebarViewController.getDashboardButton());
            } catch (Exception e) {
                System.err.println("Error setting active button: " + e.getMessage());
            }
        } else {
            System.out.println("SidebarView Controller NOT injected. Add fx:id=\"sidebarView\" to <fx:include> if direct access is needed.");
        }
        
        // Configure ScrollPane
        if (scrollPane != null) {
            // Make sure the scroll pane takes all available height
            scrollPane.setFitToHeight(false);
            scrollPane.setPannable(true);
            
            // Ensure scrollbar styling
            scrollPane.getStyleClass().add("dashboard-scroll-pane");
        } else {
            System.err.println("scrollPane is null! FXML mapping issue.");
        }
        
        // Initialize sample portfolios
        initializeSamplePortfolios();
        
        // Create dashboard content programmatically
        setupDashboard();
    }
      private void setupDashboard() {
        // Check if contentArea is properly injected
        if (contentArea == null) {
            System.err.println("contentArea is null! FXML mapping issue.");
            return;
        }
        
        // Clear existing content to avoid duplication
        contentArea.getChildren().clear();
        
        // Create title and welcome message
        Label titleLabel = new Label("Dashboard");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label welcomeLabel = new Label("Welcome! Here's an overview of your investment portfolio.");
        welcomeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #A0A0A0;");
        
        // Create button for "Top Up Funds"
        Button topUpFundsButton = new Button("Top Up Funds");
        topUpFundsButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15;");
        
        // Add click event handler for "Top Up Funds" button
        topUpFundsButton.setOnAction(event -> {
            System.out.println("Top Up Funds button clicked");
            showTopUpDialog();
        });
        
        // Add button to right side of header
        HBox headerActions = new HBox();
        headerActions.setAlignment(Pos.CENTER_RIGHT);
        headerActions.getChildren().add(topUpFundsButton);
        
        // Push it to the right of existing header content
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Create header container
        HBox headerContainer = new HBox(10);
        headerContainer.getChildren().add(spacer);
        headerContainer.getChildren().add(headerActions);
        headerContainer.setPadding(new Insets(0, 0, 10, 0)); // Add some padding at the bottom
        
        // Add title, welcome message, and header actions
        contentArea.getChildren().addAll(titleLabel, welcomeLabel, headerContainer);
        
        // Create all rows
        HBox topRow = createTopRow();
        HBox middleRow = createMiddleRow();
        HBox bottomRow = createBottomRow();
        
        // Add spacing between rows
        VBox.setMargin(topRow, new Insets(10, 0, 20, 0));
        VBox.setMargin(middleRow, new Insets(0, 0, 20, 0));
        
        // Add rows to content area
        contentArea.getChildren().addAll(topRow, middleRow, bottomRow);
    }// Method to show the top-up dialog
    private void showTopUpDialog() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vunguard/views/TopUpFundsView.fxml"));
            
            // Set this controller as the controller for the FXML
            loader.setController(this);
            
            Parent root = loader.load();
            
            // Create a new stage for the dialog
            topUpStage = new Stage();
            topUpStage.setTitle("Top Up Funds");
            topUpStage.initModality(Modality.APPLICATION_MODAL);
            topUpStage.setResizable(false);
              // Create scene and set it to the stage
            Scene scene = new Scene(root);
            topUpStage.setScene(scene);
            
            // Initialize the dialog after loading
            initializeTopUpDialog();
            
            // Show the dialog
            topUpStage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("Error loading Top Up Funds dialog: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple alert if FXML loading fails
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Dialog Loading Error");
            alert.setContentText("Could not load the Top Up Funds dialog. Please try again.");
            alert.showAndWait();
        }
    }
    
    // Clear dialog state to prevent issues
    private void clearDialogState() {
        // Reset dialog fields to null to ensure clean state
        if (portfolioComboBox != null) {
            portfolioComboBox.getItems().clear();
            portfolioComboBox.setValue(null);
            portfolioComboBox.getSelectionModel().clearSelection();
        }
        if (amountField != null) {
            amountField.clear();
        }
        if (descriptionField != null) {
            descriptionField.clear();
        }
    }    // Initialize sample portfolios for demonstration
    private void initializeSamplePortfolios() {
        // Clear existing portfolios to prevent duplication across controller instances
        portfolioList.clear();
        
        Portfolio portfolio1 = new Portfolio("PF001", "Conservative Growth", 12000.00, 3000.00, 5.2, 8);
        Portfolio portfolio2 = new Portfolio("PF002", "Balanced Investment", 20000.00, 5000.00, 7.8, 12);
        Portfolio portfolio3 = new Portfolio("PF003", "Aggressive Growth", 6000.00, 2500.00, 12.1, 5);
        
        portfolioList.addAll(portfolio1, portfolio2, portfolio3);
        
        System.out.println("Sample portfolios initialized. Total count: " + portfolioList.size());
    }// Initialize the top-up dialog components
    private void initializeTopUpDialog() {
        System.out.println("Initializing top-up dialog...");
        
        if (portfolioComboBox != null) {
            System.out.println("ComboBox found, current items: " + portfolioComboBox.getItems().size());
            
            // Force complete reset of ComboBox
            portfolioComboBox.getItems().clear();
            portfolioComboBox.setValue(null);
            portfolioComboBox.getSelectionModel().clearSelection();
            
            // Create fresh portfolio names list
            for (Portfolio portfolio : portfolioList) {
                System.out.println("Adding portfolio: " + portfolio.getName());
                portfolioComboBox.getItems().add(portfolio.getName());
            }
            
            // Debug: Print final item count
            System.out.println("ComboBox items after populate: " + portfolioComboBox.getItems().size());
            System.out.println("Final items: " + portfolioComboBox.getItems());
              // Style the ComboBox for better readability with comprehensive CSS
            portfolioComboBox.setStyle(
                "-fx-background-color: #1E1E2C; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #3A3A4A; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 8; " +
                "-fx-font-size: 14px; " +
                "-fx-prompt-text-fill: #A0A0A0;"
            );
            
            // Apply additional styling to ensure dropdown visibility
            portfolioComboBox.getStylesheets().add("data:text/css," +
                ".combo-box-popup .list-view { " +
                "    -fx-background-color: #2A2A3A; " +
                "    -fx-border-color: #3A3A4A; " +
                "} " +
                ".combo-box-popup .list-view .list-cell { " +
                "    -fx-background-color: #2A2A3A; " +
                "    -fx-text-fill: white; " +
                "    -fx-padding: 8 12; " +
                "} " +
                ".combo-box-popup .list-view .list-cell:hover { " +
                "    -fx-background-color: #3A3A4A; " +
                "} " +
                ".combo-box-popup .list-view .list-cell:selected { " +
                "    -fx-background-color: #0A84FF; " +
                "    -fx-text-fill: white; " +
                "} " +
                ".combo-box .arrow-button { " +
                "    -fx-background-color: #1E1E2C; " +
                "} " +
                ".combo-box .arrow { " +
                "    -fx-background-color: white; " +
                "}"
            );
            
            // Set default selection
            if (!portfolioComboBox.getItems().isEmpty()) {
                portfolioComboBox.getSelectionModel().selectFirst();
                updatePortfolioInfo();
            }
            
            // Add listener for portfolio selection changes
            portfolioComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updatePortfolioInfo();
                }
            });
        }
        
        if (amountField != null) {
            // Add listener for amount field changes
            amountField.textProperty().addListener((obs, oldVal, newVal) -> {
                updatePortfolioInfo();
                validateInput();
            });
              // Style the amount field for better readability
            amountField.setStyle(
                "-fx-background-color: #1E1E2C; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #3A3A4A; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 10; " +
                "-fx-font-size: 14px; " +
                "-fx-prompt-text-fill: #A0A0A0;"
            );
        }
        
        if (descriptionField != null) {            // Style the description field for better readability
            descriptionField.setStyle(
                "-fx-background-color: #1E1E2C; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #3A3A4A; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 10; " +
                "-fx-font-size: 14px; " +
                "-fx-prompt-text-fill: #A0A0A0;"
            );
        }
        
        if (topUpButton != null) {
            topUpButton.setOnAction(this::handleTopUpAction);
        }
        
        if (cancelButton != null) {
            cancelButton.setOnAction(this::handleCancelAction);
        }
        
        // Initial validation
        validateInput();
    }
    
    // Update portfolio information display
    private void updatePortfolioInfo() {
        String selectedPortfolioName = portfolioComboBox.getSelectionModel().getSelectedItem();
        if (selectedPortfolioName == null) return;
        
        Portfolio selectedPortfolio = portfolioList.stream()
                .filter(p -> p.getName().equals(selectedPortfolioName))
                .findFirst()
                .orElse(null);
          if (selectedPortfolio != null && currentCashLabel != null) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            double currentCash = selectedPortfolio.getCashBalance();
            currentCashLabel.setText("Current Cash Balance: " + formatter.format(currentCash));
            
            // Show the portfolio info section
            if (portfolioInfoSection != null) {
                portfolioInfoSection.setVisible(true);
            }
            
            // Calculate new cash balance if amount is valid
            if (newCashLabel != null) {
                try {
                    String amountText = amountField.getText().trim();
                    if (!amountText.isEmpty()) {
                        double amount = Double.parseDouble(amountText);
                        double newCash = currentCash + amount;
                        newCashLabel.setText("New Cash Balance: " + formatter.format(newCash));
                    } else {
                        newCashLabel.setText("New Cash Balance: " + formatter.format(currentCash));
                    }
                } catch (NumberFormatException e) {
                    newCashLabel.setText("New Cash Balance: " + formatter.format(currentCash));
                }
            }
        }
    }
    
    // Validate input and update button state
    private void validateInput() {
        boolean isValid = false;
        
        if (amountField != null && portfolioComboBox != null && topUpButton != null) {
            String amountText = amountField.getText().trim();
            String selectedPortfolio = portfolioComboBox.getSelectionModel().getSelectedItem();
            
            try {
                if (!amountText.isEmpty() && selectedPortfolio != null) {
                    double amount = Double.parseDouble(amountText);
                    isValid = amount > 0 && amount <= 1000000; // Max limit for safety
                }
            } catch (NumberFormatException e) {
                isValid = false;
            }
        }
        
        if (topUpButton != null) {
            topUpButton.setDisable(!isValid);
        }
    }
    
    // Handle top-up action
    @FXML
    private void handleTopUpAction(ActionEvent event) {
        try {
            String selectedPortfolioName = portfolioComboBox.getSelectionModel().getSelectedItem();
            String amountText = amountField.getText().trim();
            String description = descriptionField.getText().trim();
            
            if (selectedPortfolioName == null || amountText.isEmpty()) {
                showAlert(AlertType.WARNING, "Invalid Input", "Please select a portfolio and enter an amount.");
                return;
            }
            
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert(AlertType.WARNING, "Invalid Amount", "Please enter a positive amount.");
                return;
            }
            
            // Find the selected portfolio
            Portfolio selectedPortfolio = portfolioList.stream()
                    .filter(p -> p.getName().equals(selectedPortfolioName))
                    .findFirst()
                    .orElse(null);
            
            if (selectedPortfolio == null) {
                showAlert(AlertType.ERROR, "Portfolio Error", "Selected portfolio not found.");
                return;
            }
            
            // Update portfolio cash balance
            double newBalance = selectedPortfolio.getCashBalance() + amount;
            selectedPortfolio.setCashBalance(newBalance);
              // Create transaction record
            Transaction transaction = new Transaction(
                    UUID.randomUUID().toString(),
                    "Deposit",
                    description.isEmpty() ? "Funds top-up" : description,
                    amount,
                    selectedPortfolio.getName(),
                    LocalDateTime.now(),
                    "Completed"
            );
            
            // In a real application, you would save the transaction to database here
            System.out.println("Transaction created: " + transaction.toString());
            
            // Show success message
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            String message = String.format("Successfully added %s to %s.\nNew balance: %s",
                    formatter.format(amount),
                    selectedPortfolioName,
                    formatter.format(newBalance));
            
            showAlert(AlertType.INFORMATION, "Success", message);
            
            // Close dialog
            topUpStage.close();
            
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Invalid Amount", "Please enter a valid numeric amount.");
        } catch (Exception e) {
            System.err.println("Error processing top-up: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred while processing the top-up. Please try again.");
        }
    }
    
    // Handle cancel action
    @FXML
    private void handleCancelAction(ActionEvent event) {
        if (topUpStage != null) {
            topUpStage.close();
        }
    }
    
    // Helper method to show alerts
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private HBox createTopRow() {
        HBox row = new HBox(20); // 20px spacing between cards
        
        // Create the four cards
        VBox card1 = createInfoCard("Total Portfolio Value", "$0.00", "+0.00% from last month", "#4CAF50");
        VBox card2 = createInfoCard("Average Return", "+0.00%", "Across all portfolios", null);
        VBox card3 = createInfoCard("Active Products", "0", "Different investment products", null);
        VBox card4 = createInfoCard("Recent Transactions", "0", "In the last 30 days", null);
        
        // Set equal growth for all cards
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);
        
        // Add cards to row
        row.getChildren().addAll(card1, card2, card3, card4);
        
        return row;
    }
    
    private HBox createMiddleRow() {
        HBox row = new HBox(20);
        
        // Create two larger chart cards
        VBox chartCard1 = createChartCard("Portfolio Performance", 
                                        "Track how your portfolios have performed over time", 
                                        "No performance data available");
        
        VBox chartCard2 = createChartCard("Asset Allocation", 
                                        "Distribution of your investments by asset type", 
                                        "No asset allocation data available");
        
        // Set equal growth for both cards
        HBox.setHgrow(chartCard1, Priority.ALWAYS);
        HBox.setHgrow(chartCard2, Priority.ALWAYS);
        
        // Add cards to row
        row.getChildren().addAll(chartCard1, chartCard2);
        
        return row;
    }
    
    private HBox createBottomRow() {
        HBox row = new HBox(20);
        
        // Create two larger info cards
        VBox infoCard1 = createChartCard("Recent Transactions", 
                                        null,
                                        "No recent transactions found");
        
        VBox infoCard2 = createChartCard("Top Performing Products", 
                                        null, 
                                        "No products data available");
        
        // Set equal growth for both cards
        HBox.setHgrow(infoCard1, Priority.ALWAYS);
        HBox.setHgrow(infoCard2, Priority.ALWAYS);
        
        // Add cards to row
        row.getChildren().addAll(infoCard1, infoCard2);
        
        return row;
    }
    
    private VBox createInfoCard(String title, String value, String subtitle, String subtitleColor) {
        VBox card = new VBox(5); // 5px spacing between elements
        card.setStyle("-fx-background-color: #2A2A3A; -fx-background-radius: 8;");
        card.setPadding(new Insets(20));
        card.setMinWidth(200);
        
        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 13px;");
        
        // Value label
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Subtitle label
        Label subtitleLabel = new Label(subtitle);
        if (subtitleColor != null) {
            subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        } else {
            subtitleLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 12px;");
        }
        
        // Add all to card
        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        
        return card;
    }
    
    private VBox createChartCard(String title, String subtitle, String emptyMessage) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #2A2A3A; -fx-background-radius: 8;");
        card.setPadding(new Insets(20));
        card.setMinHeight(200);
        
        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Add subtitle if provided
        if (subtitle != null) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 13px;");
            card.getChildren().add(subtitleLabel);
        }
        
        // Create empty state container
        VBox emptyState = new VBox();
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setStyle("-fx-border-color: #404040; -fx-border-radius: 5; -fx-padding: 10;");
        emptyState.setPrefHeight(120);
        VBox.setVgrow(emptyState, Priority.ALWAYS);
        
        // Empty state message
        Label emptyLabel = new Label(emptyMessage);
        emptyLabel.setStyle("-fx-text-fill: #707070; -fx-font-size: 14px;");
        emptyState.getChildren().add(emptyLabel);
        
        // Add components to card
        card.getChildren().addAll(titleLabel, emptyState);
        
        return card;
    }
} 