package com.library.view;

import com.library.model.Book;
import com.library.model.IssuedBook;
import com.library.model.Student;
import com.library.service.BookService;
import com.library.service.IssueService;
import com.library.service.StudentService;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
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
import java.util.List;

public class IssueBookPanel extends JPanel implements Refreshable {
    private final IssueService issueService = new IssueService();
    private final StudentService studentService = new StudentService();
    private final BookService bookService = new BookService();
    private final JTable table = UiUtil.table("Issue ID", "Student ID", "Student", "Book ID", "Book", "Issued", "Due", "Status");
    private final JTextField search = new JTextField(22);
    private final JCheckBox overdue = new JCheckBox("Overdue only");
    private final JComboBox<StudentOption> studentChoice = new JComboBox<>();
    private final JComboBox<BookOption> bookChoice = new JComboBox<>();

    public IssueBookPanel() {
        setLayout(new BorderLayout(0, 16)); setBackground(UiUtil.PALE); setBorder(BorderFactory.createEmptyBorder(24, 28, 26, 28));
        JPanel heading = new JPanel(new BorderLayout(0, 5)); heading.setOpaque(false); heading.add(UiUtil.title("Issue books"), BorderLayout.NORTH); heading.add(UiUtil.muted("Create a loan and monitor active circulation"), BorderLayout.CENTER); add(heading, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 12)); body.setOpaque(false);
        JPanel issueForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 11)); issueForm.setBackground(java.awt.Color.WHITE); issueForm.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiUtil.BORDER), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        studentChoice.setPreferredSize(new java.awt.Dimension(220, 34)); bookChoice.setPreferredSize(new java.awt.Dimension(340, 34));
        issueForm.add(new JLabel("Student")); issueForm.add(studentChoice); issueForm.add(new JLabel("Available book")); issueForm.add(bookChoice); var issue = UiUtil.button("Issue book", true); issueForm.add(issue);
        JPanel filter = new JPanel(new BorderLayout(8, 0)); filter.setOpaque(false); JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); left.setOpaque(false); search.setPreferredSize(new java.awt.Dimension(270, 36)); search.setToolTipText("Search student, book or issue ID"); left.add(new JLabel("Search")); left.add(search); left.add(overdue); filter.add(left, BorderLayout.WEST);
        JPanel controls = new JPanel(new BorderLayout(0, 10)); controls.setOpaque(false); controls.add(issueForm, BorderLayout.NORTH); controls.add(filter, BorderLayout.SOUTH);
        body.add(controls, BorderLayout.NORTH); body.add(UiUtil.scroll(table), BorderLayout.CENTER); add(body, BorderLayout.CENTER);
        issue.addActionListener(event -> issue()); overdue.addActionListener(event -> loadIssues());
        search.getDocument().addDocumentListener(new DocumentListener() { public void insertUpdate(DocumentEvent e) { loadIssues(); } public void removeUpdate(DocumentEvent e) { loadIssues(); } public void changedUpdate(DocumentEvent e) { loadIssues(); } });
        refreshData();
    }

    private void issue() {
        StudentOption student = (StudentOption) studentChoice.getSelectedItem(); BookOption book = (BookOption) bookChoice.getSelectedItem();
        if (student == null || book == null) { JOptionPane.showMessageDialog(this, "Add a student and an available book before issuing.", "Missing selection", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Issue '" + book.title() + "' to " + student.name() + "?", "Confirm issue", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { long id = issueService.issue(student.id(), book.id()); UiUtil.success(this, "Book issued. Issue ID: " + id); refreshData(); }
        catch (Exception exception) { UiUtil.error(this, exception); }
    }

    @Override public void refreshData() {
        try {
            studentChoice.removeAllItems(); for (Student student : studentService.all()) studentChoice.addItem(new StudentOption(student.id(), student.name()));
            bookChoice.removeAllItems(); for (Book book : bookService.availableBooks()) bookChoice.addItem(new BookOption(book.id(), book.title(), book.availableQuantity()));
            loadIssues();
        } catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void loadIssues() {
        try { List<IssuedBook> active = issueService.active(search.getText(), overdue.isSelected()); DefaultTableModel model = (DefaultTableModel) table.getModel(); model.setRowCount(0); for (IssuedBook item : active) model.addRow(new Object[]{item.issueId(), item.studentId(), item.studentName(), item.bookId(), item.bookTitle(), item.issuedOn(), item.dueOn(), item.dueOn().isBefore(java.time.LocalDate.now()) ? "OVERDUE" : "ACTIVE"}); }
        catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private record StudentOption(String id, String name) { @Override public String toString() { return name + "  /  " + id; } }
    private record BookOption(long id, String title, int available) { @Override public String toString() { return title + "  (" + available + " available)"; } }
}
