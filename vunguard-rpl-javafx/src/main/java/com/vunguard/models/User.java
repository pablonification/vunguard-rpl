package com.vunguard.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class User {
    private String username;
    private String fullName;
    private String email;
    private String role;
    private LocalDate createdDate;
    
    public User(String username, String fullName, String email, String role, LocalDate createdDate) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdDate = createdDate;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
    
    public LocalDate getCreatedDate() {
        return createdDate;
    }
    
    public String getCreatedDateFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return createdDate.format(formatter);
    }
    
    // Setters
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
} 