package com.vunguard.services;

import com.vunguard.dao.ProductDAO;
import com.vunguard.models.Product;
import java.sql.SQLException;
import java.util.List;

public class ProductService {
    private static ProductService instance;
    private ProductDAO productDAO;
    private AuthenticationService authService;
    
    private ProductService() {
        this.productDAO = new ProductDAO();
        this.authService = AuthenticationService.getInstance();
    }
    
    public static synchronized ProductService getInstance() {
        if (instance == null) {
            instance = new ProductService();
        }
        return instance;
    }
    
    public List<Product> getAllProducts() throws SQLException {
        return productDAO.findAll();
    }
    
    public Product getProductById(String id) throws SQLException {
        return productDAO.findById(id);
    }
    
    public Product getProductBySymbol(String symbol) throws SQLException {
        return productDAO.findBySymbol(symbol);
    }
    
    public boolean createProduct(String code, String name, String description, String strategy, String riskLevel) throws SQLException {
        // Check permissions
        if (!authService.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        String role = authService.getCurrentUser().getRole();
        if (!"admin".equals(role) && !"manager".equals(role)) {
            throw new SecurityException("Insufficient permissions to create products");
        }
        
        // Validate input
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Product code is required");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        
        // Check if product code already exists
        Product existing = productDAO.findBySymbol(code.trim());
        if (existing != null) {
            throw new IllegalArgumentException("Product with code '" + code.trim() + "' already exists");
        }
        
        // Create product
        Product product = new Product("0", code.trim(), name.trim(), 
                                    description != null ? description.trim() : "", 
                                    strategy != null ? strategy : "Balanced", 
                                    riskLevel != null ? riskLevel : "Medium");
        
        return productDAO.createProduct(product);
    }
    
    public List<Product> searchProducts(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }
        
        return productDAO.searchProducts(searchTerm.trim());
    }
    
    // Statistics methods
    public long getTotalProducts() throws SQLException {
        return getAllProducts().size();
    }
    
    public long getProductsByRiskLevel(String riskLevel) throws SQLException {
        return getAllProducts().stream()
            .filter(p -> riskLevel.equals(p.getRiskLevel()))
            .count();
    }
    
    public long getProductsByStrategy(String strategy) throws SQLException {
        return getAllProducts().stream()
            .filter(p -> strategy.equals(p.getStrategy()))
            .count();
    }
} 