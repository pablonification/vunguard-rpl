package com.vunguard.controllers;

import com.vunguard.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class SidebarController {

    @FXML
    private Button dashboardButton;
    @FXML
    private Button portfoliosButton;
    @FXML
    private Button transactionsButton;
    @FXML
    private Button performanceButton;
    @FXML
    private Button accountsButton;
    @FXML
    private Button collapseButton;
    @FXML
    private Button supportButton;
    @FXML
    private Button logoutButton;

    // Flag to simulate admin role - in a real app this would come from authentication system
    private boolean isAdmin = true;

    @FXML
    private void initialize() {
        System.out.println("SidebarController initialized");
        
        // Hide Accounts button for non-admin users
        accountsButton.setVisible(isAdmin);
        accountsButton.setManaged(isAdmin);
        
        // Tambahkan styling atau event listener lain jika perlu
        // Contoh: Menandai tombol aktif (misal Dashboard awalnya aktif)
        dashboardButton.setStyle("-fx-font-family: 'Inter 18pt'; -fx-text-fill: white; -fx-background-color: #1E1E2C; -fx-pref-width: 200; -fx-alignment: baseline-left; -fx-padding: 10 15; -fx-background-radius: 8;");
    }

    @FXML
    private void handleDashboardAction(ActionEvent event) {
        System.out.println("Dashboard button clicked");
        setActiveButton(dashboardButton);
        try {
            Main.loadDashboardScene();
        } catch (IOException e) {
            System.err.println("Error loading Dashboard scene: " + e.getMessage());
        }
    }

    @FXML
    private void handlePortfoliosAction(ActionEvent event) {
        System.out.println("Portfolios button clicked");
        setActiveButton(portfoliosButton);
        try {
            Main.loadPortfolioScene();
        } catch (IOException e) {
            System.err.println("Error loading Portfolio scene: " + e.getMessage());
        }
    }

    @FXML
    private void handleTransactionsAction(ActionEvent event) {
        System.out.println("Transactions button clicked");
        setActiveButton(transactionsButton);
        try {
            Main.loadTransactionScene();
        } catch (IOException e) {
            System.err.println("Error loading Transaction scene: " + e.getMessage());
        }
    }

    @FXML
    private void handlePerformanceAction(ActionEvent event) {
        System.out.println("Performance button clicked");
        setActiveButton(performanceButton);
        try {
            Main.loadPerformanceScene();
        } catch (IOException e) {
            System.err.println("Error loading Performance scene: " + e.getMessage());
        }
    }

    @FXML
    private void handleAccountsAction(ActionEvent event) {
        System.out.println("Accounts button clicked");
        setActiveButton(accountsButton);
        try {
            Main.loadAccountsScene();
        } catch (IOException e) {
            System.err.println("Error loading Accounts scene: " + e.getMessage());
        }
    }

    @FXML
    private void handleCollapseAction(ActionEvent event) {
        System.out.println("Collapse button clicked");
        // Logika untuk collapse/expand sidebar bisa ditambahkan di sini
    }    @FXML
    private void handleSupportAction(ActionEvent event) {
        System.out.println("Support button clicked");
        setActiveButton(supportButton);
        try {
            Main.loadSupportScene();
        } catch (IOException e) {
            System.err.println("Error loading Support scene: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogoutAction(ActionEvent event) {
        System.out.println("Logout button clicked");
        // Logika untuk logout
        try {
            Main.loadLoginScene();
        } catch (IOException e) {
            System.err.println("Error loading Login scene: " + e.getMessage());
        }
    }
    
    // Method untuk mengatur highlight pada tombol Portfolio dari controller lain
    public void setActivePageForPortfolio() {
        setActiveButton(portfoliosButton);
    }
    
    // Method untuk mengatur highlight pada tombol Dashboard dari controller lain
    public void setActivePageForDashboard() {
        setActiveButton(dashboardButton);
    }
    
    // Method untuk mengatur highlight pada tombol Accounts dari controller lain
    public void setActivePageForAccounts() {
        setActiveButton(accountsButton);
    }
    
    // Getter methods for buttons
    public Button getDashboardButton() {
        return dashboardButton;
    }
    
    public Button getPortfoliosButton() {
        return portfoliosButton;
    }
    
    public Button getTransactionsButton() {
        return transactionsButton;
    }
    
    public Button getPerformanceButton() {
        return performanceButton;
    }
      public Button getAccountsButton() {
        return accountsButton;
    }
    
    public Button getSupportButton() {
        return supportButton;
    }
    
    public void setActiveButton(Button activeButton) {
        // Reset style semua tombol navigasi utama
        resetButtonStyles(dashboardButton);
        resetButtonStyles(portfoliosButton);
        resetButtonStyles(transactionsButton);
        resetButtonStyles(performanceButton);
        resetButtonStyles(accountsButton);
        // Mungkin juga untuk supportButton jika dianggap bagian dari navigasi utama

        // Set style untuk tombol yang aktif
        activeButton.setStyle("-fx-font-family: 'Inter 18pt'; -fx-text-fill: white; -fx-background-color: #1E1E2C; -fx-pref-width: 200; -fx-alignment: baseline-left; -fx-padding: 10 15; -fx-background-radius: 8;");
    }

    private void resetButtonStyles(Button button) {
        // Hanya reset jika bukan tombol yang sedang ditekan/aktif
        // Ini untuk tombol-tombol yang normalnya transparan
        if (button != collapseButton && button != supportButton && button != logoutButton) { // Asumsi tombol bawah beda treatment
             button.setStyle("-fx-font-family: 'Inter 18pt'; -fx-text-fill: #A0A0A0; -fx-background-color: transparent; -fx-pref-width: 200; -fx-alignment: baseline-left; -fx-padding: 10 15;");
        }
        // Untuk tombol bawah, mungkin stylenya selalu sama (kecuali hover)
    }
} 