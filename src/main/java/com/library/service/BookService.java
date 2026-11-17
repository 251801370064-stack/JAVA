package com.library.service;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class BookService {
    private final BookDAO bookDAO;
    public BookService() { this(new BookDAO()); }
    public BookService(BookDAO bookDAO) { this.bookDAO = bookDAO; }

    public List<Book> search(String query, String sort) throws SQLException { return bookDAO.findAll(query, sort); }
    public List<Book> availableBooks() throws SQLException { return bookDAO.findAvailable(); }
    public Book find(long id) throws SQLException { return bookDAO.findById(id); }
    public int totalCount() throws SQLException { return bookDAO.totalCount(); }
    public int availableCount() throws SQLException { return bookDAO.availableCount(); }

    public void save(Book input) throws Exception {
        String isbn = ValidationUtil.isbn(input.isbn());
        String title = ValidationUtil.required(input.title(), "Title");
        String author = ValidationUtil.required(input.author(), "Author");
        String publisher = input.publisher() == null ? "" : input.publisher().trim();
        String category = input.category() == null ? "" : input.category().trim();
        BigDecimal price = input.price();
        if (price == null || price.signum() < 0 || price.scale() > 2) throw new IllegalArgumentException("Price must be a non-negative amount with up to 2 decimal places.");
        if (input.quantity() < 0) throw new IllegalArgumentException("Quantity cannot be negative.");
        if (input.id() == 0) {
            bookDAO.insert(new Book(0, isbn, title, author, publisher, category, price.setScale(2), input.quantity(), input.quantity()));
            return;
        }
        bookDAO.update(new Book(input.id(), isbn, title, author, publisher, category, price.setScale(2), input.quantity(), 0));
    }

    public void delete(long id) throws SQLException { bookDAO.delete(id); }
}
