package com.vunguard.dao;

import com.vunguard.models.User;
import com.vunguard.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAO {

    /**
     * Mengambil semua akun pengguna dari database.
     * @return ObservableList dari objek User.
     */

    public ObservableList<User> getAccounts() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT id, username, full_name, email, role, created_at FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getTimestamp("created_at").toLocalDateTime().toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Memperbarui data akun pengguna di database.
     * @param user Objek User dengan data yang telah diperbarui.
     * @param newPassword Password baru (opsional, bisa null atau kosong jika tidak diubah).
     * @return true jika berhasil, false jika gagal.
     */
    public boolean updateAccount(User user, String newPassword) {
        StringBuilder sql = new StringBuilder("UPDATE users SET username = ?, full_name = ?, email = ?, role = ?");
        boolean passwordChanged = newPassword != null && !newPassword.isEmpty();
        
        if (passwordChanged) {
            sql.append(", password = ?");
        }
        sql.append(" WHERE id = ?");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole());

            int parameterIndex = 5;
            if (passwordChanged) {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                pstmt.setString(parameterIndex++, hashedPassword);
            }
            
            pstmt.setInt(parameterIndex, user.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Menghapus akun pengguna dari database berdasarkan ID.
     * @param userId ID pengguna yang akan dihapus.
     * @return true jika berhasil, false jika gagal.
     */
    public boolean deleteAccount(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<Integer> findUserIdByUsername(String username) {
        String sql = "SELECT id FROM accounts WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}