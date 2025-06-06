package com.vunguard.models;

public class Product {
    private String id;
    private String code;
    private String name;
    private String description;
    private String strategy;
    private String riskLevel;
    
    public Product(String id, String code, String name, String description, String strategy, String riskLevel) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.strategy = strategy;
        this.riskLevel = riskLevel;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getStrategy() {
        return strategy;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    @Override
    public String toString() {
        return name; // For ComboBox display
    }
} 