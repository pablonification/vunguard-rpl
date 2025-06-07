package com.vunguard.controllers;

import com.vunguard.dao.RecommendationDAO;
import com.vunguard.services.AuthService;
import com.vunguard.models.Recommendation;
import com.vunguard.models.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CreateRecommendationController {

    @FXML
    private Button closeButton;
    
    @FXML
    private ComboBox<String> productComboBox;
    
    @FXML
    private ComboBox<String> typeComboBox;
    
    @FXML
    private TextField currentPriceField;
    
    @FXML
    private TextField targetPriceField;
    
    @FXML
    private ComboBox<String> confidenceComboBox;
    
    @FXML
    private ComboBox<String> timeframeComboBox;
    
    @FXML
    private TextArea rationaleTextArea;
    
    @FXML
    private TextArea technicalAnalysisTextArea;
    
    @FXML
    private TextArea fundamentalAnalysisTextArea;
    
    @FXML
    private TextArea risksTextArea;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button createButton;

    private InvestmentRecommendationsController parentController;
    private Recommendation editingRecommendation;
    private boolean isEditing = false;
    private RecommendationDAO recommendationDAO;

    @FXML
    private void initialize() {
        System.out.println("CreateRecommendationController initialized");
        this.recommendationDAO = new RecommendationDAO();
        setupComboBoxes();
        setupValidation();
        setDefaultValues();
    }

    private void setupComboBoxes() {
        // Product options (sample products)
        List<String> products = Arrays.asList(
            "Tech Innovation Fund",
            "Real Estate Income Trust", 
            "AI & Robotics Fund",
            "Clean Energy ETF",
            "Emerging Markets Bond",
            "Healthcare REIT",
            "Global Bond Index",
            "Growth Stock Portfolio",
            "Dividend Income Fund",
            "International Equity ETF"
        );
        productComboBox.setItems(FXCollections.observableArrayList(products));

        // Type is already set in FXML (BUY/SELL)
        
        // Confidence levels are already set in FXML (star ratings)
        
        // Timeframes are already set in FXML
    }

    private void setupValidation() {
        // Numeric validation for price fields
        currentPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                currentPriceField.setText(oldVal);
            }
            updateCreateButtonState();
        });

        targetPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                targetPriceField.setText(oldVal);
            }
            updateCreateButtonState();
        });

        // Update button state when other fields change
        productComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateCreateButtonState());
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateCreateButtonState());
        confidenceComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateCreateButtonState());
        timeframeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateCreateButtonState());
        rationaleTextArea.textProperty().addListener((obs, oldVal, newVal) -> updateCreateButtonState());
    }

    private void setDefaultValues() {
        typeComboBox.getSelectionModel().select("BUY");
        confidenceComboBox.getSelectionModel().select("★★★☆☆");
        timeframeComboBox.getSelectionModel().select("Medium Term");
    }

    private void updateCreateButtonState() {
        boolean isValid = productComboBox.getSelectionModel().getSelectedItem() != null
                && typeComboBox.getSelectionModel().getSelectedItem() != null
                && !currentPriceField.getText().trim().isEmpty()
                && !targetPriceField.getText().trim().isEmpty()
                && confidenceComboBox.getSelectionModel().getSelectedItem() != null
                && timeframeComboBox.getSelectionModel().getSelectedItem() != null
                && !rationaleTextArea.getText().trim().isEmpty();

        createButton.setDisable(!isValid);
    }

    public void setParentController(InvestmentRecommendationsController parentController) {
        this.parentController = parentController;
    }

    public void setEditingRecommendation(Recommendation recommendation) {
        this.editingRecommendation = recommendation;
        this.isEditing = true;
        createButton.setText("Update Recommendation");
        populateFieldsForEditing();
    }

    private void populateFieldsForEditing() {
        if (editingRecommendation == null) return;

        productComboBox.getSelectionModel().select(editingRecommendation.getProductName());
        typeComboBox.getSelectionModel().select(editingRecommendation.getType());
        currentPriceField.setText(String.valueOf(editingRecommendation.getCurrentPrice()));
        targetPriceField.setText(String.valueOf(editingRecommendation.getTargetPrice()));
        
        // Select confidence based on star count
        confidenceComboBox.getSelectionModel().select(editingRecommendation.getConfidenceStars());
        
        timeframeComboBox.getSelectionModel().select(editingRecommendation.getTimeframe());
        rationaleTextArea.setText(editingRecommendation.getRationale());
        technicalAnalysisTextArea.setText(editingRecommendation.getTechnicalAnalysis());
        fundamentalAnalysisTextArea.setText(editingRecommendation.getFundamentalAnalysis());
        risksTextArea.setText(editingRecommendation.getRisks());
    }

    @FXML
    private void handleCreate() {
        if (!validateForm()) {
            if (!validateForm()) return;

        try {
            Recommendation recommendation = buildRecommendation();
            boolean success = recommendationDAO.createRecommendation(recommendation); // Panggil DAO
            
            if (success) {
                showAlert("Success", "Recommendation created successfully!", Alert.AlertType.INFORMATION);
                // Refresh data di parent controller
                if (parentController != null) parentController.loadRecommendationsFromDB();
                handleClose();
            } else {
                showAlert("Error", "Failed to save recommendation to the database.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            // ... (error handling) ...
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (productComboBox.getSelectionModel().getSelectedItem() == null) {
            errors.append("• Please select a product\n");
        }

        if (typeComboBox.getSelectionModel().getSelectedItem() == null) {
            errors.append("• Please select a recommendation type\n");
        }

        try {
            double currentPrice = Double.parseDouble(currentPriceField.getText().trim());
            if (currentPrice <= 0) {
                errors.append("• Current price must be greater than 0\n");
            }
        } catch (NumberFormatException e) {
            errors.append("• Please enter a valid current price\n");
        }

        try {
            double targetPrice = Double.parseDouble(targetPriceField.getText().trim());
            if (targetPrice <= 0) {
                errors.append("• Target price must be greater than 0\n");
            }
        } catch (NumberFormatException e) {
            errors.append("• Please enter a valid target price\n");
        }

        if (confidenceComboBox.getSelectionModel().getSelectedItem() == null) {
            errors.append("• Please select a confidence level\n");
        }

        if (timeframeComboBox.getSelectionModel().getSelectedItem() == null) {
            errors.append("• Please select a timeframe\n");
        }

        if (rationaleTextArea.getText().trim().isEmpty()) {
            errors.append("• Please provide a rationale\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", "Please fix the following errors:\n\n" + errors.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private Recommendation buildRecommendation() {
        // Tentukan ID: gunakan yang sudah ada jika edit, buat baru jika tidak.
        String id = isEditing ? editingRecommendation.getId() : "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Ambil nama analis dari user yang sedang login
        User currentUser = AuthService.getCurrentUser();
        String analystName = (currentUser != null) ? currentUser.getUsername() : "unknown_analyst";
        
        // Ambil nilai dari setiap field di UI
        String productName = productComboBox.getValue();
        String type = typeComboBox.getValue();
        double currentPrice = Double.parseDouble(currentPriceField.getText().trim());
        double targetPrice = Double.parseDouble(targetPriceField.getText().trim());
        String timeframe = timeframeComboBox.getValue();
        String rationale = rationaleTextArea.getText().trim();
        String technicalAnalysis = technicalAnalysisTextArea.getText().trim();
        String fundamentalAnalysis = fundamentalAnalysisTextArea.getText().trim();
        String risks = risksTextArea.getText().trim();
        
        // Konversi rating bintang (String) menjadi angka (int)
        String confidenceStars = confidenceComboBox.getValue();
        int confidence = (int) confidenceStars.chars().filter(ch -> ch == '★').count();

        // Tentukan status: PENDING untuk baru, atau status lama jika edit
        String status = isEditing ? editingRecommendation.getStatus() : "PENDING";
        
        // Tentukan tanggal pembuatan: tanggal lama jika edit, atau sekarang jika baru
        LocalDateTime createdAt = isEditing ? editingRecommendation.getCreated() : LocalDateTime.now();

        // Buat objek Recommendation baru
        Recommendation recommendation = new Recommendation(
            id, productName, analystName, type, targetPrice, currentPrice, 
            confidence, status, createdAt, timeframe, rationale, 
            technicalAnalysis, fundamentalAnalysis, risks
        );
        
        // Jika sedang mengedit, set waktu update
        if (isEditing) {
            recommendation.setUpdated(LocalDateTime.now());
        }

        return recommendation;
    }

    @FXML
    private void handleCancel() {
        handleClose();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 