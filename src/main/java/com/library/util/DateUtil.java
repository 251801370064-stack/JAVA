package com.library.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DateUtil {
    private DateUtil() {
    }

    public static LocalDate dueDate(LocalDate issueDate, int loanDays) {
        return issueDate.plusDays(loanDays);
    }

    public static long overdueDays(LocalDate dueDate, LocalDate returnedDate) {
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, returnedDate));
    }
}
