package com.library.dao;

import com.library.config.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDAO {
    public BigDecimal finePerDay() throws SQLException { return new BigDecimal(get("fine_per_day", "1.00")); }
    public int loanPeriodDays() throws SQLException { return Integer.parseInt(get("loan_period_days", "14")); }

    public void update(BigDecimal finePerDay, int loanDays) throws SQLException {
        if (finePerDay.signum() < 0 || loanDays < 1 || loanDays > 365) throw new IllegalArgumentException("Fine must be non-negative and loan period must be between 1 and 365 days.");
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO settings (setting_key,setting_value) VALUES (?,?) ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)")) {
                statement.setString(1, "fine_per_day"); statement.setString(2, finePerDay.toPlainString()); statement.executeUpdate();
                statement.setString(1, "loan_period_days"); statement.setString(2, Integer.toString(loanDays)); statement.executeUpdate();
                connection.commit();
            } catch (SQLException exception) { connection.rollback(); throw exception; }
        }
    }

    private String get(String key, String fallback) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT setting_value FROM settings WHERE setting_key=?")) {
            statement.setString(1, key);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? result.getString(1) : fallback; }
        }
    }
}
