package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.IssuedBook;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IssueDAO {
    public long issue(String studentId, long bookId, LocalDate issuedOn, LocalDate dueOn) throws SQLException {
        String lockBook = "SELECT available_quantity FROM books WHERE book_id=? FOR UPDATE";
        String addIssue = "INSERT INTO issued_books (student_id,book_id,issued_on,due_on) VALUES (?,?,?,?)";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int available;
                try (PreparedStatement lock = connection.prepareStatement(lockBook)) {
                    lock.setLong(1, bookId);
                    try (ResultSet result = lock.executeQuery()) {
                        if (!result.next()) throw new SQLException("Selected book was not found.");
                        available = result.getInt(1);
                    }
                }
                if (available < 1) throw new SQLException("This book has no available copies.");
                try (PreparedStatement update = connection.prepareStatement("UPDATE books SET available_quantity=available_quantity-1 WHERE book_id=? AND available_quantity>0")) {
                    update.setLong(1, bookId);
                    if (update.executeUpdate() != 1) throw new SQLException("This book has no available copies.");
                }
                long id;
                try (PreparedStatement insert = connection.prepareStatement(addIssue, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    insert.setString(1, studentId); insert.setLong(2, bookId); insert.setDate(3, Date.valueOf(issuedOn)); insert.setDate(4, Date.valueOf(dueOn));
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Could not generate an issue ID."); id = keys.getLong(1); }
                }
                connection.commit();
                return id;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public List<IssuedBook> findActive(String query, boolean overdueOnly) throws SQLException {
        String sql = "SELECT i.issue_id,i.student_id,s.name AS student_name,i.book_id,b.title AS book_title,i.issued_on,i.due_on,i.status,i.returned_on "
                + "FROM issued_books i JOIN students s ON s.student_id=i.student_id JOIN books b ON b.book_id=i.book_id "
                + "WHERE i.status='ISSUED' AND (?='' OR s.name LIKE ? OR s.student_id LIKE ? OR b.title LIKE ? OR CAST(i.issue_id AS CHAR) LIKE ?) "
                + (overdueOnly ? "AND i.due_on<CURRENT_DATE " : "") + "ORDER BY i.due_on, i.issue_id";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            String value = query == null ? "" : query.trim(); String pattern = "%" + value + "%";
            statement.setString(1, value); for (int index = 2; index <= 5; index++) statement.setString(index, pattern);
            try (ResultSet result = statement.executeQuery()) {
                List<IssuedBook> records = new ArrayList<>(); while (result.next()) records.add(map(result)); return records;
            }
        }
    }

    public List<IssuedBook> findHistory(String query) throws SQLException {
        String sql = "SELECT i.issue_id,i.student_id,s.name AS student_name,i.book_id,b.title AS book_title,i.issued_on,i.due_on,i.status,i.returned_on "
                + "FROM issued_books i JOIN students s ON s.student_id=i.student_id JOIN books b ON b.book_id=i.book_id "
                + "WHERE (?='' OR s.name LIKE ? OR s.student_id LIKE ? OR b.title LIKE ? OR CAST(i.issue_id AS CHAR) LIKE ?) ORDER BY i.issued_on DESC";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            String value = query == null ? "" : query.trim(); String pattern = "%" + value + "%";
            statement.setString(1, value); for (int index = 2; index <= 5; index++) statement.setString(index, pattern);
            try (ResultSet result = statement.executeQuery()) {
                List<IssuedBook> records = new ArrayList<>(); while (result.next()) records.add(map(result)); return records;
            }
        }
    }

    public int activeCount() throws SQLException { return count("SELECT COUNT(*) FROM issued_books WHERE status='ISSUED'"); }
    public int overdueCount() throws SQLException { return count("SELECT COUNT(*) FROM issued_books WHERE status='ISSUED' AND due_on<CURRENT_DATE"); }

    private int count(String sql) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) { result.next(); return result.getInt(1); }
    }

    private IssuedBook map(ResultSet result) throws SQLException {
        Date returned = result.getDate("returned_on");
        return new IssuedBook(result.getLong("issue_id"), result.getString("student_id"), result.getString("student_name"),
                result.getLong("book_id"), result.getString("book_title"), result.getDate("issued_on").toLocalDate(),
                result.getDate("due_on").toLocalDate(), result.getString("status"), returned == null ? null : returned.toLocalDate());
    }
}
