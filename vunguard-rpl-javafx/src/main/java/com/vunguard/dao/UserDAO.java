package com.vunguard.dao;

import com.vunguard.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class UserDAO extends BaseDAO<User> {
    
    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
    
    @Override
    protected String getTableName() {
        return "accounts";
    }
    
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, full_name, email, role, created_at FROM " + getTableName() + " WHERE username = ?";
        
        List<User> users = executeQuery(sql, username);
        if (users.isEmpty()) {
            return null;
        }
        
        User user = users.get(0);
        // Get password from database for verification
        String storedPassword = getStoredPassword(username);
        
        if (storedPassword != null && BCrypt.checkpw(password, storedPassword)) {
            return user;
        }
        
        return null;
    }
    
    private String getStoredPassword(String username) throws SQLException {
        String sql = "SELECT password FROM " + getTableName() + " WHERE username = ?";
        
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        }
        
        return null;
    }
    
    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, full_name, email, role, created_at FROM " + getTableName() + " WHERE id = ?";
        return executeQuerySingle(sql, id);
    }
    
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, full_name, email, role, created_at FROM " + getTableName() + " WHERE username = ?";
        return executeQuerySingle(sql, username);
    }
    
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, username, full_name, email, role, created_at FROM " + getTableName() + " WHERE email = ?";
        return executeQuerySingle(sql, email);
    }
    
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, full_name, email, role, created_at FROM " + getTableName() + " ORDER BY created_at DESC";
        return executeQuery(sql);
    }
    
    public List<User> findByRole(String role) throws SQLException {
        String sql = "SELECT id, username, full_name, email, role, created_at FROM " + getTableName() + " WHERE role = ? ORDER BY created_at DESC";
        return executeQuery(sql, role);
    }
    
    public int createUser(User user, String password) throws SQLException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        String sql = "INSERT INTO " + getTableName() + " (username, full_name, email, role, password, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        
        return executeInsert(sql, 
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            hashedPassword,
            LocalDateTime.now()
        );
    }
    
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET username = ?, full_name = ?, email = ?, role = ? WHERE id = ?";
        
        int rowsAffected = executeUpdate(sql,
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getId()
        );
        
        return rowsAffected > 0;
    }
    
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        String sql = "UPDATE " + getTableName() + " SET password = ? WHERE id = ?";
        
        int rowsAffected = executeUpdate(sql, hashedPassword, userId);
        return rowsAffected > 0;
    }
    
    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        
        int rowsAffected = executeUpdate(sql, id);
        return rowsAffected > 0;
    }
    
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE username = ?";
        
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE email = ?";
        
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
} 