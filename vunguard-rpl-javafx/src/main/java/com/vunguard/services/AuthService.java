package com.vunguard.services;

import com.vunguard.models.User;
import com.vunguard.util.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    private static User currentUser;

    /**
     * Mengotentikasi pengguna berdasarkan username dan password.
     * @param username Username pengguna.
     * @param password Password pengguna (plain text).
     * @return true jika otentikasi berhasil, false jika tidak.
     */
   public static boolean authenticate(String username, String password) {
        String sql = "SELECT id, username, password, email, full_name, role, created_at FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    // Set user yang sedang login
                    currentUser = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at").toLocalDateTime().toLocalDate()
                    );
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.close(rs, pstmt, conn);
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}