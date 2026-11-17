package com.library.view;

import com.library.model.Admin;
import com.library.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final CardLayout formLayout = new CardLayout();
    private final JPanel formCards = new JPanel(formLayout);
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField confirmation = new JPasswordField();
    private final JLabel feedback = new JLabel(" ");
    private final JLabel confirmationLabel = label("Confirm password");
    private final JButton submitButton = UiUtil.button("Sign in", true);
    private boolean setupMode;

    public LoginFrame() {
        setTitle("Library Management System | Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(930, 620);
        setMinimumSize(new Dimension(760, 540));
        setLocationRelativeTo(null);
        setContentPane(buildPage());
        checkFirstRun();
    }

    private JPanel buildPage() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(UiUtil.NAVY);
        brand.setPreferredSize(new Dimension(360, 0));
        GridBagConstraints left = new GridBagConstraints(); left.gridx = 0; left.anchor = GridBagConstraints.WEST; left.insets = new Insets(8, 38, 8, 38);
        JLabel monogram = new JLabel("LM"); monogram.setOpaque(true); monogram.setBackground(new Color(70, 145, 207)); monogram.setForeground(Color.WHITE); monogram.setFont(new Font("Segoe UI", Font.BOLD, 20)); monogram.setHorizontalAlignment(SwingConstants.CENTER); monogram.setPreferredSize(new Dimension(52, 52));
        left.gridy = 0; brand.add(monogram, left);
        JLabel brandTitle = new JLabel("Library\nManagement"); brandTitle.setForeground(Color.WHITE); brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 31));
        left.gridy = 1; left.insets = new Insets(24, 38, 8, 38); brand.add(brandTitle, left);
        JLabel brandSubtitle = new JLabel("A quieter way to keep\neverything in circulation."); brandSubtitle.setForeground(new Color(196, 214, 231)); brandSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        left.gridy = 2; left.insets = new Insets(4, 38, 8, 38); brand.add(brandSubtitle, left);
        JLabel footer = new JLabel("LIBRARY OPERATIONS  /  ADMIN"); footer.setForeground(new Color(150, 178, 202)); footer.setFont(new Font("Segoe UI", Font.BOLD, 10));
        left.gridy = 3; left.insets = new Insets(55, 38, 8, 38); brand.add(footer, left);
        JPanel right = new JPanel(new GridBagLayout()); right.setBackground(Color.WHITE);
        JPanel content = new JPanel(new BorderLayout(0, 20)); content.setOpaque(false); content.setPreferredSize(new Dimension(360, 460));
        JLabel heading = new JLabel("Administrator access"); heading.setFont(new Font("Segoe UI", Font.BOLD, 25)); heading.setForeground(UiUtil.NAVY);
        JLabel intro = UiUtil.muted("Sign in to manage your library workspace.");
        JPanel titleBlock = new JPanel(new BorderLayout(0, 7)); titleBlock.setOpaque(false); titleBlock.add(heading, BorderLayout.NORTH); titleBlock.add(intro, BorderLayout.CENTER);
        content.add(titleBlock, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5, 0, 5, 0);
        fields.add(label("Username"), c); c.gridy++; fields.add(input(username), c);
        c.gridy++; fields.add(label("Password"), c); c.gridy++; fields.add(input(password), c);
        c.gridy++; fields.add(confirmationLabel, c); c.gridy++; fields.add(input(confirmation), c);
        c.gridy++; c.insets = new Insets(14, 0, 0, 0); submitButton.setPreferredSize(new Dimension(0, 43)); fields.add(submitButton, c);
        c.gridy++; c.insets = new Insets(6, 0, 0, 0); feedback.setFont(new Font("Segoe UI", Font.PLAIN, 12)); feedback.setForeground(new Color(177, 64, 54)); fields.add(feedback, c);
        submitButton.addActionListener(event -> submit());
        getRootPane().setDefaultButton(submitButton);
        formCards.add(fields, "form"); content.add(formCards, BorderLayout.CENTER);
        JLabel note = UiUtil.muted("Passwords are stored as salted, one-way hashes."); content.add(note, BorderLayout.SOUTH);
        GridBagConstraints center = new GridBagConstraints(); right.add(content, center);
        root.add(brand, BorderLayout.WEST); root.add(right, BorderLayout.CENTER);
        return root;
    }

    private JLabel label(String value) {
        JLabel label = new JLabel(value); label.setFont(new Font("Segoe UI", Font.BOLD, 12)); label.setForeground(UiUtil.NAVY); label.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0)); return label;
    }

    private JTextField input(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14)); field.setPreferredSize(new Dimension(300, 40)); field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiUtil.BORDER), BorderFactory.createEmptyBorder(8, 10, 8, 10))); return field;
    }

    private void checkFirstRun() {
        try {
            setupMode = !authService.hasAdministrator();
            formLayout.show(formCards, "form");
            submitButton.setText(setupMode ? "Create administrator" : "Sign in");
            confirmation.setVisible(setupMode);
            confirmationLabel.setVisible(setupMode);
            feedback.setText(setupMode ? "First run: create the administrator account." : " ");
            feedback.setForeground(setupMode ? UiUtil.BLUE : new Color(177, 64, 54));
            revalidate(); repaint();
        } catch (Exception exception) { feedback.setText("Database unavailable: " + exception.getMessage()); }
    }

    private void submit() {
        char[] secret = password.getPassword();
        char[] confirm = confirmation.getPassword();
        try {
            if (setupMode) {
                authService.createFirstAdministrator(username.getText(), secret, confirm);
                setupMode = false;
                confirmation.setVisible(false);
                confirmationLabel.setVisible(false);
                submitButton.setText("Sign in");
                password.setText(""); confirmation.setText("");
                feedback.setForeground(new Color(43, 130, 84)); feedback.setText("Administrator created. Sign in with your new credentials.");
                return;
            }
            Admin admin = authService.authenticate(username.getText(), secret);
            if (admin == null) { feedback.setText("The username or password is not correct."); return; }
            dispose(); new DashboardFrame(admin).setVisible(true);
        } catch (Exception exception) {
            feedback.setText(exception.getMessage() == null ? "Unable to authenticate." : exception.getMessage());
        } finally {
            java.util.Arrays.fill(secret, '\0'); java.util.Arrays.fill(confirm, '\0');
            password.setText(""); confirmation.setText("");
        }
    }
}
