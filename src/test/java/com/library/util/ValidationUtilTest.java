package com.library.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilTest {
    @Test
    void normalizesIsbnSeparators() {
        assertEquals("9780132350884", ValidationUtil.isbn("978-0-13-235088-4"));
    }

    @Test
    void rejectsMalformedEmailAndNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.email("student.example.edu"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.nonNegativeMoney("-1.00", "Price"));
    }

    @Test
    void parsesMoneyToTwoDecimalPlaces() {
        assertEquals(new BigDecimal("12.50"), ValidationUtil.nonNegativeMoney("12.5", "Price"));
    }
}
