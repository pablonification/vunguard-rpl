package com.vunguard.services;

import com.vunguard.dao.RecommendationDAO;
import com.vunguard.models.Recommendation;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class RecommendationService {
    private static RecommendationService instance;
    private RecommendationDAO recommendationDAO;
    private AuthenticationService authService;
    
    private RecommendationService() {
        this.recommendationDAO = new RecommendationDAO();
        this.authService = AuthenticationService.getInstance();
    }
    
    public static synchronized RecommendationService getInstance() {
        if (instance == null) {
            instance = new RecommendationService();
        }
        return instance;
    }
    
    public List<Recommendation> getAllRecommendations() throws SQLException {
        return recommendationDAO.findAll();
    }
    
    public List<Recommendation> getRecommendationsByStatus(String status) throws SQLException {
        return recommendationDAO.findByStatus(status);
    }
    
    public List<Recommendation> getPendingRecommendations() throws SQLException {
        return recommendationDAO.findPending();
    }
    
    public List<Recommendation> getRecommendationsByAnalyst(String analystName) throws SQLException {
        return recommendationDAO.findByAnalyst(analystName);
    }
    
    public List<Recommendation> getCurrentUserRecommendations() throws SQLException {
        if (!authService.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        String username = authService.getCurrentUser().getUsername();
        return recommendationDAO.findByAnalyst(username);
    }
    
    public Recommendation getRecommendationById(String id) throws SQLException {
        return recommendationDAO.findById(id);
    }
    
    public boolean createRecommendation(String productName, String type, double targetPrice, 
                                      double currentPrice, int confidence, String timeframe,
                                      String rationale, String technicalAnalysis, 
                                      String fundamentalAnalysis, String risks) throws SQLException {
        
        // Check permissions
        if (!authService.canCreateRecommendations()) {
            throw new SecurityException("Insufficient permissions to create recommendations");
        }
        
        // Validate input
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        
        if (!type.equals("BUY") && !type.equals("SELL")) {
            throw new IllegalArgumentException("Type must be BUY or SELL");
        }
        
        if (confidence < 1 || confidence > 5) {
            throw new IllegalArgumentException("Confidence must be between 1 and 5");
        }
        
        // Create recommendation
        Recommendation recommendation = new Recommendation();
        recommendation.setId(generateRecommendationId());
        recommendation.setProductName(productName.trim());
        recommendation.setAnalystName(authService.getCurrentUser().getUsername());
        recommendation.setType(type);
        recommendation.setTargetPrice(targetPrice);
        recommendation.setCurrentPrice(currentPrice);
        recommendation.setConfidence(confidence);
        recommendation.setStatus("PENDING");
        recommendation.setCreated(LocalDateTime.now());
        recommendation.setTimeframe(timeframe);
        recommendation.setRationale(rationale);
        recommendation.setTechnicalAnalysis(technicalAnalysis);
        recommendation.setFundamentalAnalysis(fundamentalAnalysis);
        recommendation.setRisks(risks);
        
        return recommendationDAO.createRecommendation(recommendation);
    }
    
    public boolean updateRecommendation(Recommendation recommendation) throws SQLException {
        // Check permissions
        if (!authService.canCreateRecommendations()) {
            throw new SecurityException("Insufficient permissions to update recommendations");
        }
        
        // Only allow updates if user owns the recommendation or is admin/manager
        Recommendation existing = recommendationDAO.findById(recommendation.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Recommendation not found");
        }
        
        String currentUsername = authService.getCurrentUser().getUsername();
        boolean isOwner = currentUsername.equals(existing.getAnalystName());
        boolean canManage = authService.canApproveRecommendations();
        
        if (!isOwner && !canManage) {
            throw new SecurityException("Can only update your own recommendations");
        }
        
        // Update timestamp
        recommendation.setUpdated(LocalDateTime.now());
        
        return recommendationDAO.updateRecommendation(recommendation);
    }
    
    public boolean approveRecommendation(String id) throws SQLException {
        // Check permissions
        if (!authService.canApproveRecommendations()) {
            throw new SecurityException("Insufficient permissions to approve recommendations");
        }
        
        Recommendation recommendation = recommendationDAO.findById(id);
        if (recommendation == null) {
            throw new IllegalArgumentException("Recommendation not found");
        }
        
        if (!"PENDING".equals(recommendation.getStatus())) {
            throw new IllegalStateException("Only pending recommendations can be approved");
        }
        
        return recommendationDAO.updateStatus(id, "APPROVED");
    }
    
    public boolean rejectRecommendation(String id) throws SQLException {
        // Check permissions
        if (!authService.canApproveRecommendations()) {
            throw new SecurityException("Insufficient permissions to reject recommendations");
        }
        
        Recommendation recommendation = recommendationDAO.findById(id);
        if (recommendation == null) {
            throw new IllegalArgumentException("Recommendation not found");
        }
        
        if (!"PENDING".equals(recommendation.getStatus())) {
            throw new IllegalStateException("Only pending recommendations can be rejected");
        }
        
        return recommendationDAO.updateStatus(id, "REJECTED");
    }
    
    public boolean implementRecommendation(String id) throws SQLException {
        // Check permissions
        if (!authService.canApproveRecommendations()) {
            throw new SecurityException("Insufficient permissions to implement recommendations");
        }
        
        Recommendation recommendation = recommendationDAO.findById(id);
        if (recommendation == null) {
            throw new IllegalArgumentException("Recommendation not found");
        }
        
        if (!"APPROVED".equals(recommendation.getStatus())) {
            throw new IllegalStateException("Only approved recommendations can be implemented");
        }
        
        return recommendationDAO.updateStatus(id, "IMPLEMENTED");
    }
    
    public boolean deleteRecommendation(String id) throws SQLException {
        // Check permissions
        Recommendation recommendation = recommendationDAO.findById(id);
        if (recommendation == null) {
            throw new IllegalArgumentException("Recommendation not found");
        }
        
        String currentUsername = authService.getCurrentUser().getUsername();
        boolean isOwner = currentUsername.equals(recommendation.getAnalystName());
        boolean canManage = authService.canApproveRecommendations();
        
        if (!isOwner && !canManage) {
            throw new SecurityException("Can only delete your own recommendations");
        }
        
        // Only allow deletion of pending or rejected recommendations
        String status = recommendation.getStatus();
        if (!"PENDING".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalStateException("Can only delete pending or rejected recommendations");
        }
        
        return recommendationDAO.deleteRecommendation(id);
    }
    
    public List<Recommendation> searchRecommendations(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllRecommendations();
        }
        
        return recommendationDAO.searchRecommendations(searchTerm.trim());
    }
    
    public List<Recommendation> getRecommendationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return recommendationDAO.findByDateRange(startDate, endDate);
    }
    
    private String generateRecommendationId() {
        return "REC" + System.currentTimeMillis();
    }
    
    // Statistics methods
    public long getTotalRecommendations() throws SQLException {
        return getAllRecommendations().size();
    }
    
    public long getPendingCount() throws SQLException {
        return getPendingRecommendations().size();
    }
    
    public long getApprovedCount() throws SQLException {
        return getRecommendationsByStatus("APPROVED").size();
    }
    
    public long getRejectedCount() throws SQLException {
        return getRecommendationsByStatus("REJECTED").size();
    }
    
    public long getImplementedCount() throws SQLException {
        return getRecommendationsByStatus("IMPLEMENTED").size();
    }
} 