package com.library.view;

import com.library.model.Student;
import com.library.service.StudentService;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class StudentsPanel extends JPanel implements Refreshable {
    private final StudentService service = new StudentService();
    private final JTable table = UiUtil.table("Student ID", "Name", "Email", "Phone", "Department", "Year", "Address");
    private final JTextField search = new JTextField(24);
    private List<Student> records = new ArrayList<>();

    public StudentsPanel() {
        setLayout(new BorderLayout(0, 16)); setBackground(UiUtil.PALE); setBorder(BorderFactory.createEmptyBorder(24, 28, 26, 28));
        JPanel heading = new JPanel(new BorderLayout(0, 5)); heading.setOpaque(false); heading.add(UiUtil.title("Students"), BorderLayout.NORTH); heading.add(UiUtil.muted("Student directory and borrowing eligibility"), BorderLayout.CENTER); add(heading, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 12)); body.setOpaque(false);
        JPanel toolbar = new JPanel(new BorderLayout(10, 0)); toolbar.setOpaque(false); JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); filters.setOpaque(false);
        search.setToolTipText("Search student name, ID or email"); search.setPreferredSize(new java.awt.Dimension(280, 36)); filters.add(new JLabel("Search")); filters.add(search); toolbar.add(filters, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false); var add = UiUtil.button("Add student", true); var edit = UiUtil.button("Edit", false); var delete = UiUtil.button("Delete", false); actions.add(add); actions.add(edit); actions.add(delete); toolbar.add(actions, BorderLayout.EAST);
        body.add(toolbar, BorderLayout.NORTH); body.add(UiUtil.scroll(table), BorderLayout.CENTER); add(body, BorderLayout.CENTER);
        add.addActionListener(event -> editStudent(null)); edit.addActionListener(event -> editStudent(selected())); delete.addActionListener(event -> deleteStudent());
        search.getDocument().addDocumentListener(new DocumentListener() { public void insertUpdate(DocumentEvent e) { refreshData(); } public void removeUpdate(DocumentEvent e) { refreshData(); } public void changedUpdate(DocumentEvent e) { refreshData(); } });
        refreshData();
    }

    @Override public void refreshData() {
        try { records = service.search(search.getText()); DefaultTableModel model = (DefaultTableModel) table.getModel(); model.setRowCount(0); for (Student student : records) model.addRow(new Object[]{student.id(), student.name(), student.email(), student.phone(), student.department(), student.academicYear(), student.address()}); }
        catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private Student selected() { int row = table.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select a student first.", "No selection", JOptionPane.WARNING_MESSAGE); return null; } return records.get(table.convertRowIndexToModel(row)); }

    private void editStudent(Student original) {
        JTextField id = new JTextField(original == null ? "" : original.id()); JTextField name = new JTextField(original == null ? "" : original.name());
        JTextField email = new JTextField(original == null ? "" : original.email()); JTextField phone = new JTextField(original == null ? "" : original.phone());
        JTextField department = new JTextField(original == null ? "" : original.department()); JTextField year = new JTextField(original == null ? "" : original.academicYear()); JTextField address = new JTextField(original == null ? "" : original.address());
        id.setEditable(original == null);
        JPanel form = new JPanel(new GridLayout(0, 2, 9, 8)); form.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 5));
        addField(form, "Student ID", id); addField(form, "Full name", name); addField(form, "Email", email); addField(form, "Phone", phone); addField(form, "Department", department); addField(form, "Academic year", year); addField(form, "Address", address);
        if (JOptionPane.showConfirmDialog(this, form, original == null ? "Add student" : "Edit student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Student student = new Student(id.getText().trim(), name.getText().trim(), email.getText().trim(), phone.getText().trim(), department.getText().trim(), year.getText().trim(), address.getText().trim());
            if (original == null) service.add(student); else service.update(student);
            refreshData(); UiUtil.success(this, original == null ? "Student added." : "Student updated.");
        }
        catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void deleteStudent() {
        Student student = selected(); if (student == null) return;
        if (JOptionPane.showConfirmDialog(this, "Delete student '" + student.name() + "'?", "Confirm deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { service.delete(student.id()); refreshData(); } catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void addField(JPanel panel, String label, JTextField input) { panel.add(new JLabel(label)); input.setColumns(20); panel.add(input); }
}
