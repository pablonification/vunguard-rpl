package com.vunguard.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String id;
    private String type;
    private String description;
    private double amount;
    private String portfolio;
    private LocalDateTime date;
    private String status;

    public Transaction(String id, String type, String description, double amount, String portfolio, LocalDateTime date, String status) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.portfolio = portfolio;
        this.date = date;
        this.status = status;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDateFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");
        return date.format(formatter);
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 