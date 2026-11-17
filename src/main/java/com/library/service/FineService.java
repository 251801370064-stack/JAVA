package com.library.service;

import com.library.dao.FineDAO;
import com.library.model.Fine;
import com.library.model.FinePayment;
import com.library.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class FineService {
    private final FineDAO fineDAO;
    public FineService() { this(new FineDAO()); }
    public FineService(FineDAO fineDAO) { this.fineDAO = fineDAO; }

    public List<Fine> search(String query, String filter) throws SQLException { return fineDAO.findAll(query, filter); }
    public List<FinePayment> paymentHistory() throws SQLException { return fineDAO.paymentHistory(); }
    public BigDecimal totalCollected() throws SQLException { return fineDAO.totalCollected(); }
    public void pay(long fineId, String amount) throws SQLException {
        BigDecimal payment = ValidationUtil.nonNegativeMoney(amount, "Payment");
        if (payment.signum() == 0) throw new IllegalArgumentException("Payment must be greater than zero.");
        fineDAO.recordPayment(fineId, payment);
    }
}
