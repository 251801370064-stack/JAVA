package com.library.service;

import com.library.dao.StudentDAO;
import com.library.model.Student;
import com.library.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class StudentService {
    private final StudentDAO studentDAO;
    public StudentService() { this(new StudentDAO()); }
    public StudentService(StudentDAO studentDAO) { this.studentDAO = studentDAO; }

    public List<Student> search(String query) throws SQLException { return studentDAO.findAll(query); }
    public List<Student> all() throws SQLException { return studentDAO.findAll(); }
    public Student find(String id) throws SQLException { return studentDAO.findById(id); }
    public int count() throws SQLException { return studentDAO.count(); }

    public void add(Student input) throws SQLException {
        Student student = validate(input);
        if (studentDAO.findById(student.id()) != null) throw new IllegalArgumentException("A student with this ID already exists.");
        studentDAO.insert(student);
    }

    public void update(Student input) throws SQLException {
        Student student = validate(input);
        if (studentDAO.findById(student.id()) == null) throw new SQLException("Student was not found.");
        studentDAO.update(student);
    }

    private Student validate(Student input) {
        String id = ValidationUtil.required(input.id(), "Student ID");
        if (id.length() > 30) throw new IllegalArgumentException("Student ID cannot exceed 30 characters.");
        return new Student(id, ValidationUtil.required(input.name(), "Name"), ValidationUtil.email(input.email()),
                input.phone() == null ? "" : input.phone().trim(), input.department() == null ? "" : input.department().trim(),
                input.academicYear() == null ? "" : input.academicYear().trim(), input.address() == null ? "" : input.address().trim());
    }

    public void delete(String id) throws SQLException { studentDAO.delete(id); }
}
