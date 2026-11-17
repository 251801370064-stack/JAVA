package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.ReturnedBook;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ReturnDAO {
    public ReturnedBook returnBook(long issueId, LocalDate returnedOn, BigDecimal finePerDay) throws SQLException {
        String select = "SELECT i.student_id,s.name AS student_name,i.book_id,b.title AS book_title,i.due_on,i.status "
                + "FROM issued_books i JOIN students s ON s.student_id=i.student_id JOIN books b ON b.book_id=i.book_id WHERE i.issue_id=? FOR UPDATE";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String studentId; String studentName; long bookId; String bookTitle; LocalDate dueOn;
                try (PreparedStatement statement = connection.prepareStatement(select)) {
                    statement.setLong(1, issueId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new SQLException("Issue record was not found.");
                        if (!"ISSUED".equals(result.getString("status"))) throw new SQLException("This book has already been returned.");
                        studentId = result.getString("student_id"); studentName = result.getString("student_name");
                        bookId = result.getLong("book_id"); bookTitle = result.getString("book_title"); dueOn = result.getDate("due_on").toLocalDate();
                    }
                }
                int overdueDays = (int) Math.max(0, ChronoUnit.DAYS.between(dueOn, returnedOn));
                BigDecimal fineAmount = finePerDay.multiply(BigDecimal.valueOf(overdueDays)).setScale(2);
                try (PreparedStatement update = connection.prepareStatement("UPDATE issued_books SET status='RETURNED',returned_on=? WHERE issue_id=? AND status='ISSUED'")) {
                    update.setDate(1, Date.valueOf(returnedOn)); update.setLong(2, issueId);
                    if (update.executeUpdate() != 1) throw new SQLException("Issue status changed. Refresh and retry.");
                }
                try (PreparedStatement update = connection.prepareStatement("UPDATE books SET available_quantity=available_quantity+1 WHERE book_id=? AND available_quantity<quantity")) {
                    update.setLong(1, bookId); if (update.executeUpdate() != 1) throw new SQLException("Book inventory is inconsistent; return was cancelled.");
                }
                long returnId;
                try (PreparedStatement insert = connection.prepareStatement("INSERT INTO returned_books (issue_id,student_id,book_id,returned_on,overdue_days,fine_amount) VALUES (?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                    insert.setLong(1, issueId); insert.setString(2, studentId); insert.setLong(3, bookId); insert.setDate(4, Date.valueOf(returnedOn)); insert.setInt(5, overdueDays); insert.setBigDecimal(6, fineAmount); insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Could not save return history."); returnId = keys.getLong(1); }
                }
                try (PreparedStatement insert = connection.prepareStatement("INSERT INTO fines (return_id,amount,paid_amount,status) VALUES (?,?,0,?)")) {
                    insert.setLong(1, returnId); insert.setBigDecimal(2, fineAmount); insert.setString(3, fineAmount.signum() == 0 ? "PAID" : "PENDING"); insert.executeUpdate();
                }
                connection.commit();
                return new ReturnedBook(returnId, issueId, studentId, studentName, bookId, bookTitle, returnedOn, overdueDays, fineAmount);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback(); throw exception;
            }
        }
    }

    public List<ReturnedBook> findAll(String query) throws SQLException {
        String sql = "SELECT r.return_id,r.issue_id,r.student_id,s.name AS student_name,r.book_id,b.title AS book_title,r.returned_on,r.overdue_days,r.fine_amount "
                + "FROM returned_books r JOIN students s ON s.student_id=r.student_id JOIN books b ON b.book_id=r.book_id "
                + "WHERE (?='' OR s.name LIKE ? OR s.student_id LIKE ? OR b.title LIKE ? OR CAST(r.issue_id AS CHAR) LIKE ?) ORDER BY r.returned_on DESC";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            String value = query == null ? "" : query.trim(); String pattern = "%" + value + "%";
            statement.setString(1, value); for (int index = 2; index <= 5; index++) statement.setString(index, pattern);
            try (ResultSet result = statement.executeQuery()) {
                List<ReturnedBook> records = new ArrayList<>();
                while (result.next()) records.add(new ReturnedBook(result.getLong("return_id"), result.getLong("issue_id"), result.getString("student_id"), result.getString("student_name"), result.getLong("book_id"), result.getString("book_title"), result.getDate("returned_on").toLocalDate(), result.getInt("overdue_days"), result.getBigDecimal("fine_amount")));
                return records;
            }
        }
    }
}
