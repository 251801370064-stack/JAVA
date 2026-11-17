package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.Fine;
import com.library.model.FinePayment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {
    public List<Fine> findAll(String query, String filter) throws SQLException {
        String statusClause = switch (filter == null ? "All" : filter) {
            case "Pending" -> " AND f.status IN ('PENDING','PARTIAL')";
            case "Paid" -> " AND f.status='PAID'";
            default -> "";
        };
        String sql = "SELECT f.fine_id,f.return_id,r.issue_id,r.student_id,s.name AS student_name,b.title AS book_title,f.amount,f.paid_amount,f.status,r.returned_on "
                + "FROM fines f JOIN returned_books r ON r.return_id=f.return_id JOIN students s ON s.student_id=r.student_id JOIN books b ON b.book_id=r.book_id "
                + "WHERE (?='' OR s.name LIKE ? OR s.student_id LIKE ? OR b.title LIKE ? OR CAST(r.issue_id AS CHAR) LIKE ?)" + statusClause + " ORDER BY r.returned_on DESC";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            String value = query == null ? "" : query.trim(); String pattern = "%" + value + "%";
            statement.setString(1, value); for (int index = 2; index <= 5; index++) statement.setString(index, pattern);
            try (ResultSet result = statement.executeQuery()) {
                List<Fine> fines = new ArrayList<>(); while (result.next()) fines.add(map(result)); return fines;
            }
        }
    }

    public void recordPayment(long fineId, BigDecimal payment) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal amount; BigDecimal paid;
                try (PreparedStatement statement = connection.prepareStatement("SELECT amount,paid_amount FROM fines WHERE fine_id=? FOR UPDATE")) {
                    statement.setLong(1, fineId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new SQLException("Fine record was not found.");
                        amount = result.getBigDecimal("amount"); paid = result.getBigDecimal("paid_amount");
                    }
                }
                if (payment.signum() <= 0 || payment.compareTo(amount.subtract(paid)) > 0) throw new SQLException("Payment must be greater than zero and no more than the outstanding balance.");
                try (PreparedStatement insert = connection.prepareStatement("INSERT INTO fine_payments (fine_id,amount) VALUES (?,?)")) {
                    insert.setLong(1, fineId); insert.setBigDecimal(2, payment); insert.executeUpdate();
                }
                BigDecimal totalPaid = paid.add(payment);
                String status = totalPaid.compareTo(amount) >= 0 ? "PAID" : "PARTIAL";
                try (PreparedStatement update = connection.prepareStatement("UPDATE fines SET paid_amount=?,status=?,paid_at=CURRENT_TIMESTAMP WHERE fine_id=?")) {
                    update.setBigDecimal(1, totalPaid); update.setString(2, status); update.setLong(3, fineId); update.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback(); throw exception;
            }
        }
    }

    public List<FinePayment> paymentHistory() throws SQLException {
        String sql = "SELECT p.payment_id,p.fine_id,r.issue_id,s.name AS student_name,p.amount,p.paid_at FROM fine_payments p "
                + "JOIN fines f ON f.fine_id=p.fine_id JOIN returned_books r ON r.return_id=f.return_id JOIN students s ON s.student_id=r.student_id ORDER BY p.paid_at DESC";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            List<FinePayment> payments = new ArrayList<>();
            while (result.next()) {
                Timestamp timestamp = result.getTimestamp("paid_at");
                payments.add(new FinePayment(result.getLong("payment_id"), result.getLong("fine_id"), result.getLong("issue_id"), result.getString("student_name"), result.getBigDecimal("amount"), timestamp == null ? LocalDateTime.now() : timestamp.toLocalDateTime()));
            }
            return payments;
        }
    }

    public BigDecimal totalCollected() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM fine_payments"); ResultSet result = statement.executeQuery()) {
            result.next(); return result.getBigDecimal(1);
        }
    }

    private Fine map(ResultSet result) throws SQLException {
        return new Fine(result.getLong("fine_id"), result.getLong("return_id"), result.getLong("issue_id"), result.getString("student_id"), result.getString("student_name"), result.getString("book_title"), result.getBigDecimal("amount"), result.getBigDecimal("paid_amount"), result.getString("status"), result.getDate("returned_on").toLocalDate());
    }
}
