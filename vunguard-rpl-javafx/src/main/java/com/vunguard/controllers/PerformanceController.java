package com.vunguard.controllers;

import com.vunguard.models.Portfolio;
import com.vunguard.models.Transaction;
import com.vunguard.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class PerformanceController {

    @FXML
    private VBox contentArea;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab overviewTab;

    @FXML
    private Tab benchmarkTab;

    @FXML
    private Tab assetAllocationTab;

    @FXML
    private ComboBox<String> timeRangeFilter;

    @FXML
    private Button generateReportButton;

    // Overview Tab content
    @FXML
    private VBox overviewContent;

    @FXML
    private VBox performanceChartContainer;

    @FXML
    private VBox returnsChartContainer;

    // Benchmark Tab content
    @FXML
    private VBox benchmarkContent;

    @FXML
    private VBox benchmarkTableContainer;

    // Asset Allocation Tab content
    @FXML
    private VBox allocationContent;

    @FXML
    private VBox assetAllocationChartContainer;

    @FXML
    private VBox riskAllocationChartContainer;

    @FXML
    private SidebarController sidebarViewController;

    // Data for demo
    private final ObservableList<BenchmarkData> benchmarkDataList = FXCollections.observableArrayList();

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

        // Setup filters
        setupFilters();

        // Setup buttons
        setupButtons();

        // Initialize tab content
        setupTabContent();

        // Load sample data
        loadSampleData();
    }

    private void setupFilters() {
        // Time range options
        timeRangeFilter.getItems().addAll("1 Week", "1 Month", "3 Months", "6 Months", "1 Year", "All Time");
        timeRangeFilter.setValue("All Time");

        timeRangeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Time range changed to: " + newVal);
            refreshData();
        });
    }

    private void setupButtons() {
        generateReportButton.setOnAction(this::handleGenerateReport);
    }

    private void setupTabContent() {
        // Setup Overview tab
        setupOverviewTab();

        // Setup Benchmark Comparison tab
        setupBenchmarkTab();

        // Setup Asset Allocation tab
        setupAssetAllocationTab();
    }

    private void setupOverviewTab() {
        // Performance Over Time chart placeholder
        Label performanceChartLabel = new Label("📈 Performance Over Time Chart");
        performanceChartLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #A0A0A0; -fx-alignment: center;");
        
        Label performanceSubLabel = new Label("Portfolio Value vs Benchmark Value over time");
        performanceSubLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #707070; -fx-alignment: center;");
        
        VBox performancePlaceholder = new VBox(10);
        performancePlaceholder.setAlignment(Pos.CENTER);
        performancePlaceholder.getChildren().addAll(performanceChartLabel, performanceSubLabel);
        
        performanceChartContainer.getChildren().clear();
        performanceChartContainer.getChildren().add(performancePlaceholder);

        // Portfolio Returns chart placeholder
        Label returnsChartLabel = new Label("📊 Portfolio Returns Chart");
        returnsChartLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #A0A0A0; -fx-alignment: center;");
        
        Label returnsSubLabel = new Label("Return (%) vs Benchmark (%) comparison");
        returnsSubLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #707070; -fx-alignment: center;");
        
        VBox returnsPlaceholder = new VBox(10);
        returnsPlaceholder.setAlignment(Pos.CENTER);
        returnsPlaceholder.getChildren().addAll(returnsChartLabel, returnsSubLabel);
        
        returnsChartContainer.getChildren().clear();
        returnsChartContainer.getChildren().add(returnsPlaceholder);
    }

    private void setupBenchmarkTab() {
        // Create benchmark comparison table
        TableView<BenchmarkData> benchmarkTable = new TableView<>();
        benchmarkTable.setItems(benchmarkDataList);
        
        // Style the table
        benchmarkTable.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        benchmarkTable.setPrefHeight(400);

        // Create columns
        TableColumn<BenchmarkData, String> portfolioColumn = new TableColumn<>("Portfolio");
        portfolioColumn.setCellValueFactory(new PropertyValueFactory<>("portfolioName"));
        portfolioColumn.setPrefWidth(150);
        portfolioColumn.setStyle("-fx-text-fill: white;");

        TableColumn<BenchmarkData, String> currentValueColumn = new TableColumn<>("Current Value");
        currentValueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        currentValueColumn.setPrefWidth(130);
        currentValueColumn.setStyle("-fx-text-fill: white;");

        TableColumn<BenchmarkData, String> initialValueColumn = new TableColumn<>("Initial Value");
        initialValueColumn.setCellValueFactory(new PropertyValueFactory<>("initialValue"));
        initialValueColumn.setPrefWidth(130);
        initialValueColumn.setStyle("-fx-text-fill: white;");

        TableColumn<BenchmarkData, String> returnColumn = new TableColumn<>("Return");
        returnColumn.setCellValueFactory(new PropertyValueFactory<>("returnPercent"));
        returnColumn.setPrefWidth(100);
        returnColumn.setStyle("-fx-text-fill: white;");

        TableColumn<BenchmarkData, String> benchmarkColumn = new TableColumn<>("Benchmark");
        benchmarkColumn.setCellValueFactory(new PropertyValueFactory<>("benchmarkPercent"));
        benchmarkColumn.setPrefWidth(100);
        benchmarkColumn.setStyle("-fx-text-fill: white;");

        TableColumn<BenchmarkData, String> differenceColumn = new TableColumn<>("Difference");
        differenceColumn.setCellValueFactory(new PropertyValueFactory<>("difference"));
        differenceColumn.setPrefWidth(100);
        differenceColumn.setStyle("-fx-text-fill: white;");

        benchmarkTable.getColumns().addAll(
            portfolioColumn, currentValueColumn, initialValueColumn, 
            returnColumn, benchmarkColumn, differenceColumn
        );

        // Style table rows
        benchmarkTable.setRowFactory(tv -> {
            TableRow<BenchmarkData> row = new TableRow<>();
            row.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        return row;
        });

        benchmarkTableContainer.getChildren().clear();
        benchmarkTableContainer.getChildren().add(benchmarkTable);
    }

    private void setupAssetAllocationTab() {
        // Current Asset Allocation pie chart placeholder
        Label assetChartLabel = new Label("🥧 Asset Allocation Pie Chart");
        assetChartLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #A0A0A0; -fx-alignment: center;");
        
        Label assetSubLabel = new Label("Distribution across different asset categories");
        assetSubLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #707070; -fx-alignment: center;");
        
        VBox assetPlaceholder = new VBox(10);
        assetPlaceholder.setAlignment(Pos.CENTER);
        assetPlaceholder.getChildren().addAll(assetChartLabel, assetSubLabel);
        
        assetAllocationChartContainer.getChildren().clear();
        assetAllocationChartContainer.getChildren().add(assetPlaceholder);

        // Risk Level allocation pie chart placeholder
        Label riskChartLabel = new Label("⚖️ Risk Level Allocation Chart");
        riskChartLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #A0A0A0; -fx-alignment: center;");
        
        Label riskSubLabel = new Label("Distribution across different risk levels");
        riskSubLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #707070; -fx-alignment: center;");
        
        VBox riskPlaceholder = new VBox(10);
        riskPlaceholder.setAlignment(Pos.CENTER);
        riskPlaceholder.getChildren().addAll(riskChartLabel, riskSubLabel);
        
        riskAllocationChartContainer.getChildren().clear();
        riskAllocationChartContainer.getChildren().add(riskPlaceholder);
    }

    private void loadSampleData() {
        benchmarkDataList.clear();
        
        benchmarkDataList.addAll(
            new BenchmarkData("Conservative Growth", "$15,250", "$12,000", "+27.1%", "+18.5%", "+8.6%"),
            new BenchmarkData("Tech Aggressive", "$8,750", "$8,000", "+9.4%", "+12.2%", "-2.8%"),
            new BenchmarkData("Balanced Fund", "$22,100", "$18,500", "+19.5%", "+15.3%", "+4.2%")
        );
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        System.out.println("Generate Report button clicked");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Generate Report");
        alert.setHeaderText("Performance Report");
        alert.setContentText("Performance report generation is not yet implemented.\n\nThis feature will generate comprehensive reports including:\n- Portfolio performance analysis\n- Benchmark comparisons\n- Asset allocation breakdowns\n- Risk assessments");
        alert.showAndWait();
    }

    private void refreshData() {
        System.out.println("Refreshing performance data for time range: " + timeRangeFilter.getValue());
        // In a real implementation, this would fetch new data based on the time range
        // For now, we'll just show a message
        loadSampleData();
    }



    // Data class for benchmark comparison table
    public static class BenchmarkData {
        private String portfolioName;
        private String currentValue;
        private String initialValue;
        private String returnPercent;
        private String benchmarkPercent;
        private String difference;

        public BenchmarkData(String portfolioName, String currentValue, String initialValue,
                           String returnPercent, String benchmarkPercent, String difference) {
            this.portfolioName = portfolioName;
            this.currentValue = currentValue;
            this.initialValue = initialValue;
            this.returnPercent = returnPercent;
            this.benchmarkPercent = benchmarkPercent;
            this.difference = difference;
        }

        // Getters
        public String getPortfolioName() { return portfolioName; }
        public String getCurrentValue() { return currentValue; }
        public String getInitialValue() { return initialValue; }
        public String getReturnPercent() { return returnPercent; }
        public String getBenchmarkPercent() { return benchmarkPercent; }
        public String getDifference() { return difference; }

        // Setters
        public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }
        public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }
        public void setInitialValue(String initialValue) { this.initialValue = initialValue; }
        public void setReturnPercent(String returnPercent) { this.returnPercent = returnPercent; }
        public void setBenchmarkPercent(String benchmarkPercent) { this.benchmarkPercent = benchmarkPercent; }
        public void setDifference(String difference) { this.difference = difference; }
    }
}