package com.vunguard.controllers;

import com.vunguard.models.Recommendation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RecommendationDetailsController {

    @FXML
    private Button closeButton;
    
    @FXML
    private Label recommendationIdLabel;
    
    @FXML
    private Label productLabel;
    
    @FXML
    private Label analystLabel;
    
    @FXML
    private Label confidenceLabel;
    
    @FXML
    private Label typeLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label timeframeLabel;
    
    @FXML
    private Label currentPriceLabel;
    
    @FXML
    private Label targetPriceLabel;
    
    @FXML
    private Label priceChangeLabel;
    
    @FXML
    private TextArea rationaleTextArea;
    
    @FXML
    private TextArea technicalAnalysisTextArea;
    
    @FXML
    private TextArea fundamentalAnalysisTextArea;
    
    @FXML
    private TextArea risksTextArea;
    
    @FXML
    private Label createdLabel;
    
    @FXML
    private Label updatedLabel;
    
    @FXML
    private Label implementedLabel;
    
    @FXML
    private Button closeFooterButton;

    private Recommendation recommendation;

    @FXML
    private void initialize() {
        System.out.println("RecommendationDetailsController initialized");
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
        populateFields();
    }

    private void populateFields() {
        if (recommendation == null) return;

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

        // Basic information
        recommendationIdLabel.setText("Full details for recommendation #" + recommendation.getId());
        productLabel.setText(recommendation.getProductName());
        analystLabel.setText(recommendation.getAnalystName());
        confidenceLabel.setText(recommendation.getConfidenceStars());
        timeframeLabel.setText(recommendation.getTimeframe());

        // Type with color coding
        typeLabel.setText(recommendation.getType());
        if ("BUY".equals(recommendation.getType())) {
            typeLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-background-color: #22C55E; -fx-background-radius: 4; -fx-padding: 4 8;");
        } else if ("SELL".equals(recommendation.getType())) {
            typeLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-background-color: #EF4444; -fx-background-radius: 4; -fx-padding: 4 8;");
        }

        // Status with color coding
        statusLabel.setText(recommendation.getStatus());
        switch (recommendation.getStatus()) {
            case "PENDING":
                statusLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-background-color: #F59E0B; -fx-background-radius: 4; -fx-padding: 4 8;");
                break;
            case "APPROVED":
                statusLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-background-color: #22C55E; -fx-background-radius: 4; -fx-padding: 4 8;");
                break;
            case "REJECTED":
                statusLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-background-color: #EF4444; -fx-background-radius: 4; -fx-padding: 4 8;");
                break;
            case "IMPLEMENTED":
                statusLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-background-color: #3B82F6; -fx-background-radius: 4; -fx-padding: 4 8;");
                break;
        }

        // Prices
        currentPriceLabel.setText(currencyFormat.format(recommendation.getCurrentPrice()));
        targetPriceLabel.setText(currencyFormat.format(recommendation.getTargetPrice()));

        // Price change with color coding
        double priceChangePercent = recommendation.getPriceChangePercentage();
        priceChangeLabel.setText(String.format("%.2f%%", Math.abs(priceChangePercent)));
        if (priceChangePercent > 0) {
            priceChangeLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #22C55E;");
        } else if (priceChangePercent < 0) {
            priceChangeLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #EF4444;");
        } else {
            priceChangeLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        }

        // Analysis sections
        rationaleTextArea.setText(recommendation.getRationale() != null ? recommendation.getRationale() : "No rationale provided");
        technicalAnalysisTextArea.setText(recommendation.getTechnicalAnalysis() != null ? recommendation.getTechnicalAnalysis() : "No technical analysis provided");
        fundamentalAnalysisTextArea.setText(recommendation.getFundamentalAnalysis() != null ? recommendation.getFundamentalAnalysis() : "No fundamental analysis provided");
        risksTextArea.setText(recommendation.getRisks() != null ? recommendation.getRisks() : "No risks identified");

        // Timestamps
        if (recommendation.getCreated() != null) {
            createdLabel.setText(recommendation.getCreated().format(dateFormatter));
        } else {
            createdLabel.setText("Unknown");
        }

        if (recommendation.getUpdated() != null) {
            updatedLabel.setText(recommendation.getUpdated().format(dateFormatter));
        } else {
            updatedLabel.setText("N/A");
        }

        if (recommendation.getImplemented() != null) {
            implementedLabel.setText(recommendation.getImplemented().format(dateFormatter));
        } else {
            implementedLabel.setText("N/A");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
} 