package com.vunguard.services;

import com.vunguard.dao.TransactionDAO;
import com.vunguard.dao.UserDAO;
import com.vunguard.models.Transaction;
import com.vunguard.models.User;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for handling transaction operations
 * Simplified version focusing on basic transaction CRUD operations
 */
public class TransactionService {
    private static final Logger logger = Logger.getLogger(TransactionService.class.getName());
    private static TransactionService instance;
    
    private final TransactionDAO transactionDAO;
    private final AuthenticationService authService;
    private final UserDAO userDAO;
    
    private TransactionService() throws Exception {
        this.transactionDAO = new TransactionDAO();
        this.authService = AuthenticationService.getInstance();
        this.userDAO = new UserDAO();
    }
    
    public static synchronized TransactionService getInstance() throws Exception {
        if (instance == null) {
            instance = new TransactionService();
        }
        return instance;
    }

    /**
     * Create a basic transaction record
     */
    public boolean createTransaction(String type, String portfolioName, String productName, 
                                   int quantity, double price, String description) {
        try {
            // Get current user ID
            int userId = authService.getCurrentUserId();
            double totalAmount = quantity * price;
            
            // For now, create transaction without portfolio/product IDs
            // These will be linked properly when DAO methods are implemented
            boolean success = transactionDAO.createTransaction(
                userId, null, null, type, 
                (double) quantity, price, totalAmount, description
            );
            
            if (success) {
                logger.info("Transaction created successfully: " + description);
                return true;
            }
            
        } catch (Exception e) {
            logger.severe("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Create a transaction for a specific user by email (for admins/managers)
     */
    public boolean createTransactionForUser(String userEmail, String type, String portfolioName, 
                                          String productName, int quantity, double price, String description) {
        try {
            // Check if current user has permission to create transactions for other users
            if (!canCreateTransactionsForOthers()) {
                logger.warning("User does not have permission to create transactions for other users");
                return false;
            }
            
            // Find user by email
            User user = userDAO.findByEmail(userEmail);
            if (user == null) {
                logger.warning("User not found with email: " + userEmail);
                return false;
            }
            
            double totalAmount = quantity * price;
            
            // Create transaction for the specified user
            boolean success = transactionDAO.createTransaction(
                user.getId(), null, null, type, 
                (double) quantity, price, totalAmount, description
            );
            
            if (success) {
                logger.info("Transaction created successfully for user " + userEmail + ": " + description);
                return true;
            }
            
        } catch (Exception e) {
            logger.severe("Error creating transaction for user " + userEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Create a deposit transaction
     */
    public boolean createDepositTransaction(double amount, String description) {
        try {
            int userId = authService.getCurrentUserId();
            String transactionDescription = description != null ? description : "Cash deposit of $" + amount;
            
            boolean success = transactionDAO.createTransaction(
                userId, null, null, "DEPOSIT", 
                null, null, amount, transactionDescription
            );
            
            if (success) {
                logger.info("Deposit transaction created: " + transactionDescription);
                return true;
            }
            
        } catch (Exception e) {
            logger.severe("Error creating deposit transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Create a withdrawal transaction
     */
    public boolean createWithdrawalTransaction(double amount, String description) {
        try {
            int userId = authService.getCurrentUserId();
            String transactionDescription = description != null ? description : "Cash withdrawal of $" + amount;
            
            boolean success = transactionDAO.createTransaction(
                userId, null, null, "WITHDRAWAL", 
                null, null, amount, transactionDescription
            );
            
            if (success) {
                logger.info("Withdrawal transaction created: " + transactionDescription);
                return true;
            }
            
        } catch (Exception e) {
            logger.severe("Error creating withdrawal transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Get all transactions for current user
     */
    public List<Transaction> getUserTransactions() {
        try {
            int userId = authService.getCurrentUserId();
            return transactionDAO.getTransactionsForUser(userId);
        } catch (Exception e) {
            logger.severe("Error getting user transactions: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get transactions for a specific user (for managers/analysts/admins)
     */
    public List<Transaction> getTransactionsForUser(int userId) {
        try {
            // Check if current user has permission to view other users' transactions
            if (!canViewOtherUsersData()) {
                logger.warning("User does not have permission to view other users' transactions");
                return List.of();
            }
            
            return transactionDAO.getTransactionsForUser(userId);
        } catch (Exception e) {
            logger.severe("Error getting transactions for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get transactions for a user by email (for managers/analysts/admins)
     */
    public List<Transaction> getTransactionsForUser(String email) {
        try {
            // Check if current user has permission to view other users' transactions
            if (!canViewOtherUsersData()) {
                logger.warning("User does not have permission to view other users' transactions");
                return List.of();
            }
            
            // Find user by email
            User user = userDAO.findByEmail(email);
            if (user == null) {
                logger.warning("User not found with email: " + email);
                return List.of();
            }
            
            return transactionDAO.getTransactionsForUser(user.getId());
        } catch (Exception e) {
            logger.severe("Error getting transactions for user email " + email + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get transactions for a specific portfolio
     */
    public List<Transaction> getPortfolioTransactions(int portfolioId) {
        try {
            return transactionDAO.getTransactionsForPortfolio(portfolioId);
        } catch (Exception e) {
            logger.severe("Error getting portfolio transactions: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get transaction by ID
     */
    public Transaction getTransactionById(int transactionId) {
        try {
            return transactionDAO.getTransactionById(transactionId);
        } catch (Exception e) {
            logger.severe("Error getting transaction by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update transaction status
     */
    public boolean updateTransactionStatus(int transactionId, String status) {
        try {
            return transactionDAO.updateTransactionStatus(transactionId, status);
        } catch (Exception e) {
            logger.severe("Error updating transaction status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get transaction statistics for a portfolio
     */
    public TransactionDAO.TransactionStats getPortfolioStats(int portfolioId) {
        try {
            return transactionDAO.getPortfolioTransactionStats(portfolioId);
        } catch (Exception e) {
            logger.severe("Error getting portfolio transaction stats: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get total transaction volume for current user
     */
    public double getUserTransactionVolume() {
        try {
            int userId = authService.getCurrentUserId();
            return transactionDAO.getTotalTransactionVolume(userId);
        } catch (Exception e) {
            logger.severe("Error getting user transaction volume: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Check if current user can view other users' data
     */
    private boolean canViewOtherUsersData() {
        try {
            String userRole = authService.getCurrentUserRole();
            return "admin".equals(userRole) || "manager".equals(userRole) || "analyst".equals(userRole);
        } catch (Exception e) {
            logger.severe("Error checking user permissions: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user can create transactions for other users
     */
    private boolean canCreateTransactionsForOthers() {
        try {
            String userRole = authService.getCurrentUserRole();
            return "admin".equals(userRole) || "manager".equals(userRole);
        } catch (Exception e) {
            logger.severe("Error checking user permissions: " + e.getMessage());
            return false;
        }
    }
} 