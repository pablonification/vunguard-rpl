package com.vunguard.services;

import com.vunguard.dao.UserDAO;
import com.vunguard.models.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthenticationService {
    private static AuthenticationService instance;
    private UserDAO userDAO;
    private Map<String, User> activeSessions;
    private User currentUser;
    
    private AuthenticationService() {
        this.userDAO = new UserDAO();
        this.activeSessions = new HashMap<>();
    }
    
    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }
    
    public String login(String username, String password) throws SQLException {
        User user = userDAO.authenticate(username, password);
        
        if (user != null) {
            // Generate session token
            String sessionToken = UUID.randomUUID().toString();
            
            // Store session
            activeSessions.put(sessionToken, user);
            currentUser = user;
            
            return sessionToken;
        }
        
        return null;
    }
    
    public boolean logout(String sessionToken) {
        if (sessionToken != null && activeSessions.containsKey(sessionToken)) {
            activeSessions.remove(sessionToken);
            currentUser = null;
            return true;
        }
        return false;
    }
    
    public void logout() {
        // Logout current user
        currentUser = null;
        // Clear all sessions for simplicity (in real app, you'd track specific session)
        activeSessions.clear();
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public User getUserBySession(String sessionToken) {
        return activeSessions.get(sessionToken);
    }
    
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
    
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
    
    public boolean hasRole(String role) {
        return currentUser != null && role.equals(currentUser.getRole());
    }
    
    public boolean hasAnyRole(String... roles) {
        if (currentUser == null) return false;
        
        String userRole = currentUser.getRole();
        for (String role : roles) {
            if (role.equals(userRole)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean canManageUsers() {
        return hasAnyRole("admin", "manager");
    }
    
    public boolean canApproveRecommendations() {
        return hasAnyRole("admin", "manager");
    }
    
    public boolean canCreateRecommendations() {
        return hasAnyRole("admin", "manager", "analyst");
    }
    
    public boolean canViewReports() {
        return hasAnyRole("admin", "manager", "analyst");
    }
    
    public boolean isValidSession(String sessionToken) {
        return sessionToken != null && activeSessions.containsKey(sessionToken);
    }
    
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    public void clearAllSessions() {
        activeSessions.clear();
        currentUser = null;
    }
    
    // User registration (admin only)
    public User registerUser(String username, String fullName, String email, String role, String password) throws SQLException {
        // Check if current user can create users
        if (!canManageUsers()) {
            throw new SecurityException("Insufficient permissions to create users");
        }
        
        // Validate username and email uniqueness
        if (userDAO.usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setRole(role);
        
        int userId = userDAO.createUser(newUser, password);
        newUser.setId(userId);
        
        return newUser;
    }
} 