package com.vunguard.dao;

import com.vunguard.config.DatabaseConfig;
import com.vunguard.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    public List<Product> findAll() throws SQLException {
        String query = """
            SELECT id, symbol, name, sector, market_cap, current_price, 
                   price_change_24h, price_change_percentage_24h, volume_24h
            FROM products
            ORDER BY name
        """;
        
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }
        }
        
        System.out.println("Retrieved " + products.size() + " products from database");
        return products;
    }
    
    public Product findById(String id) throws SQLException {
        String query = """
            SELECT id, symbol, name, sector, market_cap, current_price, 
                   price_change_24h, price_change_percentage_24h, volume_24h
            FROM products
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        
        return null;
    }
    
    public Product findBySymbol(String symbol) throws SQLException {
        String query = """
            SELECT id, symbol, name, sector, market_cap, current_price, 
                   price_change_24h, price_change_percentage_24h, volume_24h
            FROM products
            WHERE symbol = ?
        """;
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, symbol);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        
        return null;
    }
    
    public boolean createProduct(Product product) throws SQLException {
        String query = """
            INSERT INTO products (symbol, name, sector, market_cap, current_price, 
                                price_change_24h, price_change_percentage_24h, volume_24h)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, product.getCode()); // Using code as symbol
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getStrategy()); // Using strategy as sector
            stmt.setBigDecimal(4, null); // market_cap
            stmt.setBigDecimal(5, null); // current_price
            stmt.setBigDecimal(6, null); // price_change_24h
            stmt.setBigDecimal(7, null); // price_change_percentage_24h
            stmt.setBigDecimal(8, null); // volume_24h
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Created product " + product.getCode() + " - rows affected: " + rowsAffected);
            return rowsAffected > 0;
        }
    }
    
    public List<Product> searchProducts(String searchTerm) throws SQLException {
        String query = """
            SELECT id, symbol, name, sector, market_cap, current_price, 
                   price_change_24h, price_change_percentage_24h, volume_24h
            FROM products
            WHERE LOWER(name) LIKE LOWER(?) OR LOWER(symbol) LIKE LOWER(?) OR LOWER(sector) LIKE LOWER(?)
            ORDER BY name
        """;
        
        List<Product> products = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    products.add(product);
                }
            }
        }
        
        return products;
    }
    
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        // Map database columns to Product model
        String id = String.valueOf(rs.getInt("id"));
        String code = rs.getString("symbol");
        String name = rs.getString("name");
        String sector = rs.getString("sector");
        
        // Create description from available data
        String description = "Investment product in " + (sector != null ? sector : "various") + " sector";
        
        // Map sector to strategy (simplified mapping)
        String strategy = "Balanced";
        if (sector != null) {
            switch (sector.toLowerCase()) {
                case "technology":
                    strategy = "Growth";
                    break;
                case "healthcare":
                    strategy = "Growth";
                    break;
                case "automotive":
                    strategy = "Growth";
                    break;
                case "e-commerce":
                    strategy = "Growth";
                    break;
                default:
                    strategy = "Balanced";
            }
        }
        
        // Simplified risk level mapping
        String riskLevel = "Medium";
        if (strategy.equals("Growth")) {
            riskLevel = "High";
        } else if (sector != null && sector.toLowerCase().contains("bond")) {
            riskLevel = "Low";
        }
        
        return new Product(id, code, name, description, strategy, riskLevel);
    }
} 