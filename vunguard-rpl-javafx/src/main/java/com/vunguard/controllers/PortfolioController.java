package com.vunguard.controllers;

import com.vunguard.models.Portfolio;
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
import java.text.NumberFormat;
import java.util.Locale;

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
        // Increase the width of the actions column to fit both buttons
        actionsColumn.setPrefWidth(150);
        actionsColumn.setMinWidth(150);
        
        actionsColumn.setCellFactory(new Callback<TableColumn<Portfolio, Void>, TableCell<Portfolio, Void>>() {
            @Override
            public TableCell<Portfolio, Void> call(TableColumn<Portfolio, Void> param) {
                return new TableCell<Portfolio, Void>() {
                    private final Button viewButton = new Button("View");
                    private final Button editButton = new Button("Edit");
                    
                    {
                        // Configure view button
                        viewButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                        viewButton.setPrefWidth(55);
                        viewButton.setPrefHeight(25);
                        viewButton.setOnAction(event -> {
                            Portfolio portfolio = getTableView().getItems().get(getIndex());
                            System.out.println("View button clicked for: " + portfolio.getName());
                            // Handle view action
                        });
                        
                        // Configure edit button
                        editButton.setStyle("-fx-background-color: #60A5FA; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                        editButton.setPrefWidth(55);
                        editButton.setPrefHeight(25);
                        editButton.setOnAction(event -> {
                            Portfolio portfolio = getTableView().getItems().get(getIndex());
                            System.out.println("Edit button clicked for: " + portfolio.getName());
                            // Handle edit action
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(8); // 8 is the spacing
                            buttons.setAlignment(Pos.CENTER);
                            buttons.getChildren().addAll(viewButton, editButton);
                            // Add padding around the container
                            buttons.setPadding(new Insets(2, 0, 2, 0));
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }
    
    private void showCreatePortfolioDialog() {
        // This would show a dialog to create a new portfolio
        // For now we'll just add a sample portfolio to demonstrate the table
        Portfolio newPortfolio = new Portfolio(
            "Sample Portfolio",
            10000.0,
            5000.0,
            5.75,
            10
        );
        
        portfolioList.add(newPortfolio);
    }
} 