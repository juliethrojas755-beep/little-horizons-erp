package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Stateless validation rules shared by the input reader and the services.
 */
public final class Validator {

    public static final BigDecimal MIN_GRADE = new BigDecimal("0.0");
    public static final BigDecimal MAX_GRADE = new BigDecimal("5.0");

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern DOCUMENT = Pattern.compile("^[A-Za-z0-9]{5,20}$");
    private static final Pattern PHONE = Pattern.compile("^\\d{7,10}$");
    private static final Pattern NAME = Pattern.compile("^[\\p{L} '.-]{2,100}$");

    private Validator() {
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL.matcher(value).matches();
    }

    /** Colombian documents: 5 to 20 letters or digits, no spaces or symbols. */
    public static boolean isValidDocument(String value) {
        return value != null && DOCUMENT.matcher(value).matches();
    }

    /** Landline (7 digits) or mobile (10 digits). */
    public static boolean isValidPhone(String value) {
        return value != null && PHONE.matcher(value).matches();
    }

    public static boolean isValidName(String value) {
        return value != null && NAME.matcher(value.trim()).matches();
    }

    /** Grade between 0.0 and 5.0 with at most one decimal, as DECIMAL(2,1) requires. */
    public static boolean isValidGrade(BigDecimal grade) {
        return grade != null
                && grade.scale() <= 1
                && grade.compareTo(MIN_GRADE) >= 0
                && grade.compareTo(MAX_GRADE) <= 0;
    }

    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }
}
