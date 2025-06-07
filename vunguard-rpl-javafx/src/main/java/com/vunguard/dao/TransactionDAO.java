package com.vunguard.dao;

import com.vunguard.models.Transaction;
import com.vunguard.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TransactionDAO extends BaseDAO<Transaction> {
    private static final Logger logger = Logger.getLogger(TransactionDAO.class.getName());

    @Override
    protected String getTableName() {
        return "transactions";
    }

    @Override
    protected Transaction mapResultSetToEntity(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(String.valueOf(rs.getInt("id")));
        transaction.setType(rs.getString("type"));
        transaction.setQuantity(rs.getBigDecimal("quantity") != null ? rs.getBigDecimal("quantity").intValue() : 0);
        transaction.setPrice(rs.getDouble("price"));
        transaction.setTotal(rs.getDouble("total_amount"));
        transaction.setDescription(rs.getString("description"));
        transaction.setStatus(rs.getString("status"));
        
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            transaction.setDate(timestamp.toLocalDateTime());
        }
        
        // Set portfolio and product names (requires joins)
        transaction.setPortfolio("Portfolio #" + rs.getInt("portfolio_id")); // Will be updated with actual names
        transaction.setProduct("Product #" + rs.getInt("product_id")); // Will be updated with actual names
        
        return transaction;
    }

    /**
     * Create a new transaction (buy/sell/deposit/withdrawal)
     */
    public boolean createTransaction(int userId, Integer portfolioId, Integer productId, 
                                   String type, Double quantity, Double price, 
                                   double totalAmount, String description) {
        String sql = """
            INSERT INTO transactions (user_id, portfolio_id, product_id, type, quantity, price, 
                                    total_amount, description, status, created_at, executed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            if (portfolioId != null) {
                stmt.setInt(2, portfolioId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            if (productId != null) {
                stmt.setInt(3, productId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, type);
            if (quantity != null) {
                stmt.setDouble(5, quantity);
            } else {
                stmt.setNull(5, Types.DOUBLE);
            }
            if (price != null) {
                stmt.setDouble(6, price);
            } else {
                stmt.setNull(6, Types.DOUBLE);
            }
            stmt.setDouble(7, totalAmount);
            stmt.setString(8, description);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Transaction created successfully: " + description);
                return true;
            }
            
        } catch (SQLException e) {
            logger.severe("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Get all transactions for a specific user with portfolio and product names
     */
    public List<Transaction> getTransactionsForUser(int userId) {
        String sql = """
            SELECT t.*, 
                   p.name as portfolio_name,
                   pr.name as product_name,
                   pr.symbol as product_symbol
            FROM transactions t
            LEFT JOIN portfolios p ON t.portfolio_id = p.id
            LEFT JOIN products pr ON t.product_id = pr.id
            WHERE t.user_id = ?
            ORDER BY t.created_at DESC
            """;
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Transaction transaction = mapResultSetToEntity(rs);
                
                // Set actual portfolio and product names
                String portfolioName = rs.getString("portfolio_name");
                transaction.setPortfolio(portfolioName != null ? portfolioName : "Cash Account");
                
                String productName = rs.getString("product_name");
                String productSymbol = rs.getString("product_symbol");
                if (productName != null) {
                    transaction.setProduct(productSymbol != null ? productSymbol : productName);
                } else {
                    transaction.setProduct("Cash");
                }
                
                transactions.add(transaction);
            }
            
            logger.info("Retrieved " + transactions.size() + " transactions for user " + userId);
            
        } catch (SQLException e) {
            logger.severe("Error retrieving transactions for user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }

    /**
     * Get transactions for a specific portfolio
     */
    public List<Transaction> getTransactionsForPortfolio(int portfolioId) {
        String sql = """
            SELECT t.*, 
                   p.name as portfolio_name,
                   pr.name as product_name,
                   pr.symbol as product_symbol
            FROM transactions t
            LEFT JOIN portfolios p ON t.portfolio_id = p.id
            LEFT JOIN products pr ON t.product_id = pr.id
            WHERE t.portfolio_id = ?
            ORDER BY t.created_at DESC
            """;
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, portfolioId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Transaction transaction = mapResultSetToEntity(rs);
                
                // Set actual portfolio and product names
                String portfolioName = rs.getString("portfolio_name");
                transaction.setPortfolio(portfolioName != null ? portfolioName : "Portfolio #" + portfolioId);
                
                String productName = rs.getString("product_name");
                String productSymbol = rs.getString("product_symbol");
                if (productName != null) {
                    transaction.setProduct(productSymbol != null ? productSymbol : productName);
                } else {
                    transaction.setProduct("Cash");
                }
                
                transactions.add(transaction);
            }
            
            logger.info("Retrieved " + transactions.size() + " transactions for portfolio " + portfolioId);
            
        } catch (SQLException e) {
            logger.severe("Error retrieving transactions for portfolio: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }

    /**
     * Get transaction by ID with full details
     */
    public Transaction getTransactionById(int transactionId) {
        String sql = """
            SELECT t.*, 
                   p.name as portfolio_name,
                   pr.name as product_name,
                   pr.symbol as product_symbol,
                   a.full_name as user_name
            FROM transactions t
            LEFT JOIN portfolios p ON t.portfolio_id = p.id
            LEFT JOIN products pr ON t.product_id = pr.id
            LEFT JOIN accounts a ON t.user_id = a.id
            WHERE t.id = ?
            """;
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Transaction transaction = mapResultSetToEntity(rs);
                
                // Set actual portfolio and product names
                String portfolioName = rs.getString("portfolio_name");
                transaction.setPortfolio(portfolioName != null ? portfolioName : "Cash Account");
                
                String productName = rs.getString("product_name");
                String productSymbol = rs.getString("product_symbol");
                if (productName != null) {
                    transaction.setProduct(productSymbol != null ? productSymbol : productName);
                } else {
                    transaction.setProduct("Cash");
                }
                
                logger.info("Retrieved transaction " + transactionId);
                return transaction;
            }
            
        } catch (SQLException e) {
            logger.severe("Error retrieving transaction by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Update transaction status
     */
    public boolean updateTransactionStatus(int transactionId, String status) {
        String sql = "UPDATE transactions SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, transactionId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Transaction " + transactionId + " status updated to " + status);
                return true;
            }
            
        } catch (SQLException e) {
            logger.severe("Error updating transaction status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Get total transaction volume for a user
     */
    public double getTotalTransactionVolume(int userId) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM transactions WHERE user_id = ? AND status = 'COMPLETED'";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting total transaction volume: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }

    /**
     * Get transaction statistics for a portfolio
     */
    public TransactionStats getPortfolioTransactionStats(int portfolioId) {
        String sql = """
            SELECT 
                COUNT(*) as total_transactions,
                SUM(CASE WHEN type = 'BUY' THEN total_amount ELSE 0 END) as total_buys,
                SUM(CASE WHEN type = 'SELL' THEN total_amount ELSE 0 END) as total_sells,
                SUM(CASE WHEN type = 'DEPOSIT' THEN total_amount ELSE 0 END) as total_deposits,
                SUM(CASE WHEN type = 'WITHDRAWAL' THEN total_amount ELSE 0 END) as total_withdrawals
            FROM transactions 
            WHERE portfolio_id = ? AND status = 'COMPLETED'
            """;
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, portfolioId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new TransactionStats(
                    rs.getInt("total_transactions"),
                    rs.getDouble("total_buys"),
                    rs.getDouble("total_sells"),
                    rs.getDouble("total_deposits"),
                    rs.getDouble("total_withdrawals")
                );
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting portfolio transaction stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return new TransactionStats(0, 0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Helper class for transaction statistics
     */
    public static class TransactionStats {
        private final int totalTransactions;
        private final double totalBuys;
        private final double totalSells;
        private final double totalDeposits;
        private final double totalWithdrawals;
        
        public TransactionStats(int totalTransactions, double totalBuys, double totalSells, 
                              double totalDeposits, double totalWithdrawals) {
            this.totalTransactions = totalTransactions;
            this.totalBuys = totalBuys;
            this.totalSells = totalSells;
            this.totalDeposits = totalDeposits;
            this.totalWithdrawals = totalWithdrawals;
        }
        
        // Getters
        public int getTotalTransactions() { return totalTransactions; }
        public double getTotalBuys() { return totalBuys; }
        public double getTotalSells() { return totalSells; }
        public double getTotalDeposits() { return totalDeposits; }
        public double getTotalWithdrawals() { return totalWithdrawals; }
        public double getNetFlow() { return totalDeposits - totalWithdrawals; }
        public double getTradingVolume() { return totalBuys + totalSells; }
    }
} 