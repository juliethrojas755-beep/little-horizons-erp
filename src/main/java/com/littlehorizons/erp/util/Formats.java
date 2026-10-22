package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatos colombianos para dinero y fechas, compartidos por la consola y el PDF.
 */
public final class Formats {

    // Configuración regional de Colombia: usa punto para los miles ($ 350.000)
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    // Fechas como día/mes/año (19/10/2026)
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Constructor privado: clase de utilidad con métodos estáticos, no se instancia
    private Formats() {
    }

    /** @return por ejemplo "$ 350.000" (en el uso diario los pesos no llevan centavos) */
    public static String money(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }
        NumberFormat format = NumberFormat.getNumberInstance(COLOMBIA);
        format.setMinimumFractionDigits(0);    // no muestra decimales si son cero
        format.setMaximumFractionDigits(2);    // pero sí hasta dos si existen
        return "$ " + format.format(amount);
    }

    /** @return la fecha como dd/MM/yyyy, o "-" si es null */
    public static String date(LocalDate date) {
        return date == null ? "-" : date.format(DATE);
    }
}
