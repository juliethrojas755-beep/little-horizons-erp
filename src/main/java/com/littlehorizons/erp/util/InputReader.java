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
 * Reads and validates console input. Every method retries until the value is valid.
 * <p>Navigation from any prompt: {@value #CANCEL_KEYWORD} throws {@link OperationCancelledException}
 * (back to the previous menu) and {@value #MAIN_MENU_KEYWORD} throws {@link ReturnToMainMenuException}
 * (straight to the main menu).</p>
 */
public final class InputReader {

    public static final String CANCEL_KEYWORD = "0";
    public static final String MAIN_MENU_KEYWORD = "M";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Generic read loop: asks, converts and validates. All typed readers reuse it.
     *
     * @param prompt       text shown to the user
     * @param parser       converts the raw text; may throw to signal a format error
     * @param rule         extra validation over the converted value
     * @param errorMessage message shown when conversion or validation fails
     */
    public <T> T read(String prompt, Function<String, T> parser, Predicate<T> rule, String errorMessage) {
        while (true) {
            String raw = readRaw(prompt);
            if (CANCEL_KEYWORD.equals(raw)) {
                throw new OperationCancelledException();
            }
            try {
                T value = parser.apply(raw);
                if (rule.test(value)) {
                    return value;
                }
            } catch (RuntimeException e) {
                // conversion failed, falls through to the error message
            }
            printError(errorMessage);
        }
    }

    public String readText(String prompt) {
        return read(prompt, String::trim, Validator::isNotBlank, "Este campo es obligatorio.");
    }

    /** Optional text: an empty answer returns {@code null}. */
    public String readOptionalText(String prompt) {
        String raw = readRaw(prompt + " (opcional, Enter para omitir)");
        if (CANCEL_KEYWORD.equals(raw)) {
            throw new OperationCancelledException();
        }
        return raw.isBlank() ? null : raw.trim();
    }

    public String readValidated(String prompt, Predicate<String> rule, String errorMessage) {
        return read(prompt, String::trim, rule, errorMessage);
    }

    public int readInt(String prompt, int min, int max) {
        return read(prompt, Integer::parseInt, value -> value >= min && value <= max,
                "Ingrese un número entero entre " + min + " y " + max + ".");
    }

    /** Menu option: unlike other readers, 0 is a valid answer (it means "go back"). */
    public int readMenuOption(String prompt, int maxOption) {
        while (true) {
            String raw = readRaw(prompt);
            try {
                int option = Integer.parseInt(raw);
                if (option >= 0 && option <= maxOption) {
                    return option;
                }
            } catch (NumberFormatException e) {
                // invalid number, retry
            }
            printError("Opción inválida. Elija un número entre 0 y " + maxOption + ".");
        }
    }

    public BigDecimal readAmount(String prompt) {
        return read(prompt, raw -> new BigDecimal(raw.replace(",", "")), Validator::isPositive,
                "Ingrese un valor numérico mayor que cero (ej: 350000).");
    }

    public BigDecimal readGrade(String prompt) {
        return read(prompt, raw -> new BigDecimal(raw.replace(",", ".")), Validator::isValidGrade,
                "La nota debe estar entre 0.0 y 5.0 con un solo decimal (ej: 4.5).");
    }

    public LocalDate readDate(String prompt) {
        return read(prompt + " [AAAA-MM-DD]", raw -> LocalDate.parse(raw, DATE_FORMAT), date -> true,
                "Fecha inválida. Use el formato AAAA-MM-DD (ej: 2026-10-19).");
    }

    /** Date that accepts Enter as "today". */
    public LocalDate readDateOrToday(String prompt) {
        String raw = readRaw(prompt + " [AAAA-MM-DD, Enter = hoy]");
        if (CANCEL_KEYWORD.equals(raw)) {
            throw new OperationCancelledException();
        }
        if (raw.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(raw, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            printError("Fecha inválida. Use el formato AAAA-MM-DD.");
            return readDateOrToday(prompt);
        }
    }

    public LocalTime readTime(String prompt) {
        return read(prompt + " [HH:MM]", raw -> LocalTime.parse(raw, TIME_FORMAT), time -> true,
                "Hora inválida. Use el formato 24 horas HH:MM (ej: 07:30).");
    }

    public boolean readYesNo(String prompt) {
        String answer = read(prompt + " (s/n)", raw -> raw.trim().toLowerCase(),
                raw -> raw.equals("s") || raw.equals("n"), "Responda 's' para sí o 'n' para no.");
        return answer.equals("s");
    }

    /**
     * Lets the user pick one value of a list (usually an enum) by its number.
     *
     * @param label  how each option is shown
     */
    public <T> T readChoice(String prompt, List<T> options, Function<T, String> label) {
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("    %s) %s%n", Ansi.CYAN.apply(String.format("%2d", i + 1)), label.apply(options.get(i)));
        }
        int index = readInt(prompt, 1, options.size());
        return options.get(index - 1);
    }

    public void pause() {
        readRaw("\n  Presione Enter para continuar");
    }

    private String readRaw(String prompt) {
        System.out.print("  " + Ansi.BRIGHT_CYAN.apply("›") + " " + prompt + ": ");
        if (!scanner.hasNextLine()) {
            // input closed (Ctrl+D): behave like a cancel instead of crashing
            throw new OperationCancelledException();
        }
        String value = scanner.nextLine().trim();
        if (MAIN_MENU_KEYWORD.equalsIgnoreCase(value)) {
            throw new ReturnToMainMenuException();
        }
        return value;
    }

    private void printError(String message) {
        System.out.println("  " + Ansi.RED.apply("✖ " + message));
    }
}
