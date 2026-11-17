package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.ReportData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    public ReportData run(String report) throws SQLException {
        String sql = switch (report) {
            case "All books" -> "SELECT book_id AS 'Book ID',isbn AS ISBN,title AS Title,author AS Author,publisher AS Publisher,category AS Category,price AS Price,quantity AS Copies,available_quantity AS Available FROM books ORDER BY title";
            case "Available books" -> "SELECT book_id AS 'Book ID',isbn AS ISBN,title AS Title,author AS Author,available_quantity AS Available FROM books WHERE available_quantity>0 ORDER BY title";
            case "Issued books" -> "SELECT i.issue_id AS 'Issue ID',s.student_id AS 'Student ID',s.name AS Student,b.title AS Book,i.issued_on AS Issued,i.due_on AS Due,i.status AS Status FROM issued_books i JOIN students s ON s.student_id=i.student_id JOIN books b ON b.book_id=i.book_id WHERE i.status='ISSUED' ORDER BY i.due_on";
            case "Returned books" -> "SELECT r.return_id AS 'Return ID',r.issue_id AS 'Issue ID',s.student_id AS 'Student ID',s.name AS Student,b.title AS Book,r.returned_on AS Returned,r.overdue_days AS 'Overdue days',r.fine_amount AS Fine FROM returned_books r JOIN students s ON s.student_id=r.student_id JOIN books b ON b.book_id=r.book_id ORDER BY r.returned_on DESC";
            case "Overdue books" -> "SELECT i.issue_id AS 'Issue ID',s.student_id AS 'Student ID',s.name AS Student,s.phone AS Phone,b.title AS Book,i.due_on AS 'Due date',DATEDIFF(CURRENT_DATE,i.due_on) AS 'Days overdue' FROM issued_books i JOIN students s ON s.student_id=i.student_id JOIN books b ON b.book_id=i.book_id WHERE i.status='ISSUED' AND i.due_on<CURRENT_DATE ORDER BY i.due_on";
            case "Registered students" -> "SELECT student_id AS 'Student ID',name AS Name,email AS Email,phone AS Phone,department AS Department,academic_year AS Year,address AS Address FROM students ORDER BY name";
            case "Fine collection" -> "SELECT p.payment_id AS 'Payment ID',f.fine_id AS 'Fine ID',r.issue_id AS 'Issue ID',s.name AS Student,p.amount AS Paid,p.paid_at AS 'Paid at' FROM fine_payments p JOIN fines f ON f.fine_id=p.fine_id JOIN returned_books r ON r.return_id=f.return_id JOIN students s ON s.student_id=r.student_id ORDER BY p.paid_at DESC";
            default -> throw new IllegalArgumentException("Choose a valid report.");
        };
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            ResultSetMetaData metadata = result.getMetaData();
            List<String> columns = new ArrayList<>();
            for (int index = 1; index <= metadata.getColumnCount(); index++) columns.add(metadata.getColumnLabel(index));
            List<Object[]> rows = new ArrayList<>();
            while (result.next()) {
                Object[] row = new Object[metadata.getColumnCount()];
                for (int index = 1; index <= row.length; index++) row[index - 1] = result.getObject(index);
                rows.add(row);
            }
            return new ReportData(List.copyOf(columns), List.copyOf(rows));
        }
    }
}
