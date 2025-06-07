package com.vunguard.dao;

import com.vunguard.models.Recommendation;
import com.vunguard.util.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class RecommendationDAO {

    /**
     * Menyimpan rekomendasi baru ke database.
     */
    public boolean createRecommendation(Recommendation rec) {
        String sql = "INSERT INTO recommendations (id, product_name, analyst_name, recommendation_type, target_price, current_price, confidence, status, timeframe, rationale, technical_analysis, fundamental_analysis, risks, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rec.getId());
            pstmt.setString(2, rec.getProductName());
            pstmt.setString(3, rec.getAnalystName());
            pstmt.setString(4, rec.getType());
            pstmt.setDouble(5, rec.getTargetPrice());
            pstmt.setDouble(6, rec.getCurrentPrice());
            pstmt.setInt(7, rec.getConfidence());
            pstmt.setString(8, rec.getStatus());
            pstmt.setString(9, rec.getTimeframe());
            pstmt.setString(10, rec.getRationale());
            pstmt.setString(11, rec.getTechnicalAnalysis());
            pstmt.setString(12, rec.getFundamentalAnalysis());
            pstmt.setString(13, rec.getRisks());
            pstmt.setTimestamp(14, Timestamp.valueOf(rec.getCreated()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mengambil semua rekomendasi (untuk Manajer).
     */
    public ObservableList<Recommendation> getAllRecommendations() {
        ObservableList<Recommendation> recommendations = FXCollections.observableArrayList();
        String sql = "SELECT * FROM recommendations ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                recommendations.add(mapRowToRecommendation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recommendations;
    }

    /**
     * Mengubah status rekomendasi (untuk persetujuan/penolakan).
     */
    public boolean updateStatus(String recommendationId, String newStatus) {
        String sql = "UPDATE recommendations SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(3, recommendationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Recommendation mapRowToRecommendation(ResultSet rs) throws SQLException {
        return new Recommendation(
            rs.getString("id"),
            rs.getString("product_name"),
            rs.getString("analyst_name"),
            rs.getString("recommendation_type"),
            rs.getDouble("target_price"),
            rs.getDouble("current_price"),
            rs.getInt("confidence"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getString("timeframe"),
            rs.getString("rationale"),
            rs.getString("technical_analysis"),
            rs.getString("fundamental_analysis"),
            rs.getString("risks")
        );
    }
}