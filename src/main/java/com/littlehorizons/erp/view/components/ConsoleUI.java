package com.littlehorizons.erp.view.components;

import com.littlehorizons.erp.controller.OperationResult;
import com.littlehorizons.erp.util.Ansi;
import java.util.List;

/**
 * Visual building blocks of the console: banner, headers, boxes, messages and bars.
 * <p>All the drawing lives here, so every screen looks the same (consistent UX) and the
 * views only decide WHAT to show.</p>
 */
public final class ConsoleUI {

    public static final int WIDTH = 84;

    private static final String[] LOGO = {
        " _     _ _   _   _        _   _            _                   ",
        "| |   (_) |_| |_| | ___  | | | | ___  _ __(_)_______  _ __  ___",
        "| |   | | __| __| |/ _ \\ | |_| |/ _ \\| '__| |_  / _ \\| '_ \\/ __|",
        "| |___| | |_| |_| |  __/ |  _  | (_) | |  | |/ / (_) | | | \\__ \\",
        "|_____|_|\\__|\\__|_|\\___| |_| |_|\\___/|_|  |_/___\\___/|_| |_|___/"
    };
    private static final Ansi[] LOGO_COLORS = {Ansi.BRIGHT_CYAN, Ansi.CYAN, Ansi.BRIGHT_BLUE, Ansi.BLUE, Ansi.MAGENTA};

    private ConsoleUI() {
    }

    /** Clears the screen when the console understands ANSI codes; otherwise prints blank lines. */
    public static void clear() {
        if (Ansi.isEnabled()) {
            System.out.print("\u001B[H\u001B[2J");
            System.out.flush();
        } else {
            System.out.println("\n".repeat(3));
        }
    }

    /** Big gradient logo shown at start and on the main menu. */
    public static void banner() {
        System.out.println();
        for (int i = 0; i < LOGO.length; i++) {
            System.out.println("  " + Ansi.BOLD + LOGO_COLORS[i].apply(LOGO[i]));
        }
        System.out.println("  " + Ansi.BRIGHT_YELLOW.apply("★ ") + Ansi.WHITE.apply("Colegio Internacional Little Horizons")
                + Ansi.GRAY.apply("  ·  ERP Educativo  ·  Preescolar a Grado 11°"));
        System.out.println();
    }

    /** Screen header with breadcrumb: "Inicio › Gestión Financiera". */
    public static void header(String title, String... breadcrumb) {
        String path = breadcrumb.length == 0 ? "Inicio" : "Inicio › " + String.join(" › ", breadcrumb);
        System.out.println();
        System.out.println(Ansi.BRIGHT_BLUE.apply("╔" + "═".repeat(WIDTH - 2) + "╗"));
        System.out.println(Ansi.BRIGHT_BLUE.apply("║") + center(Ansi.BOLD + Ansi.WHITE.apply(title.toUpperCase()), WIDTH - 2)
                + Ansi.BRIGHT_BLUE.apply("║"));
        System.out.println(Ansi.BRIGHT_BLUE.apply("╚" + "═".repeat(WIDTH - 2) + "╝"));
        System.out.println("  " + Ansi.GRAY.apply("⌂ " + path));
    }

    /** Smaller section title inside a screen. */
    public static void section(String title) {
        System.out.println();
        String line = "── " + title + " ";
        System.out.println(Ansi.BRIGHT_CYAN.apply(line + "─".repeat(Math.max(0, WIDTH - visibleLength(line)))));
    }

    public static void success(String message) {
        System.out.println("  " + Ansi.BRIGHT_GREEN.apply("✔ " + message));
    }

    public static void error(String message) {
        System.out.println("  " + Ansi.RED.apply("✖ " + message));
    }

    public static void warning(String message) {
        System.out.println("  " + Ansi.BRIGHT_YELLOW.apply("⚠ " + message));
    }

    public static void info(String message) {
        System.out.println("  " + Ansi.CYAN.apply("ℹ ") + message);
    }

