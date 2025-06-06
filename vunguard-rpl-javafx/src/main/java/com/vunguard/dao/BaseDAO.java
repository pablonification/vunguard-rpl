package com.vunguard.dao;

import com.vunguard.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {
    protected DatabaseConfig dbConfig;
    
    public BaseDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    protected Connection getConnection() throws SQLException {
        return dbConfig.getConnection();
    }
    
    // Abstract methods that subclasses must implement
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    protected abstract String getTableName();
    
    // Common database operations
    protected List<T> executeQuery(String sql, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
            }
        }
        
        return results;
    }
    
    protected T executeQuerySingle(String sql, Object... params) throws SQLException {
        List<T> results = executeQuery(sql, params);
        return results.isEmpty() ? null : results.get(0);
    }
    
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            return stmt.executeUpdate();
        }
    }
    
    protected int executeInsert(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating record failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating record failed, no ID obtained.");
                }
            }
        }
    }
} 