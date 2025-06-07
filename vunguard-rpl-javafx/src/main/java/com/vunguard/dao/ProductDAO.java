package com.vunguard.dao;

import com.vunguard.models.Product;
import com.vunguard.util.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProductDAO {

    /**
     * Mengambil semua produk dari database, diurutkan berdasarkan nama.
     * @return ObservableList yang berisi objek-objek Product.
     */
    public ObservableList<Product> getProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT id, code, name, description, investment_strategy, risk_level FROM products ORDER BY name ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(new Product(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("investment_strategy"),
                    rs.getString("risk_level")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Membuat produk baru di database.
     * @return true jika produk berhasil dibuat, false jika gagal.
     */
    public boolean createProduct(String code, String name, String description, String strategy, String riskLevel) {
        String sql = "INSERT INTO products (code, name, description, investment_strategy, risk_level) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            pstmt.setString(2, name);
            pstmt.setString(3, description);
            pstmt.setString(4, strategy);
            pstmt.setString(5, riskLevel);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Handle kemungkinan error duplikasi kode produk
            if (e.getSQLState().equals("23505")) { // Kode error PostgreSQL untuk 'unique_violation'
                System.err.println("Error: Product code '" + code + "' already exists.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }
}