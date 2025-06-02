package com.vunguard.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;

public class DashboardController {

    @FXML
    private VBox contentArea; // This will be referenced from the FXML
    
    @FXML
    private ScrollPane scrollPane; // Reference to the ScrollPane

    @FXML
    private SidebarController sidebarViewController;

    @FXML
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
        
        // Create dashboard content programmatically
        setupDashboard();
    }
    
    private void setupDashboard() {
        // Check if contentArea is properly injected
        if (contentArea == null) {
            System.err.println("contentArea is null! FXML mapping issue.");
            return;
        }
        
        // Create button for "Top Up Funds"
        Button topUpButton = new Button("Top Up Funds");
        topUpButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15;");
        
        // Add click event handler for "Top Up Funds" button
        topUpButton.setOnAction(event -> {
            System.out.println("Top Up Funds button clicked");
            showTopUpDialog();
        });
        
        // Add button to right side of header
        HBox headerActions = new HBox();
        headerActions.setAlignment(Pos.CENTER_RIGHT);
        headerActions.getChildren().add(topUpButton);
        
        // Push it to the right of existing header content
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Create header container
        HBox headerContainer = new HBox(10);
        headerContainer.getChildren().add(spacer);
        headerContainer.getChildren().add(headerActions);
        headerContainer.setPadding(new Insets(0, 0, 10, 0)); // Add some padding at the bottom
        
        // Add header actions after the existing labels
        contentArea.getChildren().add(2, headerContainer);
        
        // Create all rows
        HBox topRow = createTopRow();
        HBox middleRow = createMiddleRow();
        HBox bottomRow = createBottomRow();
        
        // Add spacing between rows
        VBox.setMargin(topRow, new Insets(10, 0, 20, 0));
        VBox.setMargin(middleRow, new Insets(0, 0, 20, 0));
        
        // Add rows to content area
        contentArea.getChildren().addAll(topRow, middleRow, bottomRow);
    }
    
    // Method to show the top-up dialog
    private void showTopUpDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Top Up Funds");
        alert.setHeaderText("Top Up Your Account");
        alert.setContentText("This feature is coming soon. You will be able to add funds to your account here.");
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