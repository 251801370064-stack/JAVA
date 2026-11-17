package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReturnedBook(long returnId, long issueId, String studentId, String studentName,
                           long bookId, String bookTitle, LocalDate returnedOn, int overdueDays,
                           BigDecimal fineAmount) {
}
