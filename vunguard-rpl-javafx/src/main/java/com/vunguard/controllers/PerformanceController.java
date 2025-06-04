package com.vunguard.controllers;

import com.vunguard.models.Portfolio;
import com.vunguard.models.Transaction;
import com.vunguard.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class PerformanceController {

    @FXML
    private VBox contentArea;

    @FXML
    private Button refreshButton;

    @FXML
    private ComboBox<String> timeRangeFilter;

    @FXML
    private ComboBox<String> portfolioFilter;

    @FXML
    private VBox summarySection;

    @FXML
    private VBox chartsSection;

    @FXML
    private VBox detailsSection;

    @FXML
    private SidebarController sidebarViewController;

    // Data containers using existing models
    private List<Portfolio> portfolios = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private User currentUser;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    private void initialize() {
        System.out.println("PerformanceController initialized");

        if (sidebarViewController != null) {
            System.out.println("SidebarView Controller injected into PerformanceController");
            try {
                sidebarViewController.setActiveButton(sidebarViewController.getPerformanceButton());
            } catch (Exception e) {
                System.err.println("Error setting active button: " + e.getMessage());
            }
        } else {
            System.out.println("SidebarView Controller NOT injected.");
        }

        // Initialize empty data
        initializeEmptyData();

        // Setup filters
        setupFilters();

        // Setup button events
        refreshButton.setOnAction(event -> {
            System.out.println("Refresh Data button clicked");
            refreshPerformanceData();
        });

        // Build the performance dashboard
        buildPerformanceDashboard();
    }

    private void initializeEmptyData() {
        // Clear all data containers
        portfolios.clear();
        transactions.clear();
        currentUser = null;
    }

    private void setupFilters() {
        // Time range options
        timeRangeFilter.getItems().addAll("1 Day", "1 Week", "1 Month", "3 Months", "6 Months", "1 Year", "All Time");
        timeRangeFilter.setValue("1 Month");

        // Portfolio options - start with default
        portfolioFilter.getItems().addAll("All Portfolios");
        portfolioFilter.setValue("All Portfolios");

        // Add portfolio names to filter when portfolios are loaded
        updatePortfolioFilter();

        // Add listeners for filter changes
        timeRangeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Time range changed to: " + newVal);
            updatePerformanceData();
        });

        portfolioFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Portfolio filter changed to: " + newVal);
            updatePerformanceData();
        });
    }

    private void updatePortfolioFilter() {
        // Clear and rebuild portfolio filter options
        String currentSelection = portfolioFilter.getValue();
        portfolioFilter.getItems().clear();
        portfolioFilter.getItems().add("All Portfolios");
        
        for (Portfolio portfolio : portfolios) {
            portfolioFilter.getItems().add(portfolio.getName());
        }
        
        // Restore selection if still valid
        if (portfolioFilter.getItems().contains(currentSelection)) {
            portfolioFilter.setValue(currentSelection);
        } else {
            portfolioFilter.setValue("All Portfolios");
        }
    }

    private void buildPerformanceDashboard() {
        buildSummaryCards();
        buildChartsSection();
        buildDetailsSection();
    }

    private void buildSummaryCards() {
        summarySection.getChildren().clear();

        Label summaryTitle = new Label("Performance Overview");
        summaryTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
        summarySection.getChildren().add(summaryTitle);

        // Calculate performance metrics from portfolios
        PerformanceMetrics metrics = calculatePerformanceMetrics();

        // Create first row of cards
        HBox topRow = new HBox(20);
        VBox totalReturn = createPerformanceCard("Total Return", 
                                               metrics.totalReturnValue, 
                                               metrics.totalReturnPercent, 
                                               getReturnColor(metrics.totalReturnValue));
        VBox avgReturn = createPerformanceCard("Average Return", 
                                             metrics.averageReturn, 
                                             "Annualized", 
                                             getReturnColor(metrics.averageReturn));
        VBox bestPerformer = createPerformanceCard("Best Performer", 
                                                 metrics.bestPerformerName, 
                                                 metrics.bestPerformerReturn, 
                                                 getReturnColor(metrics.bestPerformerReturn));
        VBox portfolioCount = createPerformanceCard("Total Portfolios", 
                                                  String.valueOf(portfolios.size()), 
                                                  "Active portfolios", 
                                                  null);

        setEqualGrowth(topRow, totalReturn, avgReturn, bestPerformer, portfolioCount);
        summarySection.getChildren().add(topRow);

        // Create second row of cards
        HBox bottomRow = new HBox(20);
        VBox totalValue = createPerformanceCard("Total Value", 
                                              metrics.totalPortfolioValue, 
                                              "All portfolios", 
                                              null);
        VBox totalAssets = createPerformanceCard("Total Assets", 
                                               metrics.totalAssetValue, 
                                               "Asset value only", 
                                               null);
        VBox totalCash = createPerformanceCard("Total Cash", 
                                             metrics.totalCashBalance, 
                                             "Available cash", 
                                             null);
        VBox assetCount = createPerformanceCard("Total Assets Count", 
                                              String.valueOf(metrics.totalAssetCount), 
                                              "Across all portfolios", 
                                              null);

        setEqualGrowth(bottomRow, totalValue, totalAssets, totalCash, assetCount);
        
        VBox.setMargin(bottomRow, new Insets(10, 0, 0, 0));
        summarySection.getChildren().add(bottomRow);
    }

    private void buildChartsSection() {
        chartsSection.getChildren().clear();

        Label chartsTitle = new Label("Performance Charts");
        chartsTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
        chartsSection.getChildren().add(chartsTitle);

        HBox chartsRow = new HBox(20);
        
        VBox performanceChart = createChartCard("Portfolio Performance Over Time", 
                                               "Track your portfolio value and returns", 
                                               portfolios.isEmpty() ? "No portfolio data available" : 
                                               "Performance chart for " + portfolios.size() + " portfolios");
        
        VBox allocationChart = createChartCard("Asset Allocation", 
                                              "Current distribution of your investments", 
                                              portfolios.isEmpty() ? "No allocation data available" :
                                              "Asset allocation across " + portfolios.size() + " portfolios");

        setEqualGrowth(chartsRow, performanceChart, allocationChart);
        chartsSection.getChildren().add(chartsRow);

        HBox chartsRow2 = new HBox(20);
        
        VBox returnsChart = createChartCard("Transaction History", 
                                           "Recent transaction breakdown", 
                                           transactions.isEmpty() ? "No transaction data available" :
                                           transactions.size() + " transactions recorded");
        
        VBox riskChart = createChartCard("Portfolio Risk Analysis", 
                                        "Risk-return profile of your portfolios", 
                                        portfolios.isEmpty() ? "No risk analysis data available" :
                                        "Risk analysis for " + portfolios.size() + " portfolios");

        setEqualGrowth(chartsRow2, returnsChart, riskChart);
        
        VBox.setMargin(chartsRow2, new Insets(10, 0, 0, 0));
        chartsSection.getChildren().add(chartsRow2);
    }

    private void buildDetailsSection() {
        detailsSection.getChildren().clear();

        Label detailsTitle = new Label("Detailed Performance Metrics");
        detailsTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
        detailsSection.getChildren().add(detailsTitle);

        HBox detailsRow = new HBox(20);
        
        VBox portfolioComparison = createDetailCard("Portfolio Comparison", 
                                                   createPortfolioComparisonContent());
        
        VBox recentTransactions = createDetailCard("Recent Transactions", 
                                                 createRecentTransactionsContent());

        setEqualGrowth(detailsRow, portfolioComparison, recentTransactions);
        detailsSection.getChildren().add(detailsRow);
    }

    private VBox createPortfolioComparisonContent() {
        VBox content = new VBox(10);

        if (portfolios.isEmpty()) {
            Label emptyLabel = new Label("No portfolio data available");
            emptyLabel.setStyle("-fx-text-fill: #707070; -fx-font-size: 14px; -fx-padding: 20; -fx-alignment: center;");
            content.getChildren().add(emptyLabel);
        } else {
            for (Portfolio portfolio : portfolios) {
                HBox row = createPortfolioRow(portfolio);
                content.getChildren().add(row);
            }
        }

        return content;
    }

    private VBox createRecentTransactionsContent() {
        VBox content = new VBox(10);

        if (transactions.isEmpty()) {
            Label emptyLabel = new Label("No transaction data available");
            emptyLabel.setStyle("-fx-text-fill: #707070; -fx-font-size: 14px; -fx-padding: 20; -fx-alignment: center;");
            content.getChildren().add(emptyLabel);
        } else {
            // Show last 5 transactions
            int count = Math.min(5, transactions.size());
            for (int i = 0; i < count; i++) {
                Transaction transaction = transactions.get(i);
                HBox row = createTransactionRow(transaction);
                content.getChildren().add(row);
            }
        }

        return content;
    }

    private HBox createPortfolioRow(Portfolio portfolio) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8; -fx-background-color: #353545; -fx-background-radius: 4;");

        Label nameLabel = new Label(portfolio.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        nameLabel.setPrefWidth(150);

        Label returnLabel = new Label(String.format("%.2f%%", portfolio.getReturnPercentage()));
        String returnColor = portfolio.getReturnPercentage() >= 0 ? "#4ADE80" : "#F87171";
        returnLabel.setStyle("-fx-text-fill: " + returnColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        returnLabel.setPrefWidth(80);

        Label valueLabel = new Label(currencyFormat.format(portfolio.getTotalValue()));
        valueLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 13px;");
        valueLabel.setPrefWidth(100);

        Label assetsLabel = new Label(portfolio.getNumberOfAssets() + " assets");
        assetsLabel.setStyle("-fx-text-fill: #60A5FA; -fx-font-size: 13px;");

        row.getChildren().addAll(nameLabel, returnLabel, valueLabel, assetsLabel);
        return row;
    }

    private HBox createTransactionRow(Transaction transaction) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8; -fx-background-color: #353545; -fx-background-radius: 4;");

        Label typeLabel = new Label(transaction.getType());
        String typeColor = switch (transaction.getType().toLowerCase()) {
            case "buy", "deposit" -> "#4ADE80";
            case "sell", "withdrawal" -> "#F87171";
            default -> "#60A5FA";
        };
        typeLabel.setStyle("-fx-text-fill: " + typeColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        typeLabel.setPrefWidth(80);

        Label amountLabel = new Label(currencyFormat.format(transaction.getAmount()));
        amountLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        amountLabel.setPrefWidth(100);

        Label portfolioLabel = new Label(transaction.getPortfolio());
        portfolioLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 13px;");
        portfolioLabel.setPrefWidth(120);

        Label dateLabel = new Label(transaction.getDateFormatted());
        dateLabel.setStyle("-fx-text-fill: #707070; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(typeLabel, amountLabel, portfolioLabel, dateLabel, spacer);
        return row;
    }

    private PerformanceMetrics calculatePerformanceMetrics() {
        PerformanceMetrics metrics = new PerformanceMetrics();
        
        if (portfolios.isEmpty()) {
            return metrics; // Returns default empty values
        }

        double totalValue = 0;
        double totalAssets = 0;
        double totalCash = 0;
        double totalReturn = 0;
        int totalAssetCount = 0;
        Portfolio bestPerformer = portfolios.get(0);

        for (Portfolio portfolio : portfolios) {
            totalValue += portfolio.getTotalValue();
            totalAssets += portfolio.getAssetValue();
            totalCash += portfolio.getCashBalance();
            totalReturn += portfolio.getReturnPercentage();
            totalAssetCount += portfolio.getNumberOfAssets();

            if (portfolio.getReturnPercentage() > bestPerformer.getReturnPercentage()) {
                bestPerformer = portfolio;
            }
        }

        metrics.totalPortfolioValue = currencyFormat.format(totalValue);
        metrics.totalAssetValue = currencyFormat.format(totalAssets);
        metrics.totalCashBalance = currencyFormat.format(totalCash);
        metrics.totalAssetCount = totalAssetCount;
        
        double avgReturn = totalReturn / portfolios.size();
        metrics.averageReturn = String.format("%.2f%%", avgReturn);
        
        // Calculate total return value (simplified)
        double returnValue = totalAssets * (avgReturn / 100);
        metrics.totalReturnValue = currencyFormat.format(returnValue);
        metrics.totalReturnPercent = String.format("%.2f%%", avgReturn);
        
        metrics.bestPerformerName = bestPerformer.getName();
        metrics.bestPerformerReturn = String.format("%.2f%%", bestPerformer.getReturnPercentage());

        return metrics;
    }

    // Helper methods
    private VBox createPerformanceCard(String title, String value, String subtitle, String subtitleColor) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #2A2A3A; -fx-background-radius: 8; -fx-padding: 20;");
        card.setMinWidth(200);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 13px;");

        Label valueLabel = new Label(value);
        if (value.equals("--") || value.equals("0")) {
            valueLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #707070; -fx-font-weight: bold;");
        } else {
            valueLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        Label subtitleLabel = new Label(subtitle);
        if (subtitleColor != null) {
            subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            subtitleLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 12px;");
        }

        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return card;
    }

    private VBox createChartCard(String title, String subtitle, String message) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #2A2A3A; -fx-background-radius: 8; -fx-padding: 20;");
        card.setMinHeight(250);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 13px;");

        VBox emptyState = new VBox();
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setStyle("-fx-border-color: #404040; -fx-border-radius: 5; -fx-border-style: dashed; -fx-padding: 20;");
        emptyState.setPrefHeight(180);
        VBox.setVgrow(emptyState, Priority.ALWAYS);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #707070; -fx-font-size: 14px;");
        emptyState.getChildren().add(messageLabel);

        card.getChildren().addAll(titleLabel, subtitleLabel, emptyState);
        return card;
    }

    private VBox createDetailCard(String title, VBox content) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #2A2A3A; -fx-background-radius: 8; -fx-padding: 20;");
        card.setMinHeight(200);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, content);
        return card;
    }

    private void setEqualGrowth(HBox container, VBox... children) {
        for (VBox child : children) {
            HBox.setHgrow(child, Priority.ALWAYS);
        }
        container.getChildren().addAll(children);
    }

    private String getReturnColor(String returnValue) {
        if (returnValue.contains("+")) return "#4ADE80";
        if (returnValue.contains("-")) return "#F87171";
        return null;
    }

    private void refreshPerformanceData() {
        System.out.println("Refreshing performance data...");
        buildPerformanceDashboard();
        showRefreshNotification();
    }

    private void updatePerformanceData() {
        String timeRange = timeRangeFilter.getValue();
        String portfolio = portfolioFilter.getValue();
        System.out.println("Updating performance data for " + timeRange + " range and " + portfolio);
        buildPerformanceDashboard();
    }

    private void showRefreshNotification() {
        Label notification = new Label("✓ Performance data refreshed");
        notification.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                             "-fx-padding: 8 12; -fx-background-radius: 4; -fx-font-size: 12px;");
        
        contentArea.getChildren().add(3, notification);
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    contentArea.getChildren().remove(notification);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Public methods for data insertion using existing models
    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios.clear();
        this.portfolios.addAll(portfolios);
        updatePortfolioFilter();
        buildPerformanceDashboard();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);
        buildPerformanceDashboard();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        buildPerformanceDashboard();
    }

    public void addPortfolio(Portfolio portfolio) {
        this.portfolios.add(portfolio);
        updatePortfolioFilter();
        buildPerformanceDashboard();
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(0, transaction); // Add to beginning for recent transactions
        buildPerformanceDashboard();
    }

    // Helper class for calculated metrics
    private static class PerformanceMetrics {
        String totalReturnValue = "--";
        String totalReturnPercent = "--";
        String averageReturn = "--";
        String bestPerformerName = "--";
        String bestPerformerReturn = "--";
        String totalPortfolioValue = "--";
        String totalAssetValue = "--";
        String totalCashBalance = "--";
        int totalAssetCount = 0;
    }
}