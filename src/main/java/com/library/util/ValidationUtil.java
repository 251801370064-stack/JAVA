package com.library.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern ISBN = Pattern.compile("^(?:[0-9]{10}|[0-9]{13})$");

    private ValidationUtil() {
    }

    public static String required(String value, String label) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(label + " is required.");
        return normalized;
    }

    public static String email(String value) {
        String normalized = required(value, "Email");
        if (!EMAIL.matcher(normalized).matches()) throw new IllegalArgumentException("Enter a valid email address.");
        return normalized;
    }

    public static String isbn(String value) {
        String normalized = required(value, "ISBN").replaceAll("[- ]", "");
        if (!ISBN.matcher(normalized).matches()) throw new IllegalArgumentException("ISBN must contain 10 or 13 digits.");
        return normalized;
    }

    public static int nonNegativeInt(String value, String label) {
        try {
            int parsed = Integer.parseInt(required(value, label));
            if (parsed < 0) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) { throw new IllegalArgumentException(label + " must be a non-negative whole number."); }
    }

    public static BigDecimal nonNegativeMoney(String value, String label) {
        try {
            BigDecimal parsed = new BigDecimal(required(value, label));
            if (parsed.signum() < 0 || parsed.scale() > 2) throw new NumberFormatException();
            return parsed.setScale(2);
        } catch (NumberFormatException exception) { throw new IllegalArgumentException(label + " must be a non-negative amount with up to 2 decimal places."); }
    }
}
