package com.library.service;

import com.library.dao.FineDAO;
import com.library.dao.ReturnDAO;
import com.library.dao.SettingsDAO;
import com.library.model.ReturnedBook;

import java.sql.SQLException;
import java.util.List;

public class ReturnService {
    private final ReturnDAO returnDAO;
    private final FineDAO fineDAO;
    private final SettingsDAO settingsDAO;
    public ReturnService() { this(new ReturnDAO(), new FineDAO(), new SettingsDAO()); }
    public ReturnService(ReturnDAO returnDAO, FineDAO fineDAO, SettingsDAO settingsDAO) {
        this.returnDAO = returnDAO; this.fineDAO = fineDAO; this.settingsDAO = settingsDAO;
    }

    public ReturnedBook process(long issueId) throws Exception {
        return returnDAO.returnBook(issueId, java.time.LocalDate.now(), settingsDAO.finePerDay());
    }
    public List<ReturnedBook> history(String query) throws SQLException { return returnDAO.findAll(query); }
    public java.math.BigDecimal collected() throws SQLException { return fineDAO.totalCollected(); }
}
