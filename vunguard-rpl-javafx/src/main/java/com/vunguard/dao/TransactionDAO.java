package com.vunguard.dao;

import com.vunguard.models.Transaction;
import com.vunguard.util.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class TransactionDAO {

    /**
     * Mengambil semua transaksi untuk ID akun tertentu.
     */
    public ObservableList<Transaction> getTransactionsForAccount(int accountId) {
        ObservableList<Transaction> transactions = FXCollections.observableArrayList();
        // Query ini diadaptasi dari lib/db/models/transaction.ts
        String query = "SELECT t.id, t.transaction_type, t.quantity, t.price, t.transaction_date, " +
                       "p.name as portfolio_name, pr.name as product_name " +
                       "FROM transactions t " +
                       "JOIN portfolios p ON t.portfolio_id = p.id " +
                       "JOIN assets a ON t.asset_id = a.id " +
                       "JOIN products pr ON a.product_id = pr.id " +
                       "WHERE p.account_id = ? ORDER BY t.transaction_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                    "TRX-" + rs.getInt("id"),
                    rs.getString("transaction_type"),
                    rs.getString("portfolio_name"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getTimestamp("transaction_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Membuat transaksi baru di database.
     */
    public boolean createTransaction(int portfolioId, int assetId, String type, int quantity, double price, LocalDateTime date, String notes) {
        String sql = "INSERT INTO transactions (portfolio_id, asset_id, transaction_type, quantity, price, transaction_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, portfolioId);
            pstmt.setInt(2, assetId);
            pstmt.setString(3, type);
            pstmt.setInt(4, quantity);
            pstmt.setDouble(5, price);
            pstmt.setTimestamp(6, Timestamp.valueOf(date));
            pstmt.setString(7, notes);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}