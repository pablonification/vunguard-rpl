package com.vunguard.dao;

import com.vunguard.config.DatabaseConfig;
import com.vunguard.models.Portfolio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PortfolioDAO extends BaseDAO<Portfolio> {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioDAO.class);
    
    private static PortfolioDAO instance;
    
    private PortfolioDAO() {
        super();
    }
    
    public static synchronized PortfolioDAO getInstance() {
        if (instance == null) {
            instance = new PortfolioDAO();
        }
        return instance;
    }
    
    @Override
    protected String getTableName() {
        return "portfolios";
    }
    
    @Override
    protected Portfolio mapResultSetToEntity(ResultSet rs) throws SQLException {
        double assetValue = rs.getDouble("total_value") - rs.getDouble("cash_balance");
        
        return new Portfolio(
            String.valueOf(rs.getInt("id")),
            rs.getString("name"),
            assetValue,
            rs.getDouble("cash_balance"),
            rs.getDouble("return_percentage"),
            rs.getInt("asset_count")
        );
    }
    
    /**
     * Create a new portfolio
     */
    public boolean createPortfolio(Portfolio portfolio) {
        String sql = """
            INSERT INTO portfolios (user_id, name, description, total_value, cash_balance) 
            VALUES (?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, Integer.parseInt(portfolio.getId())); // user_id
            stmt.setString(2, portfolio.getName());
            stmt.setString(3, ""); // description - Portfolio model doesn't have this field
            stmt.setDouble(4, portfolio.getTotalValue());
            stmt.setDouble(5, portfolio.getCashBalance());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        portfolio.setId(String.valueOf(generatedKeys.getInt(1)));
                    }
                }
                logger.info("Portfolio created successfully: {}", portfolio.getName());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error creating portfolio: {}", e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Get all portfolios for a specific user
     */
    public List<Portfolio> getPortfoliosByUserId(int userId) {
        List<Portfolio> portfolios = new ArrayList<>();
        String sql = """
            SELECT p.id, p.name, p.total_value, p.cash_balance, p.created_at, p.updated_at,
                   COALESCE(holdings_count.count, 0) as asset_count,
                   COALESCE(performance.return_percentage, 0.0) as return_percentage
            FROM portfolios p
            LEFT JOIN (
                SELECT portfolio_id, COUNT(*) as count 
                FROM portfolio_holdings 
                GROUP BY portfolio_id
            ) holdings_count ON p.id = holdings_count.portfolio_id
            LEFT JOIN (
                SELECT portfolio_id, 
                       CASE 
                           WHEN invested_amount > 0 THEN 
                               ((total_value - invested_amount) / invested_amount) * 100
                           ELSE 0.0
                       END as return_percentage
                FROM performance_metrics pm1
                WHERE pm1.date = (
                    SELECT MAX(date) 
                    FROM performance_metrics pm2 
                    WHERE pm2.portfolio_id = pm1.portfolio_id
                )
            ) performance ON p.id = performance.portfolio_id
            WHERE p.user_id = ?
            ORDER BY p.name
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                double assetValue = rs.getDouble("total_value") - rs.getDouble("cash_balance");
                
                Portfolio portfolio = new Portfolio(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("name"),
                    assetValue,
                    rs.getDouble("cash_balance"),
                    rs.getDouble("return_percentage"),
                    rs.getInt("asset_count")
                );
                
                portfolios.add(portfolio);
            }
            
            logger.info("Retrieved {} portfolios for user {}", portfolios.size(), userId);
            
        } catch (SQLException e) {
            logger.error("Error retrieving portfolios for user {}: {}", userId, e.getMessage(), e);
        }
        
        return portfolios;
    }
    
    /**
     * Get a specific portfolio by ID
     */
    public Portfolio getPortfolioById(int portfolioId) {
        String sql = """
            SELECT p.id, p.name, p.total_value, p.cash_balance, p.created_at, p.updated_at,
                   COALESCE(holdings_count.count, 0) as asset_count,
                   COALESCE(performance.return_percentage, 0.0) as return_percentage
            FROM portfolios p
            LEFT JOIN (
                SELECT portfolio_id, COUNT(*) as count 
                FROM portfolio_holdings 
                GROUP BY portfolio_id
            ) holdings_count ON p.id = holdings_count.portfolio_id
            LEFT JOIN (
                SELECT portfolio_id, 
                       CASE 
                           WHEN invested_amount > 0 THEN 
                               ((total_value - invested_amount) / invested_amount) * 100
                           ELSE 0.0
                       END as return_percentage
                FROM performance_metrics pm1
                WHERE pm1.date = (
                    SELECT MAX(date) 
                    FROM performance_metrics pm2 
                    WHERE pm2.portfolio_id = pm1.portfolio_id
                )
            ) performance ON p.id = performance.portfolio_id
            WHERE p.id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, portfolioId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double assetValue = rs.getDouble("total_value") - rs.getDouble("cash_balance");
                
                Portfolio portfolio = new Portfolio(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("name"),
                    assetValue,
                    rs.getDouble("cash_balance"),
                    rs.getDouble("return_percentage"),
                    rs.getInt("asset_count")
                );
                
                logger.info("Retrieved portfolio: {}", portfolio.getName());
                return portfolio;
            }
            
        } catch (SQLException e) {
            logger.error("Error retrieving portfolio {}: {}", portfolioId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Update portfolio information
     */
    public boolean updatePortfolio(Portfolio portfolio) {
        String sql = """
            UPDATE portfolios 
            SET name = ?, total_value = ?, cash_balance = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, portfolio.getName());
            stmt.setDouble(2, portfolio.getTotalValue());
            stmt.setDouble(3, portfolio.getCashBalance());
            stmt.setInt(4, Integer.parseInt(portfolio.getId()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Portfolio updated successfully: {}", portfolio.getName());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error updating portfolio {}: {}", portfolio.getName(), e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Delete a portfolio
     */
    public boolean deletePortfolio(int portfolioId) {
        String sql = "DELETE FROM portfolios WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, portfolioId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Portfolio deleted successfully: {}", portfolioId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting portfolio {}: {}", portfolioId, e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Get portfolio holdings
     */
    public List<PortfolioHolding> getPortfolioHoldings(int portfolioId) {
        List<PortfolioHolding> holdings = new ArrayList<>();
        String sql = """
            SELECT ph.id, ph.portfolio_id, ph.product_id, ph.quantity, ph.average_cost, 
                   ph.current_value, ph.last_updated,
                   p.symbol, p.name as product_name, p.current_price
            FROM portfolio_holdings ph
            JOIN products p ON ph.product_id = p.id
            WHERE ph.portfolio_id = ?
            ORDER BY p.name
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, portfolioId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                PortfolioHolding holding = new PortfolioHolding();
                holding.setId(rs.getInt("id"));
                holding.setPortfolioId(rs.getInt("portfolio_id"));
                holding.setProductId(rs.getInt("product_id"));
                holding.setQuantity(rs.getDouble("quantity"));
                holding.setAverageCost(rs.getDouble("average_cost"));
                holding.setCurrentValue(rs.getDouble("current_value"));
                holding.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                holding.setProductSymbol(rs.getString("symbol"));
                holding.setProductName(rs.getString("product_name"));
                holding.setCurrentPrice(rs.getDouble("current_price"));
                
                holdings.add(holding);
            }
            
            logger.info("Retrieved {} holdings for portfolio {}", holdings.size(), portfolioId);
            
        } catch (SQLException e) {
            logger.error("Error retrieving holdings for portfolio {}: {}", portfolioId, e.getMessage(), e);
        }
        
        return holdings;
    }
    
    /**
     * Update portfolio value calculations
     */
    public boolean updatePortfolioValues(int portfolioId) {
        String sql = """
            UPDATE portfolios 
            SET total_value = (
                SELECT COALESCE(cash_balance, 0) + COALESCE(SUM(ph.current_value), 0)
                FROM portfolios p
                LEFT JOIN portfolio_holdings ph ON p.id = ph.portfolio_id
                WHERE p.id = ?
                GROUP BY p.id, p.cash_balance
            ),
            updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, portfolioId);
            stmt.setInt(2, portfolioId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Portfolio values updated successfully for portfolio {}", portfolioId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error updating portfolio values for {}: {}", portfolioId, e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Inner class for portfolio holdings
     */
    public static class PortfolioHolding {
        private int id;
        private int portfolioId;
        private int productId;
        private double quantity;
        private double averageCost;
        private double currentValue;
        private LocalDateTime lastUpdated;
        private String productSymbol;
        private String productName;
        private double currentPrice;
        
        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public int getPortfolioId() { return portfolioId; }
        public void setPortfolioId(int portfolioId) { this.portfolioId = portfolioId; }
        
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
        
        public double getAverageCost() { return averageCost; }
        public void setAverageCost(double averageCost) { this.averageCost = averageCost; }
        
        public double getCurrentValue() { return currentValue; }
        public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
        
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
        
        public String getProductSymbol() { return productSymbol; }
        public void setProductSymbol(String productSymbol) { this.productSymbol = productSymbol; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public double getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    }
} 