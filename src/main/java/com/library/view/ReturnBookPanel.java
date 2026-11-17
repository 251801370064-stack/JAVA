package com.library.view;

import com.library.model.Fine;
import com.library.model.FinePayment;
import com.library.model.IssuedBook;
import com.library.model.ReturnedBook;
import com.library.service.FineService;
import com.library.service.IssueService;
import com.library.service.ReturnService;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
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

public class ReturnBookPanel extends JPanel implements Refreshable {
    private final IssueService issueService = new IssueService();
    private final ReturnService returnService = new ReturnService();
    private final FineService fineService = new FineService();
    private final JTable activeTable = UiUtil.table("Issue ID", "Student ID", "Student", "Book", "Issued", "Due", "Status");
    private final JTable returnedTable = UiUtil.table("Return ID", "Issue ID", "Student", "Book", "Returned", "Overdue days", "Fine");
    private final JTable fineTable = UiUtil.table("Fine ID", "Issue", "Student", "Book", "Amount", "Paid", "Balance", "Status");
    private final JTable paymentTable = UiUtil.table("Payment ID", "Fine ID", "Issue", "Student", "Amount", "Paid at");
    private final JTextField search = new JTextField(22);
    private final JComboBox<String> fineFilter = new JComboBox<>(new String[]{"All", "Pending", "Paid"});
    private List<IssuedBook> activeRecords = new ArrayList<>();
    private List<Fine> fineRecords = new ArrayList<>();

