package com.vunguard.controllers;

import com.vunguard.models.Recommendation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Parent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewRecommendationsController {

    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private VBox tableContainer;
    @FXML
    private SidebarController sidebarViewController;

    private TableView<Recommendation> recommendationsTable;
    private TableColumn<Recommendation, String> productColumn;
    private TableColumn<Recommendation, String> analystColumn;
    private TableColumn<Recommendation, String> typeColumn;
    private TableColumn<Recommendation, Double> targetPriceColumn;
    private TableColumn<Recommendation, Double> currentPriceColumn;
    private TableColumn<Recommendation, String> confidenceColumn;
    private TableColumn<Recommendation, String> statusColumn;
    private TableColumn<Recommendation, String> createdColumn;
    private TableColumn<Recommendation, Void> actionsColumn;

    private ObservableList<Recommendation> allRecommendations = FXCollections.observableArrayList();
    private FilteredList<Recommendation> filteredRecommendations;

    @FXML
    private void initialize() {
        System.out.println("ReviewRecommendationsController initialized");
        createTable();
        setupTable();
        setupFilters();
        loadSampleData();
        setupActionButtons();
    }

    private void createTable() {
        // Create table and columns
        recommendationsTable = new TableView<>();
        productColumn = new TableColumn<>("Product");
        analystColumn = new TableColumn<>("Analyst");
        typeColumn = new TableColumn<>("Type");
        targetPriceColumn = new TableColumn<>("Target Price");
        currentPriceColumn = new TableColumn<>("Current Price");
        confidenceColumn = new TableColumn<>("Confidence");
        statusColumn = new TableColumn<>("Status");
        createdColumn = new TableColumn<>("Created");
        actionsColumn = new TableColumn<>("Actions");

        // Set column widths
        productColumn.setPrefWidth(140.0);
        analystColumn.setPrefWidth(120.0);
        typeColumn.setPrefWidth(80.0);
        targetPriceColumn.setPrefWidth(100.0);
        currentPriceColumn.setPrefWidth(100.0);
        confidenceColumn.setPrefWidth(100.0);
        statusColumn.setPrefWidth(110.0);
        createdColumn.setPrefWidth(100.0);
        actionsColumn.setPrefWidth(80.0);
        actionsColumn.setSortable(false);

        // Add columns to table
        recommendationsTable.getColumns().addAll(
            productColumn, analystColumn, typeColumn, targetPriceColumn,
            currentPriceColumn, confidenceColumn, statusColumn, createdColumn, actionsColumn
        );

        // Style the table
        recommendationsTable.setStyle("-fx-background-color: #2A2A3A; -fx-control-inner-background: #2A2A3A;");
        VBox.setVgrow(recommendationsTable, javafx.scene.layout.Priority.ALWAYS);
        
        // Add table to container
        tableContainer.getChildren().add(recommendationsTable);
    }

    private void setupTable() {
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        analystColumn.setCellValueFactory(new PropertyValueFactory<>("analystName"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        targetPriceColumn.setCellValueFactory(new PropertyValueFactory<>("targetPrice"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        confidenceColumn.setCellValueFactory(new PropertyValueFactory<>("confidenceStars"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdTimeAgo"));

        // Custom formatting for price columns
        targetPriceColumn.setCellFactory(col -> new TableCell<Recommendation, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price));
                setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: transparent;");
            }
        });

        currentPriceColumn.setCellFactory(col -> new TableCell<Recommendation, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price));
                setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: transparent;");
            }
        });

        // Color coding for type column
        typeColumn.setCellFactory(col -> new TableCell<Recommendation, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(type);
                    if ("BUY".equals(type)) {
                        setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #22C55E; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-weight: bold;");
                    } else if ("SELL".equals(type)) {
                        setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #EF4444; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Color coding for status column
        statusColumn.setCellFactory(col -> new TableCell<Recommendation, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(status);
                    switch (status) {
                        case "PENDING":
                            setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #F59E0B; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-weight: bold;");
                            break;
                        case "APPROVED":
                            setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #22C55E; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-weight: bold;");
                            break;
                        case "REJECTED":
                            setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #EF4444; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-weight: bold;");
                            break;
                        case "IMPLEMENTED":
                            setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #3B82F6; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
    }

    private void setupFilters() {
        // Populate status filter ComboBox
        statusFilter.setItems(FXCollections.observableArrayList(
            "All Status", "PENDING", "APPROVED", "REJECTED", "IMPLEMENTED"
        ));
        
        filteredRecommendations = new FilteredList<>(allRecommendations);
        recommendationsTable.setItems(filteredRecommendations);
        statusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.getSelectionModel().select("All Status");
    }

    private void applyFilters() {
        String selectedStatus = statusFilter.getSelectionModel().getSelectedItem();
        filteredRecommendations.setPredicate(recommendation -> 
            selectedStatus == null || "All Status".equals(selectedStatus) || selectedStatus.equals(recommendation.getStatus())
        );
    }

    private void setupActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<Recommendation, Void>() {
            private final Button viewButton = new Button("👁");
            private final Button approveButton = new Button("✓");
            private final Button rejectButton = new Button("✕");
            
            {
                viewButton.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4;");
                approveButton.setStyle("-fx-background-color: #22C55E; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4;");
                rejectButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4;");
                
                viewButton.setOnAction(e -> handleViewRecommendation(getTableView().getItems().get(getIndex())));
                approveButton.setOnAction(e -> handleApproveRecommendation(getTableView().getItems().get(getIndex())));
                rejectButton.setOnAction(e -> handleRejectRecommendation(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Recommendation recommendation = getTableView().getItems().get(getIndex());
                    HBox hbox = new HBox(5);
                    hbox.setAlignment(Pos.CENTER);
                    hbox.getChildren().add(viewButton);
                    
                    if ("PENDING".equals(recommendation.getStatus())) {
                        hbox.getChildren().addAll(approveButton, rejectButton);
                    }
                    setGraphic(hbox);
                }
                setStyle("-fx-background-color: transparent;");
            }
        });
    }

    private void handleViewRecommendation(Recommendation recommendation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vunguard/views/RecommendationDetailsView.fxml"));
            Parent root = loader.load();
            
            RecommendationDetailsController controller = loader.getController();
            controller.setRecommendation(recommendation);
            
            Stage stage = new Stage();
            stage.setTitle("Recommendation Details");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open recommendation details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleApproveRecommendation(Recommendation recommendation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Approve Recommendation");
        alert.setHeaderText("Approve Recommendation");
        alert.setContentText("Are you sure you want to approve this " + recommendation.getType() + 
                           " recommendation for " + recommendation.getProductName() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                recommendation.setStatus("APPROVED");
                recommendation.setUpdated(LocalDateTime.now());
                recommendationsTable.refresh();
                showAlert("Success", "Recommendation approved successfully!", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void handleRejectRecommendation(Recommendation recommendation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reject Recommendation");
        alert.setHeaderText("Reject Recommendation");
        alert.setContentText("Are you sure you want to reject this " + recommendation.getType() + 
                           " recommendation for " + recommendation.getProductName() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                recommendation.setStatus("REJECTED");
                recommendation.setUpdated(LocalDateTime.now());
                recommendationsTable.refresh();
                showAlert("Success", "Recommendation rejected successfully!", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void loadSampleData() {
        List<Recommendation> sampleRecommendations = new ArrayList<>();
        
        sampleRecommendations.add(new Recommendation(
            "REC001", "Real Estate Income Trust", "budi_analyst", "SELL", 15.00, 10.00, 3, "REJECTED",
            LocalDateTime.now().minusDays(25), "Medium Term",
            "The real estate market shows signs of overvaluation in key sectors...",
            "Technical indicators show bearish divergence...",
            "Strong fundamentals but market timing concerns...",
            "Interest rate risk and liquidity concerns..."
        ));
        
        sampleRecommendations.add(new Recommendation(
            "REC002", "AI & Robotics Fund", "budi_analyst", "BUY", 13.00, 11.00, 3, "IMPLEMENTED",
            LocalDateTime.now().minusDays(25), "Long Term",
            "AI technology adoption accelerating across industries...",
            "Strong momentum indicators and volume confirmation...",
            "Growing market demand and innovation pipeline...",
            "Technology sector volatility and regulatory changes..."
        ));
        
        sampleRecommendations.add(new Recommendation(
            "REC003", "Clean Energy ETF", "sarah_analyst", "BUY", 28.50, 25.00, 4, "PENDING",
            LocalDateTime.now().minusDays(2), "Medium Term",
            "Government policies favoring renewable energy transition...",
            "Breakout above key resistance levels...",
            "Strong ESG demand and policy support...",
            "Commodity price volatility and supply chain issues..."
        ));
        
        sampleRecommendations.add(new Recommendation(
            "REC004", "Emerging Markets Bond", "david_analyst", "SELL", 45.00, 48.00, 2, "PENDING",
            LocalDateTime.now().minusDays(1), "Short Term",
            "Currency headwinds and geopolitical tensions rising...",
            "Technical breakdown below support...",
            "Weak economic fundamentals in key markets...",
            "High currency risk and political instability..."
        ));
        
        allRecommendations.addAll(sampleRecommendations);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 