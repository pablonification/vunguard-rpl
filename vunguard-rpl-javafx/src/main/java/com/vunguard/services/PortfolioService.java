package com.vunguard.services;

import com.vunguard.dao.PortfolioDAO;
import com.vunguard.dao.PortfolioDAO.PortfolioHolding;
import com.vunguard.models.Portfolio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PortfolioService {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);
    private static PortfolioService instance;
    
    private final PortfolioDAO portfolioDAO;
    private final AuthenticationService authService;
    
    private PortfolioService() {
        this.portfolioDAO = PortfolioDAO.getInstance();
        this.authService = AuthenticationService.getInstance();
    }
    
    public static synchronized PortfolioService getInstance() {
        if (instance == null) {
            instance = new PortfolioService();
        }
        return instance;
    }
    
    /**
     * Create a new portfolio for the current user
     */
    public boolean createPortfolio(String name, String description, double initialCashBalance) {
        return createPortfolioForUser(name, description, initialCashBalance, null);
    }
    
    /**
     * Create a new portfolio for a specific user (admin/manager only)
     */
    public boolean createPortfolioForUser(String name, String description, double initialCashBalance, Integer targetUserId) {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot create portfolio");
            return false;
        }
        
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Portfolio name cannot be empty");
            return false;
        }
        
        if (initialCashBalance < 0) {
            logger.warn("Initial cash balance cannot be negative");
            return false;
        }
        
        try {
            // Determine which user to create portfolio for
            int userId;
            if (targetUserId != null) {
                // Creating for specific user - check permissions
                String currentUserRole = authService.getCurrentUserRole();
                if (!isAuthorizedToViewUserPortfolios(currentUserRole)) {
                    logger.warn("User {} not authorized to create portfolios for other users", 
                               authService.getCurrentUsername());
                    return false;
                }
                userId = targetUserId;
            } else {
                // Creating for current user
                userId = authService.getCurrentUserId();
            }
            
            // Create portfolio with user ID as the portfolio ID initially (will be updated with generated ID)
            Portfolio portfolio = new Portfolio(
                String.valueOf(userId), // temporary ID, will be updated after creation
                name.trim(),
                0.0, // assetValue starts at 0
                initialCashBalance,
                0.0, // returnPercentage starts at 0
                0    // numberOfAssets starts at 0
            );
            
            boolean success = portfolioDAO.createPortfolio(portfolio);
            
            if (success) {
                logger.info("Portfolio '{}' created successfully for user {}", name, userId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating portfolio '{}': {}", name, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get all portfolios for the current user
     */
    public List<Portfolio> getUserPortfolios() {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot retrieve portfolios");
            return new ArrayList<>();
        }
        
        try {
            int userId = authService.getCurrentUserId();
            List<Portfolio> portfolios = portfolioDAO.getPortfoliosByUserId(userId);
            
            logger.info("Retrieved {} portfolios for user {}", portfolios.size(), userId);
            return portfolios;
            
        } catch (Exception e) {
            logger.error("Error retrieving portfolios: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get portfolios for a specific user (admin/manager access)
     */
    public List<Portfolio> getPortfoliosForUser(int userId) {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot retrieve portfolios");
            return new ArrayList<>();
        }
        
        String currentUserRole = authService.getCurrentUserRole();
        if (!isAuthorizedToViewUserPortfolios(currentUserRole)) {
            logger.warn("User {} not authorized to view portfolios for user {}", 
                       authService.getCurrentUsername(), userId);
            return new ArrayList<>();
        }
        
        try {
            List<Portfolio> portfolios = portfolioDAO.getPortfoliosByUserId(userId);
            
            logger.info("Retrieved {} portfolios for user {} by {}", 
                       portfolios.size(), userId, authService.getCurrentUsername());
            return portfolios;
            
        } catch (Exception e) {
            logger.error("Error retrieving portfolios for user {}: {}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get a specific portfolio by ID
     */
    public Portfolio getPortfolio(int portfolioId) {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot retrieve portfolio");
            return null;
        }
        
        try {
            Portfolio portfolio = portfolioDAO.getPortfolioById(portfolioId);
            
            if (portfolio != null) {
                // Additional authorization check could be added here
                // to ensure user can access this specific portfolio
                logger.info("Retrieved portfolio: {}", portfolio.getName());
            }
            
            return portfolio;
            
        } catch (Exception e) {
            logger.error("Error retrieving portfolio {}: {}", portfolioId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Update portfolio information
     */
    public boolean updatePortfolio(Portfolio portfolio) {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot update portfolio");
            return false;
        }
        
        if (portfolio == null || portfolio.getName() == null || portfolio.getName().trim().isEmpty()) {
            logger.warn("Invalid portfolio data for update");
            return false;
        }
        
        try {
            boolean success = portfolioDAO.updatePortfolio(portfolio);
            
            if (success) {
                logger.info("Portfolio '{}' updated successfully", portfolio.getName());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error updating portfolio '{}': {}", portfolio.getName(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Delete a portfolio
     */
    public boolean deletePortfolio(int portfolioId) {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot delete portfolio");
            return false;
        }
        
        try {
            // First check if portfolio exists and get its info for logging
            Portfolio portfolio = portfolioDAO.getPortfolioById(portfolioId);
            if (portfolio == null) {
                logger.warn("Portfolio {} not found for deletion", portfolioId);
                return false;
            }
            
            boolean success = portfolioDAO.deletePortfolio(portfolioId);
            
            if (success) {
                logger.info("Portfolio '{}' deleted successfully", portfolio.getName());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error deleting portfolio {}: {}", portfolioId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get portfolio holdings
     */
    public List<PortfolioHolding> getPortfolioHoldings(int portfolioId) {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot retrieve portfolio holdings");
            return new ArrayList<>();
        }
        
        try {
            List<PortfolioHolding> holdings = portfolioDAO.getPortfolioHoldings(portfolioId);
            
            logger.info("Retrieved {} holdings for portfolio {}", holdings.size(), portfolioId);
            return holdings;
            
        } catch (Exception e) {
            logger.error("Error retrieving holdings for portfolio {}: {}", portfolioId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Refresh portfolio values and calculations
     */
    public boolean refreshPortfolioValues(int portfolioId) {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot refresh portfolio values");
            return false;
        }
        
        try {
            boolean success = portfolioDAO.updatePortfolioValues(portfolioId);
            
            if (success) {
                logger.info("Portfolio values refreshed successfully for portfolio {}", portfolioId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error refreshing portfolio values for {}: {}", portfolioId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get all portfolios (admin only)
     */
    public List<Portfolio> getAllPortfolios() {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot retrieve all portfolios");
            return new ArrayList<>();
        }
        
        String currentUserRole = authService.getCurrentUserRole();
        if (!"admin".equals(currentUserRole)) {
            logger.warn("User {} not authorized to view all portfolios", authService.getCurrentUsername());
            return new ArrayList<>();
        }
        
        try {
            // This would require a new DAO method to get all portfolios across all users
            // For now, return empty list
            logger.info("Admin requested all portfolios");
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error retrieving all portfolios: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Validate portfolio name
     */
    public boolean isValidPortfolioName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() <= 100;
    }
    
    /**
     * Check if current user is authorized to view portfolios for specific user
     */
    private boolean isAuthorizedToViewUserPortfolios(String role) {
        return "admin".equals(role) || "manager".equals(role);
    }
    
    /**
     * Get portfolio summary statistics for current user
     */
    public PortfolioSummary getPortfolioSummary() {
        if (!authService.isLoggedIn()) {
            logger.warn("User not logged in, cannot get portfolio summary");
            return new PortfolioSummary(0, 0.0, 0.0, 0.0);
        }
        
        try {
            List<Portfolio> portfolios = getUserPortfolios();
            
            int totalPortfolios = portfolios.size();
            double totalValue = portfolios.stream().mapToDouble(Portfolio::getTotalValue).sum();
            double totalCash = portfolios.stream().mapToDouble(Portfolio::getCashBalance).sum();
            double averageReturn = portfolios.stream()
                .mapToDouble(Portfolio::getReturnPercentage)
                .average()
                .orElse(0.0);
            
            return new PortfolioSummary(totalPortfolios, totalValue, totalCash, averageReturn);
            
        } catch (Exception e) {
            logger.error("Error calculating portfolio summary: {}", e.getMessage(), e);
            return new PortfolioSummary(0, 0.0, 0.0, 0.0);
        }
    }
    
    /**
     * Inner class for portfolio summary statistics
     */
    public static class PortfolioSummary {
        private final int totalPortfolios;
        private final double totalValue;
        private final double totalCash;
        private final double averageReturn;
        
        public PortfolioSummary(int totalPortfolios, double totalValue, double totalCash, double averageReturn) {
            this.totalPortfolios = totalPortfolios;
            this.totalValue = totalValue;
            this.totalCash = totalCash;
            this.averageReturn = averageReturn;
        }
        
        public int getTotalPortfolios() { return totalPortfolios; }
        public double getTotalValue() { return totalValue; }
        public double getTotalCash() { return totalCash; }
        public double getAverageReturn() { return averageReturn; }
        public double getTotalAssetValue() { return totalValue - totalCash; }
    }
} 