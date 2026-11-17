package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Fine(long id, long returnId, long issueId, String studentId, String studentName,
                   String bookTitle, BigDecimal amount, BigDecimal paidAmount, String status,
                   LocalDate returnedOn) {
    public BigDecimal balance() {
        return amount.subtract(paidAmount);
    }
}
