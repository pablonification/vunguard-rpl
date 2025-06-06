package com.vunguard.dao;

import com.vunguard.models.Recommendation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class RecommendationDAO extends BaseDAO<Recommendation> {
    
    @Override
    protected Recommendation mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Recommendation(
            rs.getString("id"),
            rs.getString("product_name"),
            rs.getString("analyst_name"),
            rs.getString("type"),
            rs.getDouble("target_price"),
            rs.getDouble("current_price"),
            rs.getInt("confidence"),
            rs.getString("status"),
            rs.getTimestamp("created").toLocalDateTime(),
            rs.getString("timeframe"),
            rs.getString("rationale"),
            rs.getString("technical_analysis"),
            rs.getString("fundamental_analysis"),
            rs.getString("risks")
        );
    }
    
    @Override
    protected String getTableName() {
        return "recommendations";
    }
    
    public List<Recommendation> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY created DESC";
        return executeQuery(sql);
    }
    
    public List<Recommendation> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE status = ? ORDER BY created DESC";
        return executeQuery(sql, status);
    }
    
    public List<Recommendation> findByAnalyst(String analystName) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE analyst_name = ? ORDER BY created DESC";
        return executeQuery(sql, analystName);
    }
    
    public List<Recommendation> findByType(String type) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE type = ? ORDER BY created DESC";
        return executeQuery(sql, type);
    }
    
    public Recommendation findById(String id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        return executeQuerySingle(sql, id);
    }
    
    public List<Recommendation> findPending() throws SQLException {
        return findByStatus("PENDING");
    }
    
    public boolean createRecommendation(Recommendation recommendation) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + 
                    " (id, product_name, analyst_name, type, target_price, current_price, confidence, status, created, timeframe, rationale, technical_analysis, fundamental_analysis, risks) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        int rowsAffected = executeUpdate(sql,
            recommendation.getId(),
            recommendation.getProductName(),
            recommendation.getAnalystName(),
            recommendation.getType(),
            recommendation.getTargetPrice(),
            recommendation.getCurrentPrice(),
            recommendation.getConfidence(),
            recommendation.getStatus(),
            recommendation.getCreated(),
            recommendation.getTimeframe(),
            recommendation.getRationale(),
            recommendation.getTechnicalAnalysis(),
            recommendation.getFundamentalAnalysis(),
            recommendation.getRisks()
        );
        
        return rowsAffected > 0;
    }
    
    public boolean updateRecommendation(Recommendation recommendation) throws SQLException {
        String sql = "UPDATE " + getTableName() + 
                    " SET product_name = ?, analyst_name = ?, type = ?, target_price = ?, current_price = ?, " +
                    "confidence = ?, status = ?, timeframe = ?, rationale = ?, technical_analysis = ?, " +
                    "fundamental_analysis = ?, risks = ?, updated = ? WHERE id = ?";
        
        int rowsAffected = executeUpdate(sql,
            recommendation.getProductName(),
            recommendation.getAnalystName(),
            recommendation.getType(),
            recommendation.getTargetPrice(),
            recommendation.getCurrentPrice(),
            recommendation.getConfidence(),
            recommendation.getStatus(),
            recommendation.getTimeframe(),
            recommendation.getRationale(),
            recommendation.getTechnicalAnalysis(),
            recommendation.getFundamentalAnalysis(),
            recommendation.getRisks(),
            LocalDateTime.now(),
            recommendation.getId()
        );
        
        return rowsAffected > 0;
    }
    
    public boolean updateStatus(String id, String status) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET status = ?, updated = ? WHERE id = ?";
        
        int rowsAffected = executeUpdate(sql, status, LocalDateTime.now(), id);
        return rowsAffected > 0;
    }
    
    public boolean deleteRecommendation(String id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        
        int rowsAffected = executeUpdate(sql, id);
        return rowsAffected > 0;
    }
    
    public List<Recommendation> searchRecommendations(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE product_name ILIKE ? OR analyst_name ILIKE ? OR analysis ILIKE ? " +
                    "ORDER BY created DESC";
        String searchPattern = "%" + searchTerm + "%";
        
        return executeQuery(sql, searchPattern, searchPattern, searchPattern);
    }
    
    public List<Recommendation> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE created BETWEEN ? AND ? ORDER BY created DESC";
        
        return executeQuery(sql, startDate, endDate);
    }
} 