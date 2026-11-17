package com.library.view;

import com.library.model.IssuedBook;
import com.library.service.BookService;
import com.library.service.FineService;
import com.library.service.IssueService;
import com.library.service.StudentService;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel implements Refreshable {
    private final BookService books = new BookService();
    private final StudentService students = new StudentService();
    private final IssueService issues = new IssueService();
    private final FineService fines = new FineService();
    private final JLabel[] values = new JLabel[6];
    private final JTable dueTable = UiUtil.table("Issue", "Student", "Book", "Due date", "Status");

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 20)); setBackground(UiUtil.PALE); setBorder(BorderFactory.createEmptyBorder(24, 28, 26, 28));
        JPanel heading = new JPanel(new BorderLayout()); heading.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout(0, 5)); titles.setOpaque(false); titles.add(UiUtil.title("Dashboard"), BorderLayout.NORTH); titles.add(UiUtil.muted("An at-a-glance view of your collection and circulation."), BorderLayout.CENTER);
        heading.add(titles, BorderLayout.WEST);
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14)); grid.setOpaque(false);
        String[] labels = {"Total books", "Available copies", "Currently issued", "Registered students", "Overdue books", "Fines collected"};
        for (int index = 0; index < labels.length; index++) grid.add(metric(labels[index], index));
        JPanel top = new JPanel(new BorderLayout(0, 20)); top.setOpaque(false); top.add(heading, BorderLayout.NORTH); top.add(grid, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        JPanel activity = new JPanel(new BorderLayout(0, 10)); activity.setBackground(Color.WHITE); activity.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiUtil.BORDER), BorderFactory.createEmptyBorder(14, 16, 16, 16)));
        JPanel activityHeading = new JPanel(new BorderLayout()); activityHeading.setOpaque(false); JLabel activityTitle = new JLabel("Due soon and overdue"); activityTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16)); activityTitle.setForeground(UiUtil.NAVY); activityHeading.add(activityTitle, BorderLayout.WEST); activityHeading.add(UiUtil.muted("Active loans ordered by due date"), BorderLayout.EAST);
        activity.add(activityHeading, BorderLayout.NORTH); activity.add(new JScrollPane(dueTable), BorderLayout.CENTER); add(activity, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel metric(String label, int index) {
        JPanel card = new JPanel(new BorderLayout(0, 10)); card.setBackground(Color.WHITE); card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiUtil.BORDER), BorderFactory.createEmptyBorder(15, 17, 15, 17)));
        JLabel name = UiUtil.muted(label); JLabel value = new JLabel("--"); value.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28)); value.setForeground(index == 4 ? new Color(178, 86, 54) : UiUtil.NAVY); values[index] = value;
        card.add(name, BorderLayout.NORTH); card.add(value, BorderLayout.CENTER); return card;
    }

    @Override public void refreshData() {
        try {
            values[0].setText(Integer.toString(books.totalCount())); values[1].setText(Integer.toString(books.availableCount()));
            values[2].setText(Integer.toString(issues.activeCount())); values[3].setText(Integer.toString(students.count()));
            values[4].setText(Integer.toString(issues.overdueCount())); values[5].setText(NumberFormat.getCurrencyInstance(Locale.US).format(fines.totalCollected()));
            List<IssuedBook> active = issues.active("", false);
            DefaultTableModel model = (DefaultTableModel) dueTable.getModel(); model.setRowCount(0);
            active.stream().limit(8).forEach(issue -> model.addRow(new Object[]{issue.issueId(), issue.studentName(), issue.bookTitle(), issue.dueOn(), issue.dueOn().isBefore(LocalDate.now()) ? "Overdue" : "Active"}));
        } catch (Exception exception) { UiUtil.error(this, exception); }
    }
}
