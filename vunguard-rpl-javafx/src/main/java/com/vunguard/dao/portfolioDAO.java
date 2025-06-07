package com.vunguard.dao;

import com.vunguard.models.Portfolio;
import com.vunguard.util.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PortfolioDAO {

    /**
     * Mengambil daftar portofolio untuk ID akun tertentu dari database.
     * @param accountId ID dari akun pengguna.
     * @return ObservableList dari objek Portfolio.
     */
    public ObservableList<Portfolio> getPortfoliosForAccount(int accountId) {
        ObservableList<Portfolio> portfolios = FXCollections.observableArrayList();
        // Query ini diadaptasi dari lib/db/models/portfolio.ts
        String query = "SELECT " +
                       "  p.id, p.name, p.description, " +
                       "  COALESCE(lp.total_value, 0) as total_value, " +
                       "  COALESCE(lp.return_percentage, 0) as return_percentage, " +
                       "  COALESCE(ac.asset_count, 0) as asset_count " +
                       "FROM portfolios p " +
                       "LEFT JOIN ( " +
                       "  SELECT portfolio_id, value as total_value, return_percentage " +
                       "  FROM performances " +
                       "  WHERE (portfolio_id, date) IN ( " +
                       "    SELECT portfolio_id, MAX(date) " +
                       "    FROM performances " +
                       "    WHERE asset_id IS NULL " +
                       "    GROUP BY portfolio_id " +
                       "  ) AND asset_id IS NULL " +
                       ") lp ON p.id = lp.portfolio_id " +
                       "LEFT JOIN ( " +
                       "  SELECT portfolio_id, COUNT(*) as asset_count " +
                       "  FROM assets " +
                       "  GROUP BY portfolio_id " +
                       ") ac ON p.id = ac.portfolio_id " +
                       "WHERE p.account_id = ? " +
                       "ORDER BY p.name ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                portfolios.add(new Portfolio(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("name"),
                    rs.getDouble("total_value"),
                    0.0, // Saldo tunai tidak tersedia dalam query ini
                    rs.getDouble("return_percentage"),
                    rs.getInt("asset_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error, misalnya dengan menampilkan dialog
        }
        return portfolios;
    }
}