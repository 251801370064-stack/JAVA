package com.library.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilTest {
    @Test
    void calculatesDueDateFromLoanPeriod() {
        assertEquals(LocalDate.of(2026, 12, 15), DateUtil.dueDate(LocalDate.of(2026, 12, 1), 14));
    }

    @Test
    void overdueDaysNeverGoesBelowZero() {
        LocalDate dueDate = LocalDate.of(2026, 12, 15);
        assertEquals(0, DateUtil.overdueDays(dueDate, dueDate));
        assertEquals(0, DateUtil.overdueDays(dueDate, dueDate.minusDays(2)));
        assertEquals(3, DateUtil.overdueDays(dueDate, dueDate.plusDays(3)));
    }
}
