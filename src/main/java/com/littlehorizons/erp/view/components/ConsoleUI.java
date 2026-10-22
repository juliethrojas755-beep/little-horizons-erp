package com.littlehorizons.erp.view.components;

import com.littlehorizons.erp.controller.OperationResult;
import com.littlehorizons.erp.util.Ansi;
import java.util.List;

/**
 * Piezas visuales de la consola: logo, encabezados, cajas, mensajes y barras.
 * <p>Todo el dibujo vive aquí, así todas las pantallas se ven igual (experiencia consistente) y las
 * vistas solo deciden QUÉ mostrar.</p>
 */
public final class ConsoleUI {

    // Ancho en caracteres de los encabezados y las cajas
    public static final int WIDTH = 84;

    // Dibujo del logo en arte ASCII: una línea de texto por cada fila
    private static final String[] LOGO = {
        " _     _ _   _   _        _   _            _                   ",
        "| |   (_) |_| |_| | ___  | | | | ___  _ __(_)_______  _ __  ___",
        "| |   | | __| __| |/ _ \\ | |_| |/ _ \\| '__| |_  / _ \\| '_ \\/ __|",
        "| |___| | |_| |_| |  __/ |  _  | (_) | |  | |/ / (_) | | | \\__ \\",
        "|_____|_|\\__|\\__|_|\\___| |_| |_|\\___/|_|  |_/___\\___/|_| |_|___/"
    };
    // Un color por cada fila del logo, para que se vea con degradado
    private static final Ansi[] LOGO_COLORS = {Ansi.BRIGHT_CYAN, Ansi.CYAN, Ansi.BRIGHT_BLUE, Ansi.BLUE, Ansi.MAGENTA};

    // Constructor privado: clase de utilidad con métodos estáticos
    private ConsoleUI() {
    }

    /** Limpia la pantalla si la consola entiende códigos ANSI; si no (por ejemplo en NetBeans) imprime líneas en blanco. */
    public static void clear() {
        if (Ansi.isEnabled()) {
            // ESC[H mueve el cursor arriba a la izquierda y ESC[2J borra toda la pantalla
            System.out.print("\u001B[H\u001B[2J");
            System.out.flush();
        } else {
            System.out.println("\n".repeat(3));
        }
    }

    /** Logo grande con degradado de colores que se muestra al iniciar y en el menú principal. */
    public static void banner() {
        System.out.println();
        for (int i = 0; i < LOGO.length; i++) {
            System.out.println("  " + Ansi.BOLD + LOGO_COLORS[i].apply(LOGO[i]));
        }
        System.out.println("  " + Ansi.BRIGHT_YELLOW.apply("★ ") + Ansi.WHITE.apply("Colegio Internacional Little Horizons")
                + Ansi.GRAY.apply("  ·  ERP Educativo  ·  Preescolar a Grado 11°"));
        System.out.println();
    }

    /** Encabezado de pantalla con el camino recorrido (migas de pan): "Inicio › Gestión Financiera". */
    public static void header(String title, String... breadcrumb) {
        // Camino recorrido: "Inicio" o "Inicio › Módulo › Opción"
        String path = breadcrumb.length == 0 ? "Inicio" : "Inicio › " + String.join(" › ", breadcrumb);
        System.out.println();
        // Caja doble: línea superior, título centrado en mayúsculas y línea inferior
        System.out.println(Ansi.BRIGHT_BLUE.apply("╔" + "═".repeat(WIDTH - 2) + "╗"));
        System.out.println(Ansi.BRIGHT_BLUE.apply("║") + center(Ansi.BOLD + Ansi.WHITE.apply(title.toUpperCase()), WIDTH - 2)
                + Ansi.BRIGHT_BLUE.apply("║"));
        System.out.println(Ansi.BRIGHT_BLUE.apply("╚" + "═".repeat(WIDTH - 2) + "╝"));
        System.out.println("  " + Ansi.GRAY.apply("⌂ " + path));
    }

    /** Título de sección más pequeño dentro de una pantalla. */
    public static void section(String title) {
        System.out.println();
        // Línea con el título al inicio, completada con guiones hasta el ancho total
        String line = "── " + title + " ";
        System.out.println(Ansi.BRIGHT_CYAN.apply(line + "─".repeat(Math.max(0, WIDTH - visibleLength(line)))));
    }

    // Mensajes con símbolo y color: éxito (verde), error (rojo), advertencia (amarillo) e información (cian)
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

