package com.library.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

final class UiUtil {
    static final Color NAVY = new Color(22, 52, 82);
    static final Color BLUE = new Color(35, 108, 177);
    static final Color PALE = new Color(241, 245, 249);
    static final Color MUTED = new Color(101, 116, 132);
    static final Color BORDER = new Color(221, 229, 236);

    private UiUtil() {
    }

    static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setMargin(new Insets(9, 15, 9, 15));
        button.setBackground(primary ? BLUE : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : NAVY);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? BLUE : BORDER), BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        return button;
    }

    static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 25));
        label.setForeground(NAVY);
        return label;
    }

    static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(MUTED);
        return label;
    }

    static JTable table(String... columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(220, 237, 251));
        table.setSelectionForeground(NAVY);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(PALE);
        table.getTableHeader().setForeground(NAVY);
        table.setFillsViewportHeight(true);
        return table;
    }

    static JScrollPane scroll(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        return pane;
    }

    static void error(java.awt.Component parent, Throwable exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) message = exception.getClass().getSimpleName();
        JOptionPane.showMessageDialog(parent, message, "Unable to complete action", JOptionPane.ERROR_MESSAGE);
    }

    static void success(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    static void installDefaults() {
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollBar.width", 12);
    }
}
