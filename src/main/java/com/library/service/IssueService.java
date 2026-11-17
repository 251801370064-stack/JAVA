package com.library.service;

import com.library.dao.IssueDAO;
import com.library.dao.SettingsDAO;
import com.library.dao.StudentDAO;
import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.model.IssuedBook;
import com.library.model.Student;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class IssueService {
    private final IssueDAO issueDAO;
    private final BookDAO bookDAO;
    private final StudentDAO studentDAO;
    private final SettingsDAO settingsDAO;
    public IssueService() { this(new IssueDAO(), new BookDAO(), new StudentDAO(), new SettingsDAO()); }
    public IssueService(IssueDAO issueDAO, BookDAO bookDAO, StudentDAO studentDAO, SettingsDAO settingsDAO) {
        this.issueDAO = issueDAO; this.bookDAO = bookDAO; this.studentDAO = studentDAO; this.settingsDAO = settingsDAO;
    }

    public long issue(String studentId, long bookId) throws Exception {
        Student student = studentDAO.findById(studentId);
        if (student == null) throw new IllegalArgumentException("Select a registered student.");
        Book book = bookDAO.findById(bookId);
        if (book == null || book.availableQuantity() < 1) throw new IllegalArgumentException("The selected book is not available.");
        LocalDate today = LocalDate.now();
        return issueDAO.issue(studentId, bookId, today, today.plusDays(settingsDAO.loanPeriodDays()));
    }

    public List<IssuedBook> active(String query, boolean overdueOnly) throws SQLException { return issueDAO.findActive(query, overdueOnly); }
    public List<IssuedBook> history(String query) throws SQLException { return issueDAO.findHistory(query); }
    public int activeCount() throws SQLException { return issueDAO.activeCount(); }
    public int overdueCount() throws SQLException { return issueDAO.overdueCount(); }
}
