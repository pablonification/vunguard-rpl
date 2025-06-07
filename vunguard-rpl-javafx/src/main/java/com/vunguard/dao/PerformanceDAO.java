package com.vunguard.dao;

import com.vunguard.util.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PerformanceDAO {

    // Data class untuk tabel perbandingan benchmark
    public static class BenchmarkData {
        private final String portfolioName;
        private final double currentValue;
        private final double returnPercent;
        private final double benchmarkPercent;

        public BenchmarkData(String name, double currentVal, double returnPct, double benchmarkPct) {
            this.portfolioName = name;
            this.currentValue = currentVal;
            this.returnPercent = returnPct;
            this.benchmarkPercent = benchmarkPct;
        }
        // Getters
        public String getPortfolioName() { return portfolioName; }
        public double getCurrentValue() { return currentValue; }
        public double getReturnPercent() { return returnPercent; }
        public double getBenchmarkPercent() { return benchmarkPercent; }
        public double getDifference() { return returnPercent - benchmarkPercent; }
    }

    /**
     * Mengambil data untuk tabel perbandingan benchmark.
     */
    public ObservableList<BenchmarkData> getPortfolioPerformances(int accountId) {
        ObservableList<BenchmarkData> data = FXCollections.observableArrayList();
        String query = "SELECT p.name, " +
                       "COALESCE(SUM(a.quantity * a.current_price), 0) as current_value, " +
                       "COALESCE((SELECT return_percentage FROM performances WHERE portfolio_id = p.id AND asset_id IS NULL ORDER BY date DESC LIMIT 1), 0) as return_percentage, " +
                       "COALESCE((SELECT benchmark_comparison FROM performances WHERE portfolio_id = p.id AND asset_id IS NULL ORDER BY date DESC LIMIT 1), 0) as benchmark_comparison " +
                       "FROM portfolios p LEFT JOIN assets a ON p.id = a.portfolio_id " +
                       "WHERE p.account_id = ? GROUP BY p.id, p.name ORDER BY p.name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                data.add(new BenchmarkData(rs.getString("name"), rs.getDouble("current_value"), rs.getDouble("return_percentage"), rs.getDouble("benchmark_comparison")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    /**
     * Mengambil data historis nilai portofolio untuk grafik line chart.
     */
    public ObservableList<XYChart.Data<String, Number>> getPerformanceOverTime(int accountId) {
        ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();
        String query = "SELECT date, SUM(value) as total_value FROM performances " +
                       "WHERE portfolio_id IN (SELECT id FROM portfolios WHERE account_id = ?) AND asset_id IS NULL " +
                       "GROUP BY date ORDER BY date ASC";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                data.add(new XYChart.Data<>(date.format(formatter), rs.getDouble("total_value")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    /**
     * Mengambil data alokasi aset untuk pie chart.
     */
    public ObservableList<PieChart.Data> getAssetAllocation(int accountId) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        String query = "SELECT pr.name, SUM(a.quantity * a.current_price) as value FROM assets a " +
                       "JOIN products pr ON a.product_id = pr.id " +
                       "JOIN portfolios p ON a.portfolio_id = p.id " +
                       "WHERE p.account_id = ? GROUP BY pr.name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                data.add(new PieChart.Data(rs.getString("name"), rs.getDouble("value")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    /**
     * Mengambil data alokasi risiko untuk pie chart.
     */
    public ObservableList<PieChart.Data> getRiskAllocation(int accountId) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        String query = "SELECT pr.risk_level, SUM(a.quantity * a.current_price) as value FROM assets a " +
                       "JOIN products pr ON a.product_id = pr.id " +
                       "JOIN portfolios p ON a.portfolio_id = p.id " +
                       "WHERE p.account_id = ? GROUP BY pr.risk_level";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                data.add(new PieChart.Data(rs.getString("risk_level"), rs.getDouble("value")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }
}