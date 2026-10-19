package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Colombian formats for money and dates, shared by the console and the PDF.
 */
public final class Formats {

    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formats() {
    }

    /** @return e.g. "$ 350.000" (pesos have no cents in daily use) */
    public static String money(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }
        NumberFormat format = NumberFormat.getNumberInstance(COLOMBIA);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return "$ " + format.format(amount);
    }

    public static String date(LocalDate date) {
        return date == null ? "-" : date.format(DATE);
    }
}
