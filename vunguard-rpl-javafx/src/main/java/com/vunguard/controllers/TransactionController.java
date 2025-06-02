package com.vunguard.controllers;

import com.vunguard.models.Transaction;
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
import javafx.collections.transformation.FilteredList;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
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

    // Sample data for transactions
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private FilteredList<Transaction> filteredTransactions;

    @FXML
    private void initialize() {
        System.out.println("TransactionController initialized");

        if (sidebarViewController != null) {
            System.out.println("SidebarView Controller injected into TransactionController");
            // Set the transactions button as active in the sidebar
            try {
                sidebarViewController.setActiveButton(sidebarViewController.getTransactionsButton());
            } catch (Exception e) {
                System.err.println("Error setting active button: " + e.getMessage());
            }
        } else {
            System.out.println("SidebarView Controller NOT injected.");
        }

        // Add sample transactions
        addSampleTransactions();

        // Setup filtered list
        filteredTransactions = new FilteredList<>(transactionList, p -> true);
        
        // Configure table columns
        setupTableColumns();

        // Setup filters
        setupFilters();

        // Setup button events
        newTransactionButton.setOnAction(event -> {
            System.out.println("New Transaction button clicked");
            showNewTransactionDialog();
        });

        // Set the data to the table
        transactionTable.setItems(filteredTransactions);
    }

    private void addSampleTransactions() {
        // Current date and time
        LocalDateTime now = LocalDateTime.now();

        transactionList.addAll(
            new Transaction(
                "TRX-" + UUID.randomUUID().toString().substring(0, 8),
                "Deposit",
                "Initial deposit",
                10000.0,
                "Conservative Portfolio",
                now.minus(30, ChronoUnit.DAYS),
                "Completed"
            ),
            new Transaction(
                "TRX-" + UUID.randomUUID().toString().substring(0, 8),
                "Buy",
                "Purchase of AAPL stock",
                2500.0,
                "Growth Portfolio",
                now.minus(15, ChronoUnit.DAYS),
                "Completed"
            ),
            new Transaction(
                "TRX-" + UUID.randomUUID().toString().substring(0, 8),
                "Sell",
                "Sale of MSFT stock",
                1200.0,
                "Growth Portfolio",
                now.minus(5, ChronoUnit.DAYS),
                "Completed"
            ),
            new Transaction(
                "TRX-" + UUID.randomUUID().toString().substring(0, 8),
                "Dividend",
                "Dividend payment from AAPL",
                50.0,
                "Income Portfolio",
                now.minus(2, ChronoUnit.DAYS),
                "Processing"
            ),
            new Transaction(
                "TRX-" + UUID.randomUUID().toString().substring(0, 8),
                "Withdrawal",
                "Monthly withdrawal",
                500.0,
                "Income Portfolio",
                now.minus(1, ChronoUnit.DAYS),
                "Pending"
            )
        );
    }

    private void setupTableColumns() {
        // Basic columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Type column with custom styling
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
                            setStyle("-fx-text-fill: #4ADE80;"); // Green
                            break;
                        case "Sell":
                            setStyle("-fx-text-fill: #F87171;"); // Red
                            break;
                        case "Deposit":
                            setStyle("-fx-text-fill: #60A5FA;"); // Blue
                            break;
                        case "Withdrawal":
                            setStyle("-fx-text-fill: #FBBF24;"); // Yellow
                            break;
                        case "Dividend":
                            setStyle("-fx-text-fill: #A78BFA;"); // Purple
                            break;
                        default:
                            setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });
        
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
                    String type = getTableView().getItems().get(getIndex()).getType();
                    
                    // Set color based on transaction type
                    if ("Deposit".equals(type) || "Buy".equals(type) || "Withdrawal".equals(type)) {
                        setText(currencyFormat.format(value));
                    } else {
                        setText("+" + currencyFormat.format(value));
                    }
                    
                    if ("Withdrawal".equals(type) || "Buy".equals(type)) {
                        setStyle("-fx-text-fill: #F87171;"); // Red for money spent
                    } else if ("Deposit".equals(type) || "Sell".equals(type) || "Dividend".equals(type)) {
                        setStyle("-fx-text-fill: #4ADE80;"); // Green for money received
                    }
                }
            }
        });
        
        portfolioColumn.setCellValueFactory(new PropertyValueFactory<>("portfolio"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateFormatted"));
        
        // Status column with custom styling
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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
                            setStyle("-fx-text-fill: #4ADE80;"); // Green
                            break;
                        case "Processing":
                            setStyle("-fx-text-fill: #60A5FA;"); // Blue
                            break;
                        case "Pending":
                            setStyle("-fx-text-fill: #FBBF24;"); // Yellow
                            break;
                        case "Failed":
                            setStyle("-fx-text-fill: #F87171;"); // Red
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
        // Set the width of actions column
        actionsColumn.setPrefWidth(150);
        actionsColumn.setMinWidth(150);
        
        actionsColumn.setCellFactory(new Callback<TableColumn<Transaction, Void>, TableCell<Transaction, Void>>() {
            @Override
            public TableCell<Transaction, Void> call(TableColumn<Transaction, Void> param) {
                return new TableCell<Transaction, Void>() {
                    private final Button detailsButton = new Button("Details");
                    private final Button receiptButton = new Button("Receipt");
                    
                    {
                        // Configure details button
                        detailsButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                        detailsButton.setPrefWidth(65);
                        detailsButton.setPrefHeight(25);
                        detailsButton.setOnAction(event -> {
                            Transaction transaction = getTableView().getItems().get(getIndex());
                            System.out.println("Details button clicked for transaction: " + transaction.getId());
                            // Handle view details action
                        });
                        
                        // Configure receipt button
                        receiptButton.setStyle("-fx-background-color: #60A5FA; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                        receiptButton.setPrefWidth(65);
                        receiptButton.setPrefHeight(25);
                        receiptButton.setOnAction(event -> {
                            Transaction transaction = getTableView().getItems().get(getIndex());
                            System.out.println("Receipt button clicked for transaction: " + transaction.getId());
                            // Handle view receipt action
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
                            buttons.getChildren().addAll(detailsButton, receiptButton);
                            buttons.setPadding(new Insets(2, 0, 2, 0));
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }
    
    private void setupFilters() {
        // Add filter items
        typeFilter.getItems().addAll("All Types", "Buy", "Sell", "Deposit", "Withdrawal", "Dividend");
        portfolioFilter.getItems().addAll("All Portfolios", "Conservative Portfolio", "Growth Portfolio", "Income Portfolio");
        statusFilter.getItems().addAll("All Statuses", "Completed", "Processing", "Pending", "Failed");
        
        // Set default values
        typeFilter.setValue("All Types");
        portfolioFilter.setValue("All Portfolios");
        statusFilter.setValue("All Statuses");
        
        // Add filter listeners
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        portfolioFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        // Clear filters button
        clearFiltersButton.setOnAction(event -> {
            typeFilter.setValue("All Types");
            portfolioFilter.setValue("All Portfolios");
            statusFilter.setValue("All Statuses");
            applyFilters();
        });
    }
    
    private void applyFilters() {
        String typeValue = typeFilter.getValue();
        String portfolioValue = portfolioFilter.getValue();
        String statusValue = statusFilter.getValue();
        
        filteredTransactions.setPredicate(transaction -> {
            boolean matchesType = "All Types".equals(typeValue) || typeValue.equals(transaction.getType());
            boolean matchesPortfolio = "All Portfolios".equals(portfolioValue) || portfolioValue.equals(transaction.getPortfolio());
            boolean matchesStatus = "All Statuses".equals(statusValue) || statusValue.equals(transaction.getStatus());
            
            return matchesType && matchesPortfolio && matchesStatus;
        });
    }
    
    private void showNewTransactionDialog() {
        // In a real app, this would open a form to create a new transaction
        // For demo purposes, we'll just add a random new transaction
        
        String[] types = {"Buy", "Sell", "Deposit", "Withdrawal", "Dividend"};
        String[] portfolios = {"Conservative Portfolio", "Growth Portfolio", "Income Portfolio"};
        String[] statuses = {"Completed", "Processing", "Pending"};
        
        String randomType = types[(int)(Math.random() * types.length)];
        String randomPortfolio = portfolios[(int)(Math.random() * portfolios.length)];
        String randomStatus = statuses[(int)(Math.random() * statuses.length)];
        
        double amount = Math.round(Math.random() * 10000 * 100.0) / 100.0;
        
        Transaction newTransaction = new Transaction(
            "TRX-" + UUID.randomUUID().toString().substring(0, 8),
            randomType,
            "Sample " + randomType.toLowerCase() + " transaction",
            amount,
            randomPortfolio,
            LocalDateTime.now(),
            randomStatus
        );
        
        transactionList.add(newTransaction);
        applyFilters(); // Reapply filters to make sure the new transaction shows if it meets criteria
    }
} 