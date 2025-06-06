package com.vunguard.controllers;

import com.vunguard.models.Recommendation;
import com.vunguard.services.RecommendationService;
import com.vunguard.services.AuthenticationService;
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

public class InvestmentRecommendationsController {

    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private Button newRecommendationButton;
    
    @FXML
    private VBox tableContainer;
    
    @FXML
    private SidebarController sidebarViewController;

    private TableView<Recommendation> recommendationsTable;
    private TableColumn<Recommendation, String> productColumn;
    private TableColumn<Recommendation, String> typeColumn;
    private TableColumn<Recommendation, Double> targetPriceColumn;
    private TableColumn<Recommendation, Double> currentPriceColumn;
    private TableColumn<Recommendation, String> confidenceColumn;
    private TableColumn<Recommendation, String> statusColumn;
    private TableColumn<Recommendation, String> createdColumn;
    private TableColumn<Recommendation, Void> actionsColumn;

    private ObservableList<Recommendation> allRecommendations = FXCollections.observableArrayList();
    private FilteredList<Recommendation> filteredRecommendations;
    private RecommendationService recommendationService;
    private AuthenticationService authService;
    private String currentAnalyst = "current_analyst"; // Will be updated from session

    @FXML
    private void initialize() {
        System.out.println("InvestmentRecommendationsController initialized");
        
        // Initialize services
        try {
            recommendationService = RecommendationService.getInstance();
            authService = AuthenticationService.getInstance();
            
            // Get current analyst from session
            if (authService.isAuthenticated()) {
                currentAnalyst = authService.getCurrentUser().getUsername();
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize services: " + e.getMessage());
            e.printStackTrace();
        }
        
        createTable();
        setupTable();
        setupFilters();
        loadRecommendationsFromDatabase();
        setupActionButtons();
    }

    private void createTable() {
        // Create TableView
        recommendationsTable = new TableView<>();
        recommendationsTable.setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: transparent;");
        
        // Create columns
        productColumn = new TableColumn<>("Product");
        productColumn.setPrefWidth(120);
        
        typeColumn = new TableColumn<>("Type");
        typeColumn.setPrefWidth(80);
        
        targetPriceColumn = new TableColumn<>("Target Price");
        targetPriceColumn.setPrefWidth(100);
        
        currentPriceColumn = new TableColumn<>("Current Price");
        currentPriceColumn.setPrefWidth(100);
        
        confidenceColumn = new TableColumn<>("Confidence");
        confidenceColumn.setPrefWidth(100);
        
        statusColumn = new TableColumn<>("Status");
        statusColumn.setPrefWidth(100);
        
        createdColumn = new TableColumn<>("Created");
        createdColumn.setPrefWidth(120);
        
        actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(150);
        
        // Add columns to table
        recommendationsTable.getColumns().addAll(
            productColumn, typeColumn, targetPriceColumn, currentPriceColumn,
            confidenceColumn, statusColumn, createdColumn, actionsColumn
        );
        
        // Add table to container
        tableContainer.getChildren().add(recommendationsTable);
        VBox.setVgrow(recommendationsTable, javafx.scene.layout.Priority.ALWAYS);
    }

    private void setupTable() {
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
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
            (selectedStatus == null || "All Status".equals(selectedStatus) || selectedStatus.equals(recommendation.getStatus()))
            && currentAnalyst.equals(recommendation.getAnalystName()) // Only show current analyst's recommendations
        );
    }

