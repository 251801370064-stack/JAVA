package com.library.service;

import com.library.dao.ReportDAO;
import com.library.model.ReportData;

import java.sql.SQLException;

public class ReportService {
    private final ReportDAO reportDAO;
    public ReportService() { this(new ReportDAO()); }
    public ReportService(ReportDAO reportDAO) { this.reportDAO = reportDAO; }
    public ReportData generate(String report) throws SQLException { return reportDAO.run(report); }
}