    /**
     * Imprime el mensaje de un resultado con el color correcto (verde si salió bien, rojo si falló).
     *
     * @return {@code true} cuando la operación fue exitosa
     */
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

    /** Ficha con parejas etiqueta/valor alineadas en dos columnas. */
    public static void card(String title, List<String[]> rows) {
        // El ancho de la columna de etiquetas es el de la etiqueta más larga + 2
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
     * Barra horizontal, por ejemplo para promedios u ocupación.
     *
     * @param ratio valor entre 0 y 1 (qué parte de la barra se llena)
     * @param width cantidad total de caracteres de la barra
     * @param color color de la parte llena
     */
    public static String bar(double ratio, int width, Ansi color) {
        // Se limita la proporción al rango 0 a 1 para que la barra nunca se desborde
        double safe = Math.max(0, Math.min(1, ratio));
        // Cantidad de bloques llenos (█); el resto se rellena con bloques claros (░)
        int filled = (int) Math.round(safe * width);
        return color.apply("█".repeat(filled)) + Ansi.GRAY.apply("░".repeat(width - filled));
    }

    /** Colorea una nota según la escala de desempeño colombiana: rojo bajo, amarillo básico, verde alto y superior. */
    public static String grade(java.math.BigDecimal grade) {
        if (grade == null) {
            return Ansi.GRAY.apply("-");
        }
        // Escala: menos de 3.0 rojo, menos de 4.0 amarillo, menos de 4.6 verde y desde 4.6 verde brillante
        double value = grade.doubleValue();
        Ansi color = value < 3.0 ? Ansi.RED : value < 4.0 ? Ansi.YELLOW : value < 4.6 ? Ansi.GREEN : Ansi.BRIGHT_GREEN;
        return Ansi.BOLD + color.apply(grade.toPlainString());
    }

    /** Colorea una palabra de estado: verde para lo bueno, amarillo para advertencias y rojo para problemas. */
    public static String status(String text) {
        // switch con flechas: devuelve el texto con el color que corresponde a su estado
        return switch (text) {
            case "Activo", "Activa", "Disponible", "Devuelto", "Pagado", "Presente", "Completada" -> Ansi.GREEN.apply(text);
            case "Prestado", "Tardanza", "Licencia", "Pendiente", "En Mantenimiento", "Excusa Médica" -> Ansi.YELLOW.apply(text);
            case "Vencido", "Ausente", "Retirado", "Retirada", "Dado de Baja", "Suspendido", "Inactivo" -> Ansi.RED.apply(text);
            default -> text;
        };
    }

    /**
     * Devuelve el emoji cuando hay colores (terminal real) y un texto simple cuando la consola
     * no puede dibujar emojis, como la ventana Output de un IDE.
     */
    public static String symbol(String emoji, String plain) {
        return Ansi.isEnabled() ? emoji : plain;
    }

    public static void blank() {
        System.out.println();
    }

    // ====================== AYUDAS DE TEXTO (entienden los códigos ANSI) ======================

    /**
     * Ancho real en pantalla: ignora los códigos ANSI (no ocupan espacio) y cuenta cada emoji como dos columnas,
     * así el texto con color y las medallas quedan alineados en las tablas.
     */
    public static int visibleLength(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "").codePoints()
                .map(codePoint -> codePoint >= 0x1F300 && codePoint <= 0x1FAFF ? 2 : 1)
                .sum();
    }

    // Rellena con espacios a la derecha hasta alcanzar el ancho (texto alineado a la izquierda)
    public static String pad(String text, int width) {
        int missing = width - visibleLength(text);
        return missing > 0 ? text + " ".repeat(missing) : text;
    }

    // Rellena con espacios a la izquierda (texto alineado a la derecha, para números y dinero)
    public static String padLeft(String text, int width) {
        int missing = width - visibleLength(text);
        return missing > 0 ? " ".repeat(missing) + text : text;
    }

    // Corta un texto largo y le pone "…" al final; si es null devuelve un guion
    public static String truncate(String text, int width) {
        if (text == null) {
            return "-";
        }
        return text.length() <= width ? text : text.substring(0, Math.max(0, width - 1)) + "…";
    }

    // Centra un texto repartiendo los espacios sobrantes a ambos lados
    private static String center(String text, int width) {
        int total = width - visibleLength(text);
        int left = Math.max(0, total / 2);
        return " ".repeat(left) + text + Ansi.RESET + " ".repeat(Math.max(0, total - left));
    }
}
