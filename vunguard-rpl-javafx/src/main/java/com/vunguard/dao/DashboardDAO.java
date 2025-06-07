package com.vunguard.dao;

import com.vunguard.util.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DashboardDAO {

    public Map<String, Number> getDashboardSummary(int accountId) {
        Map<String, Number> summary = new HashMap<>();
        
        // Query diadaptasi dari lib/db/models/dashboard.ts
        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Get total value and average return
            String valueQuery = "SELECT COALESCE(SUM(value), 0) as total_value, COALESCE(AVG(return_percentage), 0) as avg_return " +
                                "FROM performances p JOIN portfolios pf ON p.portfolio_id = pf.id " +
                                "WHERE p.asset_id IS NULL AND pf.account_id = ? AND p.date = " +
                                "(SELECT MAX(date) FROM performances p2 WHERE p2.portfolio_id = p.portfolio_id AND p2.asset_id IS NULL)";
            try (PreparedStatement pstmt = conn.prepareStatement(valueQuery)) {
                pstmt.setInt(1, accountId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    summary.put("totalValue", rs.getDouble("total_value"));
                    summary.put("averageReturn", rs.getDouble("avg_return"));
                }
            }

            // 2. Get active products
            String productsQuery = "SELECT COUNT(DISTINCT a.product_id) as active_products FROM assets a JOIN portfolios p ON a.portfolio_id = p.id WHERE p.account_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(productsQuery)) {
                pstmt.setInt(1, accountId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) summary.put("activeProducts", rs.getInt("active_products"));
            }

            // 3. Get recent transactions (last 30 days)
            String transQuery = "SELECT COUNT(*) as recent_transactions FROM transactions t JOIN portfolios p ON t.portfolio_id = p.id WHERE p.account_id = ? AND t.transaction_date >= NOW() - INTERVAL '1 month'";
            try (PreparedStatement pstmt = conn.prepareStatement(transQuery)) {
                pstmt.setInt(1, accountId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) summary.put("recentTransactions", rs.getInt("recent_transactions"));
            }
            
            // Monthly change logic can be added here if needed

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }
}