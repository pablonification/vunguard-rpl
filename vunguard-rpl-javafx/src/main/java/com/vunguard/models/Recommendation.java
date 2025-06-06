package com.vunguard.models;

import java.time.LocalDateTime;

public class Recommendation {
    private String id;
    private String productName;
    private String analystName;
    private String type; // BUY or SELL
    private double targetPrice;
    private double currentPrice;
    private int confidence; // 1-5 stars
    private String status; // PENDING, APPROVED, REJECTED, IMPLEMENTED
    private LocalDateTime created;
    private LocalDateTime updated;
    private String timeframe; // Short Term, Medium Term, Long Term
    private String rationale;
    private String technicalAnalysis;
    private String fundamentalAnalysis;
    private String risks;
    private LocalDateTime implemented;

    // Constructors
    public Recommendation() {
    }

    public Recommendation(String id, String productName, String analystName, String type, 
                         double targetPrice, double currentPrice, int confidence, String status, 
                         LocalDateTime created, String timeframe, String rationale, 
                         String technicalAnalysis, String fundamentalAnalysis, String risks) {
        this.id = id;
        this.productName = productName;
        this.analystName = analystName;
        this.type = type;
        this.targetPrice = targetPrice;
        this.currentPrice = currentPrice;
        this.confidence = confidence;
        this.status = status;
        this.created = created;
        this.timeframe = timeframe;
        this.rationale = rationale;
        this.technicalAnalysis = technicalAnalysis;
        this.fundamentalAnalysis = fundamentalAnalysis;
        this.risks = risks;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAnalystName() {
        return analystName;
    }

    public void setAnalystName(String analystName) {
        this.analystName = analystName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getTechnicalAnalysis() {
        return technicalAnalysis;
    }

    public void setTechnicalAnalysis(String technicalAnalysis) {
        this.technicalAnalysis = technicalAnalysis;
    }

    public String getFundamentalAnalysis() {
        return fundamentalAnalysis;
    }

    public void setFundamentalAnalysis(String fundamentalAnalysis) {
        this.fundamentalAnalysis = fundamentalAnalysis;
    }

    public String getRisks() {
        return risks;
    }

    public void setRisks(String risks) {
        this.risks = risks;
    }

    public LocalDateTime getImplemented() {
        return implemented;
    }

    public void setImplemented(LocalDateTime implemented) {
        this.implemented = implemented;
    }

    // Helper methods
    public String getConfidenceStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < confidence; i++) {
            stars.append("★");
        }
        for (int i = confidence; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    public double getPriceChange() {
        return targetPrice - currentPrice;
    }

    public double getPriceChangePercentage() {
        if (currentPrice == 0) return 0;
        return ((targetPrice - currentPrice) / currentPrice) * 100;
    }

    public String getCreatedTimeAgo() {
        if (created == null) return "Unknown";
        
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.temporal.ChronoUnit.DAYS.between(created, now);
        
        if (days == 0) {
            return "Today";
        } else if (days == 1) {
            return "1 day ago";
        } else {
            return days + " days ago";
        }
    }

    @Override
    public String toString() {
        return "Recommendation{" +
                "id='" + id + '\'' +
                ", productName='" + productName + '\'' +
                ", analystName='" + analystName + '\'' +
                ", type='" + type + '\'' +
                ", targetPrice=" + targetPrice +
                ", currentPrice=" + currentPrice +
                ", confidence=" + confidence +
                ", status='" + status + '\'' +
                ", created=" + created +
                '}';
    }
} 