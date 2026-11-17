package com.library.service;

import com.library.dao.SettingsDAO;
import com.library.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;

public class SettingsService {
    private final SettingsDAO settingsDAO;
    public SettingsService() { this(new SettingsDAO()); }
    public SettingsService(SettingsDAO settingsDAO) { this.settingsDAO = settingsDAO; }

    public BigDecimal finePerDay() throws SQLException { return settingsDAO.finePerDay(); }
    public int loanPeriodDays() throws SQLException { return settingsDAO.loanPeriodDays(); }
    public void update(String finePerDay, String loanDays) throws SQLException {
        BigDecimal fine = ValidationUtil.nonNegativeMoney(finePerDay, "Fine per day");
        int days = ValidationUtil.nonNegativeInt(loanDays, "Loan period");
        settingsDAO.update(fine, days);
    }
}
