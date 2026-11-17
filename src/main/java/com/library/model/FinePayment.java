package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinePayment(long paymentId, long fineId, long issueId, String studentName,
                          BigDecimal amount, LocalDateTime paidAt) {
}
