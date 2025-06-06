package com.vunguard.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String id;
    private String type;
    private String description;
    private double amount;
    private String portfolio;
    private String product;
    private int quantity;
    private double price;
    private double total;
    private LocalDateTime date;
    private String status;

    // Constructor for web UI format (Portfolio, Product, Type, Quantity, Price, Total, Date)
    public Transaction(String id, String type, String portfolio, String product, int quantity, double price, String dateFormatted) {
        this.id = id;
        this.type = type;
        this.portfolio = portfolio;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.total = quantity * price;
        this.date = LocalDateTime.now(); // In real app, parse dateFormatted
        this.status = "Completed";
        this.description = type + " " + quantity + " units of " + product;
        this.amount = this.total;
    }

    // Original constructor
    public Transaction(String id, String type, String description, double amount, String portfolio, LocalDateTime date, String status) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.portfolio = portfolio;
        this.date = date;
        this.status = status;
        this.product = "N/A";
        this.quantity = 1;
        this.price = amount;
        this.total = amount;
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

    public String getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getTotal() {
        return total;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDateFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
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

    public void setProduct(String product) {
        this.product = product;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 