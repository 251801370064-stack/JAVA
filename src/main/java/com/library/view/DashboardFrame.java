package com.library.view;

import com.library.model.Admin;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {
    private final Admin admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final Map<String, JPanel> pages = new LinkedHashMap<>();
    private final Map<String, JButton> navigation = new LinkedHashMap<>();
    private final JLabel pageTitle = new JLabel("Dashboard");

    public DashboardFrame(Admin admin) {
        this.admin = admin;
        setTitle("Library Management System"); setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); setSize(1320, 850); setMinimumSize(new Dimension(1080, 700)); setLocationRelativeTo(null);
        setContentPane(build());
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent event) { logout(); } });
        navigate("Dashboard");
    }

    private JPanel build() {
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(UiUtil.PALE);
        JPanel sidebar = new JPanel(new BorderLayout()); sidebar.setPreferredSize(new Dimension(226, 0)); sidebar.setBackground(UiUtil.NAVY);
        JPanel brand = new JPanel(new BorderLayout(10, 0)); brand.setBackground(UiUtil.NAVY); brand.setBorder(BorderFactory.createEmptyBorder(22, 18, 22, 14));
        JLabel mark = new JLabel("L"); mark.setHorizontalAlignment(SwingConstants.CENTER); mark.setOpaque(true); mark.setBackground(new Color(57, 134, 194)); mark.setForeground(Color.WHITE); mark.setFont(new Font("Segoe UI", Font.BOLD, 19)); mark.setPreferredSize(new Dimension(40, 40));
        JPanel brandWords = new JPanel(new BorderLayout(0, 3)); brandWords.setOpaque(false); JLabel appName = new JLabel("Library Desk"); appName.setForeground(Color.WHITE); appName.setFont(new Font("Segoe UI", Font.BOLD, 16)); JLabel appLabel = new JLabel("MANAGEMENT SYSTEM"); appLabel.setForeground(new Color(159, 184, 207)); appLabel.setFont(new Font("Segoe UI", Font.BOLD, 9)); brandWords.add(appName, BorderLayout.NORTH); brandWords.add(appLabel, BorderLayout.CENTER);
        brand.add(mark, BorderLayout.WEST); brand.add(brandWords, BorderLayout.CENTER); sidebar.add(brand, BorderLayout.NORTH);
        JPanel menu = new JPanel(new java.awt.GridLayout(0, 1, 0, 5)); menu.setBackground(UiUtil.NAVY); menu.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        String[] items = {"Dashboard", "Books", "Students", "Issue Books", "Return Books", "Reports", "Settings"};
        for (String item : items) {
            JButton button = new JButton(item); button.setHorizontalAlignment(SwingConstants.LEFT); button.setFont(new Font("Segoe UI", Font.BOLD, 13)); button.setForeground(new Color(213, 226, 237)); button.setBackground(UiUtil.NAVY); button.setFocusPainted(false); button.setBorder(BorderFactory.createEmptyBorder(12, 13, 12, 8)); button.setMargin(new Insets(0, 0, 0, 0)); button.addActionListener(event -> navigate(item)); navigation.put(item, button); menu.add(button);
        }
        sidebar.add(menu, BorderLayout.CENTER);
        JPanel account = new JPanel(new BorderLayout(0, 10)); account.setBackground(UiUtil.NAVY); account.setBorder(BorderFactory.createEmptyBorder(14, 16, 18, 16));
        JLabel signedIn = new JLabel("SIGNED IN AS"); signedIn.setForeground(new Color(151, 177, 200)); signedIn.setFont(new Font("Segoe UI", Font.BOLD, 9)); JLabel user = new JLabel(admin.username()); user.setForeground(Color.WHITE); user.setFont(new Font("Segoe UI", Font.BOLD, 13)); JPanel who = new JPanel(new BorderLayout(0, 4)); who.setOpaque(false); who.add(signedIn, BorderLayout.NORTH); who.add(user, BorderLayout.CENTER);
        JButton logout = new JButton("Log out"); logout.setForeground(Color.WHITE); logout.setBackground(new Color(39, 70, 99)); logout.setFocusPainted(false); logout.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12)); logout.setHorizontalAlignment(SwingConstants.LEFT); logout.addActionListener(event -> logout());
        account.add(who, BorderLayout.NORTH); account.add(logout, BorderLayout.SOUTH); sidebar.add(account, BorderLayout.SOUTH);
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(UiUtil.PALE);
        JPanel topbar = new JPanel(new BorderLayout()); topbar.setBackground(Color.WHITE); topbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiUtil.BORDER)); topbar.setPreferredSize(new Dimension(0, 66)); topbar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiUtil.BORDER), BorderFactory.createEmptyBorder(0, 26, 0, 26)));
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); pageTitle.setForeground(UiUtil.NAVY); JLabel identity = new JLabel("Administrator  /  " + admin.username()); identity.setFont(new Font("Segoe UI", Font.PLAIN, 12)); identity.setForeground(UiUtil.MUTED); topbar.add(pageTitle, BorderLayout.WEST); topbar.add(identity, BorderLayout.EAST); main.add(topbar, BorderLayout.NORTH);
        register("Dashboard", new DashboardPanel()); register("Books", new BooksPanel()); register("Students", new StudentsPanel()); register("Issue Books", new IssueBookPanel()); register("Return Books", new ReturnBookPanel()); register("Reports", new ReportsPanel()); register("Settings", new SettingsPanel());
        main.add(contentCards, BorderLayout.CENTER); root.add(sidebar, BorderLayout.WEST); root.add(main, BorderLayout.CENTER); return root;
    }

    private void register(String name, JPanel panel) { pages.put(name, panel); contentCards.add(panel, name); }

    private void navigate(String name) {
        pageTitle.setText(name); cardLayout.show(contentCards, name);
        navigation.forEach((key, button) -> { boolean selected = key.equals(name); button.setBackground(selected ? new Color(43, 82, 119) : UiUtil.NAVY); button.setForeground(selected ? Color.WHITE : new Color(213, 226, 237)); });
        JPanel page = pages.get(name); if (page instanceof Refreshable refreshable) refreshable.refreshData();
    }

    private void logout() {
        int choice = javax.swing.JOptionPane.showConfirmDialog(this, "Sign out of the library workspace?", "Log out", javax.swing.JOptionPane.YES_NO_OPTION);
        if (choice == javax.swing.JOptionPane.YES_OPTION) { dispose(); new LoginFrame().setVisible(true); }
    }
}