    /** Prints the result message with the right color. @return {@code true} when it succeeded */
    public static boolean show(OperationResult<?> result) {
        if (result.success()) {
            if (result.message() != null) {
                success(result.message());
            }
        } else {
            error(result.message());
        }
        return result.success();
    }

    /** Label/value pairs aligned in two columns, like a record card. */
    public static void card(String title, List<String[]> rows) {
        int labelWidth = rows.stream().mapToInt(row -> row[0].length()).max().orElse(10) + 2;
        System.out.println("  " + Ansi.BRIGHT_BLUE.apply("┌─ ") + Ansi.BOLD + Ansi.WHITE.apply(title) + " "
                + Ansi.BRIGHT_BLUE.apply("─".repeat(Math.max(2, WIDTH - 7 - title.length()))));
        for (String[] row : rows) {
            System.out.println("  " + Ansi.BRIGHT_BLUE.apply("│ ") + Ansi.GRAY.apply(pad(row[0] + ":", labelWidth))
                    + (row[1] == null ? "-" : row[1]));
        }
        System.out.println("  " + Ansi.BRIGHT_BLUE.apply("└" + "─".repeat(WIDTH - 4)));
    }

    /**
     * Horizontal bar, e.g. for averages or occupancy.
     *
     * @param ratio value between 0 and 1
     */
    public static String bar(double ratio, int width, Ansi color) {
        double safe = Math.max(0, Math.min(1, ratio));
        int filled = (int) Math.round(safe * width);
        return color.apply("█".repeat(filled)) + Ansi.GRAY.apply("░".repeat(width - filled));
    }

    /** Colors a grade by the Colombian performance scale. */
    public static String grade(java.math.BigDecimal grade) {
        if (grade == null) {
            return Ansi.GRAY.apply("-");
        }
        double value = grade.doubleValue();
        Ansi color = value < 3.0 ? Ansi.RED : value < 4.0 ? Ansi.YELLOW : value < 4.6 ? Ansi.GREEN : Ansi.BRIGHT_GREEN;
        return Ansi.BOLD + color.apply(grade.toPlainString());
    }

    /** Colors a status word: green for good states, yellow for warnings, red for problems. */
    public static String status(String text) {
        return switch (text) {
            case "Activo", "Activa", "Disponible", "Devuelto", "Pagado", "Presente", "Completada" -> Ansi.GREEN.apply(text);
            case "Prestado", "Tardanza", "Licencia", "Pendiente", "En Mantenimiento", "Excusa Médica" -> Ansi.YELLOW.apply(text);
            case "Vencido", "Ausente", "Retirado", "Retirada", "Dado de Baja", "Suspendido", "Inactivo" -> Ansi.RED.apply(text);
            default -> text;
        };
    }

    /**
     * Returns the emoji when colors are enabled (real terminal) and a plain text
     * when the console cannot draw emojis, like an IDE output window.
     */
    public static String symbol(String emoji, String plain) {
        return Ansi.isEnabled() ? emoji : plain;
    }

    public static void blank() {
        System.out.println();
    }

    // ---------- text helpers (ANSI aware) ----------

    /**
     * Width on screen: ignores ANSI codes and counts emojis as two columns,
     * so colored text and medals align correctly in tables.
     */
    public static int visibleLength(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "").codePoints()
                .map(codePoint -> codePoint >= 0x1F300 && codePoint <= 0x1FAFF ? 2 : 1)
                .sum();
    }

    public static String pad(String text, int width) {
        int missing = width - visibleLength(text);
        return missing > 0 ? text + " ".repeat(missing) : text;
    }

    public static String padLeft(String text, int width) {
        int missing = width - visibleLength(text);
        return missing > 0 ? " ".repeat(missing) + text : text;
    }

    public static String truncate(String text, int width) {
        if (text == null) {
            return "-";
        }
        return text.length() <= width ? text : text.substring(0, Math.max(0, width - 1)) + "…";
    }

    private static String center(String text, int width) {
        int total = width - visibleLength(text);
        int left = Math.max(0, total / 2);
        return " ".repeat(left) + text + Ansi.RESET + " ".repeat(Math.max(0, total - left));
    }
}