    private void setupActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<Recommendation, Void>() {
            private final Button viewButton = new Button("👁");
            private final Button editButton = new Button("✏");
            private final Button deleteButton = new Button("🗑");
            
            {
                viewButton.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4;");
                editButton.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4;");
                deleteButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4;");
                
                viewButton.setOnAction(e -> handleViewRecommendation(getTableView().getItems().get(getIndex())));
                editButton.setOnAction(e -> handleEditRecommendation(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> handleDeleteRecommendation(getTableView().getItems().get(getIndex())));
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
                    
                    // Only show edit/delete for pending recommendations
                    if ("PENDING".equals(recommendation.getStatus())) {
                        hbox.getChildren().addAll(editButton, deleteButton);
                    }
                    setGraphic(hbox);
                }
                setStyle("-fx-background-color: transparent;");
            }
        });
    }

    @FXML
    private void handleCreateRecommendation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vunguard/views/CreateRecommendationView.fxml"));
            Parent root = loader.load();
            
            CreateRecommendationController controller = loader.getController();
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Create New Recommendation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open create recommendation dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

    private void handleEditRecommendation(Recommendation recommendation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vunguard/views/CreateRecommendationView.fxml"));
            Parent root = loader.load();
            
            CreateRecommendationController controller = loader.getController();
            controller.setParentController(this);
            controller.setEditingRecommendation(recommendation);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Recommendation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open edit recommendation dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteRecommendation(Recommendation recommendation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Recommendation");
        alert.setHeaderText("Delete Recommendation");
        alert.setContentText("Are you sure you want to delete this " + recommendation.getType() + 
                           " recommendation for " + recommendation.getProductName() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (recommendationService != null) {
                        // Delete from database first
                        boolean success = recommendationService.deleteRecommendation(recommendation.getId());
                        if (success) {
                            // Reload recommendations from database to reflect the deletion
                            loadRecommendationsFromDatabase();
                            showAlert("Success", "Recommendation deleted successfully!", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Error", "Failed to delete recommendation from database. Please try again.", Alert.AlertType.ERROR);
                        }
                    } else {
                        showAlert("Error", "Recommendation service not available. Please restart the application.", Alert.AlertType.ERROR);
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting recommendation: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete recommendation: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    public void addRecommendation(Recommendation recommendation) {
        try {
            if (recommendationService != null) {
                // Save to database using RecommendationService
                boolean success = recommendationService.createRecommendation(
                    recommendation.getProductName(),
                    recommendation.getType(),
                    recommendation.getTargetPrice(),
                    recommendation.getCurrentPrice(),
                    recommendation.getConfidence(),
                    recommendation.getTimeframe(),
                    recommendation.getRationale(),
                    recommendation.getTechnicalAnalysis(),
                    recommendation.getFundamentalAnalysis(),
                    recommendation.getRisks()
                );
                
                if (success) {
                    // Reload recommendations from database to get the latest data
                    loadRecommendationsFromDatabase();
                    System.out.println("Recommendation saved to database successfully");
                } else {
                    showAlert("Error", "Failed to save recommendation to database. Please try again.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Recommendation service not available. Please restart the application.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.err.println("Error saving recommendation to database: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to save recommendation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void updateRecommendation(Recommendation oldRecommendation, Recommendation newRecommendation) {
        try {
            if (recommendationService != null) {
                // Update in database first
                boolean success = recommendationService.updateRecommendation(newRecommendation);
                if (success) {
                    // Reload recommendations from database to get the latest data
                    loadRecommendationsFromDatabase();
                    System.out.println("Recommendation updated in database successfully");
                } else {
                    showAlert("Error", "Failed to update recommendation in database. Please try again.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Recommendation service not available. Please restart the application.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.err.println("Error updating recommendation: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to update recommendation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadRecommendationsFromDatabase() {
        if (recommendationService == null) {
            System.err.println("RecommendationService not initialized, using empty data");
            return;
        }
        
        try {
            // Load only current user's recommendations
            List<Recommendation> recommendations = recommendationService.getCurrentUserRecommendations();
            allRecommendations.clear();
            allRecommendations.addAll(recommendations);
            System.out.println("Loaded " + recommendations.size() + " recommendations for analyst: " + currentAnalyst);
        } catch (Exception e) {
            System.err.println("Failed to load recommendations from database: " + e.getMessage());
            e.printStackTrace();
            
            // Show error to user
            showAlert("Database Error", 
                     "Failed to load your recommendations from database. Please check your connection.", 
                     Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 