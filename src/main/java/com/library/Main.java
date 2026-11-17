package com.library;

import com.formdev.flatlaf.FlatLightLaf;
import com.library.config.DatabaseConnection;
import com.library.view.LoginFrame;
import com.library.view.UiDefaults;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.Connection;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UiDefaults.install();
        SwingUtilities.invokeLater(() -> {
            try (Connection ignored = DatabaseConnection.getConnection()) {
                new LoginFrame().setVisible(true);
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to MySQL. Check that MySQL is running, import database/library_management.sql, and configure library.properties.\n\n" + exception.getMessage(),
                        "Database connection required", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
