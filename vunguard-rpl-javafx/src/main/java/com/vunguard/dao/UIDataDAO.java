package com.vunguard.dao;

import com.vunguard.util.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIDataDAO {

    // Class helper untuk menyimpan data produk dalam portofolio
    public static class PortfolioAssetInfo {
        public final int assetId;
        public final String productName;
        public PortfolioAssetInfo(int assetId, String productName) { this.assetId = assetId; this.productName = productName; }
        @Override public String toString() { return productName; } // Agar nama tampil di ComboBox
    }

    // Class helper untuk menyimpan data portofolio
    public static class PortfolioInfo {
        public final int portfolioId;
        public final String portfolioName;
        public final List<PortfolioAssetInfo> assets;
        public PortfolioInfo(int portfolioId, String portfolioName) { this.portfolioId = portfolioId; this.portfolioName = portfolioName; this.assets = new ArrayList<>(); }
        @Override public String toString() { return portfolioName; } // Agar nama tampil di ComboBox
    }

    /**
     * Mengambil daftar portofolio beserta aset di dalamnya untuk ID akun tertentu.
     */
    public List<PortfolioInfo> getPortfolioAndAssetDataForAccount(int accountId) {
        Map<Integer, PortfolioInfo> portfolioMap = new HashMap<>();
        String query = "SELECT p.id as portfolio_id, p.name as portfolio_name, a.id as asset_id, pr.name as product_name " +
                       "FROM portfolios p " +
                       "LEFT JOIN assets a ON p.id = a.portfolio_id " +
                       "LEFT JOIN products pr ON a.product_id = pr.id " +
                       "WHERE p.account_id = ? ORDER BY p.name, pr.name";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int portfolioId = rs.getInt("portfolio_id");
                PortfolioInfo portfolioInfo = portfolioMap.computeIfAbsent(portfolioId, id -> new PortfolioInfo(id, rs.getString("portfolio_name")));
                int assetId = rs.getInt("asset_id");
                if (!rs.wasNull()) {
                    portfolioInfo.assets.add(new PortfolioAssetInfo(assetId, rs.getString("product_name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(portfolioMap.values());
    }
}