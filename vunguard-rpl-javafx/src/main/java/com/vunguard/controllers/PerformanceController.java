package com.vunguard.controllers;

import com.vunguard.dao.PerformanceDAO;
import com.vunguard.models.Portfolio;
import com.vunguard.models.Transaction;
import com.vunguard.models.User;
import com.vunguard.services.AuthService;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
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
        this.performanceDAO = new PerformanceDAO();
        this.currentUser = AuthService.getCurrentUser();

        if (sidebarViewController != null) {
            try {
                sidebarViewController.setActiveButton(sidebarViewController.getPerformanceButton());
            } catch (Exception e) {
                System.err.println("Error setting active button: " + e.getMessage());
            }
        }

        if (currentUser == null) {
            contentArea.getChildren().setAll(new Label("Please log in to view performance data."));
            return;
        }

        // Setup filters
        setupFilters();

        // Setup buttons
        setupButtons();

        // Initialize tab content
        setupTabContent();
    }

    private void setupFilters() {
        if (timeRangeFilter != null) {
            timeRangeFilter.getItems().addAll("1 Week", "1 Month", "3 Months", "6 Months", "1 Year", "All Time");
            timeRangeFilter.setValue("All Time");
            // NOTE: Logic untuk memfilter data berdasarkan waktu belum diimplementasikan di DAO.
            // Listener ini bisa ditambahkan nanti saat DAO mendukungnya.
            timeRangeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
                System.out.println("Time range changed to: " + newVal + ". Data refresh logic to be implemented.");
            });
        }
    }

    private void setupButtons() {
        if (generateReportButton != null) {
            generateReportButton.setOnAction(this::handleGenerateReport);
        }
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
        performanceChartContainer.getChildren().clear();
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Total Value (USD)");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Portfolio Value Over Time");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Portfolio Value");
        series.setData(performanceDAO.getPerformanceOverTime(currentUser.getId()));
        
        lineChart.getData().add(series);
        performanceChartContainer.getChildren().add(lineChart);
        
        // Placeholder untuk returns chart (dapat dikembangkan lebih lanjut)
        returnsChartContainer.getChildren().setAll(new Label("Portfolio Returns Chart (Placeholder)"));
    }

    private void setupBenchmarkTab() {
        benchmarkTableContainer.getChildren().clear();
        
        TableView<PerformanceDAO.BenchmarkData> benchmarkTable = new TableView<>();
        
        TableColumn<PerformanceDAO.BenchmarkData, String> portfolioCol = new TableColumn<>("Portfolio");
        portfolioCol.setCellValueFactory(new PropertyValueFactory<>("portfolioName"));

        TableColumn<PerformanceDAO.BenchmarkData, Double> currentValueCol = new TableColumn<>("Current Value");
        currentValueCol.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        formatCurrencyColumn(currentValueCol);

        TableColumn<PerformanceDAO.BenchmarkData, Double> returnCol = new TableColumn<>("Return");
        returnCol.setCellValueFactory(new PropertyValueFactory<>("returnPercent"));
        formatPercentageColumn(returnCol, false);

        TableColumn<PerformanceDAO.BenchmarkData, Double> benchmarkCol = new TableColumn<>("Benchmark");
        benchmarkCol.setCellValueFactory(new PropertyValueFactory<>("benchmarkPercent"));
        formatPercentageColumn(benchmarkCol, false);

        TableColumn<PerformanceDAO.BenchmarkData, Double> diffCol = new TableColumn<>("Difference");
        diffCol.setCellValueFactory(new PropertyValueFactory<>("difference"));
        formatPercentageColumn(diffCol, true);

        benchmarkTable.getColumns().setAll(portfolioCol, currentValueCol, returnCol, benchmarkCol, diffCol);
        benchmarkTable.setItems(performanceDAO.getPortfolioPerformances(currentUser.getId()));
        
        benchmarkTableContainer.getChildren().add(benchmarkTable);
    }

   private void setupAssetAllocationTab() {
        assetAllocationChartContainer.getChildren().clear();
        riskAllocationChartContainer.getChildren().clear();
        
        // Asset Allocation Pie Chart
        PieChart assetChart = new PieChart(performanceDAO.getAssetAllocation(currentUser.getId()));
        assetChart.setTitle("Asset Allocation");
        assetChart.setLegendSide(Side.LEFT);
        assetAllocationChartContainer.getChildren().add(assetChart);

        // Risk Allocation Pie Chart
        PieChart riskChart = new PieChart(performanceDAO.getRiskAllocation(currentUser.getId()));
        riskChart.setTitle("Risk Allocation");
        riskChart.setLegendSide(Side.LEFT);
        riskAllocationChartContainer.getChildren().add(riskChart);
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        System.out.println("Generate Report button clicked");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Generate Report");
        alert.setHeaderText("Report Generation");
        alert.setContentText("This feature will generate a comprehensive performance report in a future update.");
        alert.showAndWait();
    }

    private void formatCurrencyColumn(TableColumn<PerformanceDAO.BenchmarkData, Double> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : NumberFormat.getCurrencyInstance(Locale.US).format(item));
            }
        });
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

        public BenchmarkData(String portfolioName, String currentValue, String initialValue, String returnPercent, String benchmarkPercent, String difference) {
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

    private void formatPercentageColumn(TableColumn<PerformanceDAO.BenchmarkData, Double> column, boolean showSignForPositive) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String format = showSignForPositive ? "%+.2f%%" : "%.2f%%";
                    setText(String.format(format, item));
                    setStyle(item >= 0 ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #F44336;");
                }
            }
        });
    }
}