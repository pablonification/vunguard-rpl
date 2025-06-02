package com.vunguard.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Portfolio {
    private String name;
    private double assetValue;
    private double cashBalance;
    private double totalValue;
    private double returnPercentage;
    private int numberOfAssets;
    private LocalDateTime lastUpdated;

    public Portfolio(String name, double assetValue, double cashBalance, double returnPercentage, int numberOfAssets) {
        this.name = name;
        this.assetValue = assetValue;
        this.cashBalance = cashBalance;
        this.totalValue = assetValue + cashBalance;
        this.returnPercentage = returnPercentage;
        this.numberOfAssets = numberOfAssets;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getAssetValue() {
        return assetValue;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public double getReturnPercentage() {
        return returnPercentage;
    }

    public int getNumberOfAssets() {
        return numberOfAssets;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getLastUpdatedFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return lastUpdated.format(formatter);
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAssetValue(double assetValue) {
        this.assetValue = assetValue;
        this.totalValue = this.assetValue + this.cashBalance;
        this.lastUpdated = LocalDateTime.now();
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
        this.totalValue = this.assetValue + this.cashBalance;
        this.lastUpdated = LocalDateTime.now();
    }

    public void setReturnPercentage(double returnPercentage) {
        this.returnPercentage = returnPercentage;
        this.lastUpdated = LocalDateTime.now();
    }

    public void setNumberOfAssets(int numberOfAssets) {
        this.numberOfAssets = numberOfAssets;
        this.lastUpdated = LocalDateTime.now();
    }
} 