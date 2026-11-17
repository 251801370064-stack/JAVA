package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private static final String COLUMNS = "book_id, isbn, title, author, publisher, category, price, quantity, available_quantity";

    public List<Book> findAll(String query, String sort) throws SQLException {
        String order = switch (sort == null ? "title" : sort) {
            case "author" -> "author";
            case "available" -> "available_quantity DESC";
            case "newest" -> "book_id DESC";
            default -> "title";
        };
        String sql = "SELECT " + COLUMNS + " FROM books WHERE (? = '' OR title LIKE ? OR author LIKE ? OR isbn LIKE ? OR CAST(book_id AS CHAR) LIKE ?) ORDER BY " + order;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + (query == null ? "" : query.trim()) + "%";
            statement.setString(1, query == null ? "" : query.trim());
            for (int index = 2; index <= 5; index++) statement.setString(index, pattern);
            try (ResultSet result = statement.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (result.next()) books.add(map(result));
                return books;
            }
        }
    }

    public List<Book> findAvailable() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM books WHERE available_quantity > 0 ORDER BY title");
             ResultSet result = statement.executeQuery()) {
            List<Book> books = new ArrayList<>();
            while (result.next()) books.add(map(result));
            return books;
        }
    }

    public Book findById(long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM books WHERE book_id = ?")) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? map(result) : null; }
        }
    }

    public void insert(Book book) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, publisher, category, price, quantity, available_quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, book, false);
            statement.executeUpdate();
        }
    }

    public void update(Book book) throws SQLException {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, publisher=?, category=?, price=?, quantity=?, available_quantity=? WHERE book_id=?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            try {
                int checkedOut;
                try (PreparedStatement lock = connection.prepareStatement("SELECT quantity,available_quantity FROM books WHERE book_id=? FOR UPDATE")) {
                    lock.setLong(1, book.id());
                    try (ResultSet result = lock.executeQuery()) {
                        if (!result.next()) throw new SQLException("Book was not found.");
                        checkedOut = result.getInt("quantity") - result.getInt("available_quantity");
                    }
                }
                if (book.quantity() < checkedOut) throw new SQLException("Total quantity cannot be less than the " + checkedOut + " copies currently issued.");
                Book adjusted = new Book(book.id(), book.isbn(), book.title(), book.author(), book.publisher(), book.category(), book.price(), book.quantity(), book.quantity() - checkedOut);
                bind(statement, adjusted, true);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM books WHERE book_id=?")) {
            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) throw new SQLException("Book was not found.");
        }
    }

    public int totalCount() throws SQLException { return count("SELECT COUNT(*) FROM books"); }
    public int availableCount() throws SQLException { return count("SELECT COALESCE(SUM(available_quantity), 0) FROM books"); }

    private int count(String sql) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            result.next(); return result.getInt(1);
        }
    }

    private void bind(PreparedStatement statement, Book book, boolean update) throws SQLException {
        statement.setString(1, book.isbn()); statement.setString(2, book.title()); statement.setString(3, book.author());
        statement.setString(4, book.publisher()); statement.setString(5, book.category()); statement.setBigDecimal(6, book.price());
        statement.setInt(7, book.quantity()); statement.setInt(8, book.availableQuantity());
        if (update) statement.setLong(9, book.id());
    }

    private Book map(ResultSet result) throws SQLException {
        return new Book(result.getLong("book_id"), result.getString("isbn"), result.getString("title"),
                result.getString("author"), result.getString("publisher"), result.getString("category"),
                result.getBigDecimal("price"), result.getInt("quantity"), result.getInt("available_quantity"));
    }
}
