package com.library.view;

import com.library.service.SettingsService;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class SettingsPanel extends JPanel implements Refreshable {
    private final SettingsService service = new SettingsService();
    private final JTextField finePerDay = new JTextField(16);
    private final JTextField loanDays = new JTextField(16);

    public SettingsPanel() {
        setLayout(new BorderLayout(0, 16)); setBackground(UiUtil.PALE); setBorder(BorderFactory.createEmptyBorder(24, 28, 26, 28));
        JPanel heading = new JPanel(new BorderLayout(0, 5)); heading.setOpaque(false); heading.add(UiUtil.title("Settings"), BorderLayout.NORTH); heading.add(UiUtil.muted("Configure circulation policy"), BorderLayout.CENTER); add(heading, BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(0, 16)); content.setBackground(java.awt.Color.WHITE); content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiUtil.BORDER), BorderFactory.createEmptyBorder(20, 22, 20, 22)));
        JLabel title = new JLabel("Loan rules"); title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 17)); title.setForeground(UiUtil.NAVY); content.add(title, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(0, 2, 14, 14)); form.setOpaque(false); form.add(new JLabel("Fine per overdue day")); form.add(finePerDay); form.add(new JLabel("Loan period (days)")); form.add(loanDays);
        content.add(form, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false); bottom.add(UiUtil.muted("Policy changes apply to new issues and returns processed after saving."), BorderLayout.WEST); var save = UiUtil.button("Save settings", true); bottom.add(save, BorderLayout.EAST); content.add(bottom, BorderLayout.SOUTH); add(content, BorderLayout.NORTH);
        save.addActionListener(event -> save()); refreshData();
    }

    @Override public void refreshData() {
        try { finePerDay.setText(service.finePerDay().toPlainString()); loanDays.setText(Integer.toString(service.loanPeriodDays())); }
        catch (Exception exception) { UiUtil.error(this, exception); }
    }

    private void save() {
        try { service.update(finePerDay.getText(), loanDays.getText()); UiUtil.success(this, "Circulation settings saved."); refreshData(); }
        catch (Exception exception) { UiUtil.error(this, exception); }
    }
}
