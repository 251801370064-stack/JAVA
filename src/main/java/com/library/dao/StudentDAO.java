package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private static final String COLUMNS = "student_id, name, email, phone, department, academic_year, address";

    public List<Student> findAll(String query) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM students WHERE (? = '' OR student_id LIKE ? OR name LIKE ? OR email LIKE ?) ORDER BY name";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            String value = query == null ? "" : query.trim(); String pattern = "%" + value + "%";
            statement.setString(1, value); statement.setString(2, pattern); statement.setString(3, pattern); statement.setString(4, pattern);
            try (ResultSet result = statement.executeQuery()) {
                List<Student> students = new ArrayList<>();
                while (result.next()) students.add(map(result));
                return students;
            }
        }
    }

    public List<Student> findAll() throws SQLException { return findAll(""); }

    public Student findById(String id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM students WHERE student_id=?")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? map(result) : null; }
        }
    }

    public void insert(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_id,name,email,phone,department,academic_year,address) VALUES (?,?,?,?,?,?,?)";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, student); statement.executeUpdate();
        }
    }

    public void update(Student student) throws SQLException {
        String sql = "UPDATE students SET name=?,email=?,phone=?,department=?,academic_year=?,address=? WHERE student_id=?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.name()); statement.setString(2, student.email()); statement.setString(3, student.phone());
            statement.setString(4, student.department()); statement.setString(5, student.academicYear()); statement.setString(6, student.address());
            statement.setString(7, student.id()); if (statement.executeUpdate() == 0) throw new SQLException("Student was not found.");
        }
    }

    public void delete(String id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM students WHERE student_id=?")) {
            statement.setString(1, id); if (statement.executeUpdate() == 0) throw new SQLException("Student was not found.");
        }
    }

    public int count() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM students"); ResultSet result = statement.executeQuery()) {
            result.next(); return result.getInt(1);
        }
    }

    private void bind(PreparedStatement statement, Student student) throws SQLException {
        statement.setString(1, student.id()); statement.setString(2, student.name()); statement.setString(3, student.email());
        statement.setString(4, student.phone()); statement.setString(5, student.department()); statement.setString(6, student.academicYear()); statement.setString(7, student.address());
    }

    private Student map(ResultSet result) throws SQLException {
        return new Student(result.getString("student_id"), result.getString("name"), result.getString("email"),
                result.getString("phone"), result.getString("department"), result.getString("academic_year"), result.getString("address"));
    }
}
