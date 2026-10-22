package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Lee y valida lo que el usuario escribe en la consola. Cada método repite la pregunta hasta recibir un dato válido.
 * <p>Navegación desde cualquier pregunta: {@value #CANCEL_KEYWORD} lanza {@link OperationCancelledException}
 * (vuelve al menú anterior) y {@value #MAIN_MENU_KEYWORD} lanza {@link ReturnToMainMenuException}
 * (va directo al menú principal).</p>
 */
public final class InputReader {

    // ---------- Constantes ----------

    // Texto que el usuario escribe para cancelar y volver atrás
    public static final String CANCEL_KEYWORD = "0";
    // Texto que el usuario escribe para ir al menú principal
    public static final String MAIN_MENU_KEYWORD = "M";
    // Formato de fechas que se pide al usuario: año-mes-día (2026-10-19)
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // Formato de horas en 24 horas (07:30)
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // Scanner que lee el teclado; se recibe por constructor (inyección de dependencias)
    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    // ====================== MÉTODO GENÉRICO ======================

    /**
     * Ciclo de lectura genérico: pregunta, convierte y valida. Todos los lectores con tipo lo reutilizan.
     *
     * @param prompt       texto que se muestra al usuario
     * @param parser       convierte el texto escrito al tipo que se necesita; puede lanzar una excepción si el formato es malo
     * @param rule         validación adicional sobre el valor ya convertido
     * @param errorMessage mensaje que se muestra si la conversión o la validación fallan
     */
    public <T> T read(String prompt, Function<String, T> parser, Predicate<T> rule, String errorMessage) {
        // Se repite hasta que el usuario escriba un dato válido
        while (true) {
            String raw = readRaw(prompt);
            // "0" cancela la operación y vuelve al menú anterior
            if (CANCEL_KEYWORD.equals(raw)) {
                throw new OperationCancelledException();
            }
            try {
                T value = parser.apply(raw);         // intenta convertir el texto (por ejemplo, a número o fecha)
                if (rule.test(value)) {              // y revisa la regla (por ejemplo, que esté en el rango)
                    return value;                    // dato válido: se devuelve y termina el ciclo
                }
            } catch (RuntimeException e) {
                // la conversión falló (no era un número, una fecha...): se cae al mensaje de error
            }
            printError(errorMessage);
        }
    }

    // ====================== LECTORES POR TIPO ======================

    /** Texto obligatorio: no acepta vacío. */
    public String readText(String prompt) {
        return read(prompt, String::trim, Validator::isNotBlank, "Este campo es obligatorio.");
    }

    /** Texto opcional: si el usuario solo presiona Enter se devuelve {@code null}. */
    public String readOptionalText(String prompt) {
        String raw = readRaw(prompt + " (opcional, Enter para omitir)");
        if (CANCEL_KEYWORD.equals(raw)) {
            throw new OperationCancelledException();
        }
        return raw.isBlank() ? null : raw.trim();
    }

    /** Texto que debe cumplir una regla propia (por ejemplo, un correo válido). */
    public String readValidated(String prompt, Predicate<String> rule, String errorMessage) {
        return read(prompt, String::trim, rule, errorMessage);
    }

    /** Número entero dentro de un rango (incluye los extremos). */
    public int readInt(String prompt, int min, int max) {
        return read(prompt, Integer::parseInt, value -> value >= min && value <= max,
                "Ingrese un número entero entre " + min + " y " + max + ".");
    }

    /** Opción de un menú: a diferencia de los otros lectores, 0 es una respuesta válida (significa "volver"). */
    public int readMenuOption(String prompt, int maxOption) {
        while (true) {
            String raw = readRaw(prompt);
            try {
                int option = Integer.parseInt(raw);
                if (option >= 0 && option <= maxOption) {
                    return option;
                }
            } catch (NumberFormatException e) {
                // no era un número: se vuelve a preguntar
            }
            printError("Opción inválida. Elija un número entre 0 y " + maxOption + ".");
        }
    }

    /** Valor de dinero mayor que cero (acepta comas de miles: 350,000). */
    public BigDecimal readAmount(String prompt) {
        return read(prompt, raw -> new BigDecimal(raw.replace(",", "")), Validator::isPositive,
                "Ingrese un valor numérico mayor que cero (ej: 350000).");
    }

    /** Nota de 0.0 a 5.0 con un solo decimal (acepta coma o punto: 4,5 o 4.5). */
    public BigDecimal readGrade(String prompt) {
        return read(prompt, raw -> new BigDecimal(raw.replace(",", ".")), Validator::isValidGrade,
                "La nota debe estar entre 0.0 y 5.0 con un solo decimal (ej: 4.5).");
    }

    /** Fecha obligatoria en formato AAAA-MM-DD. */
    public LocalDate readDate(String prompt) {
        // LocalDate.parse lanza una excepción si el formato es inválido; read() la captura y repite la pregunta
        return read(prompt + " [AAAA-MM-DD]", raw -> LocalDate.parse(raw, DATE_FORMAT), date -> true,
                "Fecha inválida. Use el formato AAAA-MM-DD (ej: 2026-10-19).");
    }

    /** Fecha que acepta Enter como "hoy". */
    public LocalDate readDateOrToday(String prompt) {
        String raw = readRaw(prompt + " [AAAA-MM-DD, Enter = hoy]");
        if (CANCEL_KEYWORD.equals(raw)) {
            throw new OperationCancelledException();
        }
        // Enter vacío = fecha de hoy
        if (raw.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(raw, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            printError("Fecha inválida. Use el formato AAAA-MM-DD.");
            return readDateOrToday(prompt);   // se vuelve a preguntar llamándose a sí mismo
        }
    }

    /** Hora obligatoria en formato de 24 horas HH:MM. */
    public LocalTime readTime(String prompt) {
        return read(prompt + " [HH:MM]", raw -> LocalTime.parse(raw, TIME_FORMAT), time -> true,
                "Hora inválida. Use el formato 24 horas HH:MM (ej: 07:30).");
    }

    /** Pregunta de sí o no: devuelve true para "s" y false para "n". */
    public boolean readYesNo(String prompt) {
        String answer = read(prompt + " (s/n)", raw -> raw.trim().toLowerCase(),
                raw -> raw.equals("s") || raw.equals("n"), "Responda 's' para sí o 'n' para no.");
        return answer.equals("s");
    }

    /**
     * Deja escoger un valor de una lista (normalmente un enum) por su número.
     *
     * @param label cómo se muestra cada opción en pantalla
     */
    public <T> T readChoice(String prompt, List<T> options, Function<T, String> label) {
        // Se imprime la lista numerada desde 1
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("    %s) %s%n", Ansi.CYAN.apply(String.format("%2d", i + 1)), label.apply(options.get(i)));
        }
        int index = readInt(prompt, 1, options.size());   // el número debe estar dentro de la lista
        return options.get(index - 1);                    // -1 porque la lista de Java empieza en 0
    }

    /** Detiene la pantalla hasta que el usuario presione Enter (para que alcance a leer el resultado). */
    public void pause() {
        readRaw("\n  Presione Enter para continuar");
    }

    // ====================== LECTURA BÁSICA ======================

    /** Muestra el prompt y lee una línea; es el único punto que toca el teclado. */
    private String readRaw(String prompt) {
        System.out.print("  " + Ansi.BRIGHT_CYAN.apply("›") + " " + prompt + ": ");
        if (!scanner.hasNextLine()) {
            // el teclado se cerró (Ctrl+D): se comporta como cancelar en lugar de romperse
            throw new OperationCancelledException();
        }
        String value = scanner.nextLine().trim();
        // "M" en cualquier pregunta lleva al menú principal
        if (MAIN_MENU_KEYWORD.equalsIgnoreCase(value)) {
            throw new ReturnToMainMenuException();
        }
        return value;
    }

    /** Imprime un mensaje de error en rojo. */
    private void printError(String message) {
        System.out.println("  " + Ansi.RED.apply("✖ " + message));
    }
}
