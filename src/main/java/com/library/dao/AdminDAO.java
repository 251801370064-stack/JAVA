package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AdminDAO {
    public int count() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM admins");
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }

    public Optional<String> passwordHash(String username) throws SQLException {
        String sql = "SELECT password_hash FROM admins WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(result.getString(1)) : Optional.empty();
            }
        }
    }

    public Admin find(String username) throws SQLException {
        String sql = "SELECT admin_id, username FROM admins WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? new Admin(result.getLong("admin_id"), result.getString("username")) : null;
            }
        }
    }

    public void createFirstAdmin(String username, String passwordHash) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
              try (PreparedStatement lock = connection.prepareStatement("SELECT setting_key FROM settings WHERE setting_key='fine_per_day' FOR UPDATE");
                  ResultSet ignored = lock.executeQuery();
                  PreparedStatement check = connection.prepareStatement("SELECT admin_id FROM admins LIMIT 1 FOR UPDATE");
                  ResultSet existing = check.executeQuery()) {
                if (existing.next()) {
                    throw new SQLException("An administrator already exists. Sign in instead.");
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO admins (username, password_hash) VALUES (?, ?)")) {
                    insert.setString(1, username);
                    insert.setString(2, passwordHash);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}
