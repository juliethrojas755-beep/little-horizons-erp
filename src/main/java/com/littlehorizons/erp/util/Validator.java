package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Reglas de validación sin estado, compartidas por el lector de consola y los servicios.
 * <p>Se usan expresiones regulares (Pattern): describen cómo debe verse un texto válido.</p>
 */
public final class Validator {

    // Rango válido de una nota en Colombia
    public static final BigDecimal MIN_GRADE = new BigDecimal("0.0");
    public static final BigDecimal MAX_GRADE = new BigDecimal("5.0");

    // Se compilan una sola vez (son costosas de crear) y se reutilizan
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");   // algo@dominio.com
    private static final Pattern DOCUMENT = Pattern.compile("^[A-Za-z0-9]{5,20}$");             // 5 a 20 letras o números
    private static final Pattern PHONE = Pattern.compile("^\\d{7,10}$");                         // 7 u 10 dígitos
    private static final Pattern NAME = Pattern.compile("^[\\p{L} '.-]{2,100}$");                // letras (con tildes), espacio, ' . -

    // Constructor privado: clase de utilidad, no se instancia
    private Validator() {
    }

    /** Verdadero si el texto no es null ni está en blanco. */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /** Verdadero si el texto tiene forma de correo electrónico. */
    public static boolean isValidEmail(String value) {
        return value != null && EMAIL.matcher(value).matches();
    }

    /** Documentos colombianos: de 5 a 20 letras o dígitos, sin espacios ni símbolos. */
    public static boolean isValidDocument(String value) {
        return value != null && DOCUMENT.matcher(value).matches();
    }

    /** Teléfono fijo (7 dígitos) o celular (10 dígitos). */
    public static boolean isValidPhone(String value) {
        return value != null && PHONE.matcher(value).matches();
    }

    /** Nombre o apellido: solo letras y espacios, mínimo 2 caracteres. */
    public static boolean isValidName(String value) {
        return value != null && NAME.matcher(value.trim()).matches();
    }

    /** Nota entre 0.0 y 5.0 con máximo un decimal, como exige la columna DECIMAL(2,1). */
    public static boolean isValidGrade(BigDecimal grade) {
        return grade != null
                && grade.scale() <= 1                          // a lo sumo un decimal
                && grade.compareTo(MIN_GRADE) >= 0             // no menor que 0.0
                && grade.compareTo(MAX_GRADE) <= 0;            // no mayor que 5.0
    }

    /** Verdadero si el valor es mayor que cero. */
    public static boolean isPositive(BigDecimal amount) {
        // signum() devuelve 1 si es positivo, 0 si es cero y -1 si es negativo
        return amount != null && amount.signum() > 0;
    }
}