    public ReturnBookPanel() {
        setLayout(new BorderLayout(0, 15)); setBackground(UiUtil.PALE); setBorder(BorderFactory.createEmptyBorder(24, 28, 26, 28));
        JPanel heading = new JPanel(new BorderLayout(0, 5)); heading.setOpaque(false); heading.add(UiUtil.title("Returns & fines"), BorderLayout.NORTH); heading.add(UiUtil.muted("Process returns, calculate overdue charges and record payments"), BorderLayout.CENTER); add(heading, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane(); tabs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13)); tabs.addTab("Active loans", activeTab()); tabs.addTab("Return history", UiUtil.scroll(returnedTable)); tabs.addTab("Fines & payments", fineTab()); add(tabs, BorderLayout.CENTER);
        search.setPreferredSize(new java.awt.Dimension(260, 36)); search.setToolTipText("Search by issue, student or book");
        search.getDocument().addDocumentListener(new DocumentListener() { public void insertUpdate(DocumentEvent e) { refreshData(); } public void removeUpdate(DocumentEvent e) { refreshData(); } public void changedUpdate(DocumentEvent e) { refreshData(); } });
        fineFilter.addActionListener(event -> loadFines()); refreshData();
    }

    private JPanel activeTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setBackground(java.awt.Color.WHITE); panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel tools = new JPanel(new BorderLayout()); tools.setOpaque(false); JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); left.setOpaque(false); left.add(new JLabel("Search")); left.add(search); tools.add(left, BorderLayout.WEST);
        var returnButton = UiUtil.button("Return selected book", true); tools.add(returnButton, BorderLayout.EAST); returnButton.addActionListener(event -> processReturn()); panel.add(tools, BorderLayout.NORTH); panel.add(UiUtil.scroll(activeTable), BorderLayout.CENTER); return panel;
    }

    private JPanel fineTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setBackground(java.awt.Color.WHITE); panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel toolbar = new JPanel(new BorderLayout()); toolbar.setOpaque(false); JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); left.setOpaque(false); left.add(new JLabel("Status")); left.add(fineFilter); toolbar.add(left, BorderLayout.WEST);
        var pay = UiUtil.button("Record payment", true); toolbar.add(pay, BorderLayout.EAST); pay.addActionListener(event -> recordPayment());
        JPanel split = new JPanel(new GridLayout(2, 1, 0, 10)); split.setOpaque(false); JPanel fines = new JPanel(new BorderLayout(0, 5)); fines.add(UiUtil.muted("Fine balances"), BorderLayout.NORTH); fines.add(UiUtil.scroll(fineTable), BorderLayout.CENTER); JPanel payments = new JPanel(new BorderLayout(0, 5)); payments.add(UiUtil.muted("Payment history"), BorderLayout.NORTH); payments.add(UiUtil.scroll(paymentTable), BorderLayout.CENTER); split.add(fines); split.add(payments);
        panel.add(toolbar, BorderLayout.NORTH); panel.add(split, BorderLayout.CENTER); return panel;
    }

    private void processReturn() {
        int row = activeTable.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select an active loan first.", "No selection", JOptionPane.WARNING_MESSAGE); return; }
        IssuedBook issue = activeRecords.get(activeTable.convertRowIndexToModel(row));
        if (JOptionPane.showConfirmDialog(this, "Record return of '" + issue.bookTitle() + "' from " + issue.studentName() + "? Any overdue fine will be calculated using current settings.", "Confirm return", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { var result = returnService.process(issue.issueId()); UiUtil.success(this, "Return saved. Overdue days: " + result.overdueDays() + " | Fine: " + result.fineAmount()); refreshData(); }
        catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void recordPayment() {
        int row = fineTable.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select a fine balance first.", "No selection", JOptionPane.WARNING_MESSAGE); return; }
        Fine fine = fineRecords.get(fineTable.convertRowIndexToModel(row)); if (fine.balance().signum() <= 0) { JOptionPane.showMessageDialog(this, "This fine is already settled.", "No balance", JOptionPane.INFORMATION_MESSAGE); return; }
        JTextField amount = new JTextField(fine.balance().toPlainString()); JPanel form = new JPanel(new GridLayout(0, 1, 5, 5)); form.add(new JLabel("Outstanding balance: " + fine.balance())); form.add(new JLabel("Payment amount")); form.add(amount);
        if (JOptionPane.showConfirmDialog(this, form, "Record fine payment", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try { fineService.pay(fine.id(), amount.getText()); refreshData(); UiUtil.success(this, "Payment recorded."); }
        catch (Exception exception) { UiUtil.error(this, exception); }
    }

    @Override public void refreshData() {
        try {
            activeRecords = issueService.active(search.getText(), false); DefaultTableModel active = (DefaultTableModel) activeTable.getModel(); active.setRowCount(0);
            for (IssuedBook item : activeRecords) active.addRow(new Object[]{item.issueId(), item.studentId(), item.studentName(), item.bookTitle(), item.issuedOn(), item.dueOn(), item.dueOn().isBefore(java.time.LocalDate.now()) ? "OVERDUE" : "ACTIVE"});
            List<ReturnedBook> returned = returnService.history(search.getText()); DefaultTableModel returns = (DefaultTableModel) returnedTable.getModel(); returns.setRowCount(0);
            for (ReturnedBook item : returned) returns.addRow(new Object[]{item.returnId(), item.issueId(), item.studentName(), item.bookTitle(), item.returnedOn(), item.overdueDays(), item.fineAmount()});
            loadFines();
        } catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void loadFines() {
        try {
            fineRecords = fineService.search(search.getText(), (String) fineFilter.getSelectedItem()); DefaultTableModel fines = (DefaultTableModel) fineTable.getModel(); fines.setRowCount(0);
            for (Fine item : fineRecords) fines.addRow(new Object[]{item.id(), item.issueId(), item.studentName(), item.bookTitle(), item.amount(), item.paidAmount(), item.balance(), item.status()});
            List<FinePayment> payments = fineService.paymentHistory(); DefaultTableModel history = (DefaultTableModel) paymentTable.getModel(); history.setRowCount(0);
            for (FinePayment payment : payments) history.addRow(new Object[]{payment.paymentId(), payment.fineId(), payment.issueId(), payment.studentName(), payment.amount(), payment.paidAt()});
        } catch (Exception exception) { UiUtil.error(this, exception); }
    }
}
