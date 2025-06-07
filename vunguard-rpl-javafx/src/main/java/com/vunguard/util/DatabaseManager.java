package com.vunguard.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // Connection string didapat dari file backend lib/db/models/account.ts
    private static final String HOST = "ep-late-queen-a1p6lfmz-pooler.ap-southeast-1.aws.neon.tech";
    private static final String DATABASE = "neondb";
    private static final String USER = "neondb_owner";
    // PENTING: Di aplikasi production, password harus disimpan dengan aman, tidak di-hardcode.
    private static final String PASSWORD = "npg_0DtOI4YdeNJl";
    private static final String JDBC_URL = "jdbc:postgresql://" + HOST + "/" + DATABASE + "?sslmode=require";

    /**
     * Mendapatkan koneksi ke database.
     * @return Objek Connection yang aktif.
     * @throws SQLException Jika koneksi gagal.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Mendaftarkan driver PostgreSQL
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC PostgreSQL tidak ditemukan.");
            e.printStackTrace();
            throw new SQLException("Driver database tidak ditemukan", e);
        }
    }

    /**
     * Menutup resource JDBC dengan aman.
     * @param resources Resource yang akan ditutup (Connection, PreparedStatement, ResultSet).
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Abaikan error saat menutup resource
                    e.printStackTrace();
                }
            }
        }
    }
}