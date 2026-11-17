package com.library.view;

import com.library.model.ReportData;
import com.library.service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ReportsPanel extends JPanel implements Refreshable {
    private final ReportService service = new ReportService();
    private final JComboBox<String> reportChoice = new JComboBox<>(new String[]{"All books", "Available books", "Issued books", "Returned books", "Overdue books", "Registered students", "Fine collection"});
    private final JTable table = UiUtil.table();
    private ReportData current = new ReportData(List.of(), List.of());

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 15)); setBackground(UiUtil.PALE); setBorder(BorderFactory.createEmptyBorder(24, 28, 26, 28));
        JPanel heading = new JPanel(new BorderLayout(0, 5)); heading.setOpaque(false); heading.add(UiUtil.title("Reports"), BorderLayout.NORTH); heading.add(UiUtil.muted("Live reports generated from your library database"), BorderLayout.CENTER); add(heading, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 12)); body.setOpaque(false);
        JPanel toolbar = new JPanel(new BorderLayout()); toolbar.setOpaque(false); JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); filters.setOpaque(false); filters.add(new JLabel("Report")); filters.add(reportChoice); toolbar.add(filters, BorderLayout.WEST);
        var export = UiUtil.button("Export CSV", false); toolbar.add(export, BorderLayout.EAST); export.addActionListener(event -> exportCsv()); reportChoice.addActionListener(event -> refreshData());
        body.add(toolbar, BorderLayout.NORTH); body.add(UiUtil.scroll(table), BorderLayout.CENTER); add(body, BorderLayout.CENTER); refreshData();
    }

    @Override public void refreshData() {
        try {
            current = service.generate((String) reportChoice.getSelectedItem()); DefaultTableModel model = new DefaultTableModel(current.columns().toArray(), 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
            for (Object[] row : current.rows()) model.addRow(row); table.setModel(model); table.setRowHeight(32); table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        } catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new java.io.File(((String) reportChoice.getSelectedItem()).toLowerCase().replace(' ', '-') + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (BufferedWriter writer = Files.newBufferedWriter(chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8)) {
            writeRow(writer, current.columns().toArray()); for (Object[] row : current.rows()) writeRow(writer, row);
            UiUtil.success(this, "Report exported to " + chooser.getSelectedFile().getAbsolutePath());
        } catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void writeRow(BufferedWriter writer, Object[] cells) throws java.io.IOException {
        for (int index = 0; index < cells.length; index++) {
            if (index > 0) writer.write(','); String value = cells[index] == null ? "" : cells[index].toString();
            writer.write('"'); writer.write(value.replace("\"", "\"\"")); writer.write('"');
        }
        writer.newLine();
    }
}
