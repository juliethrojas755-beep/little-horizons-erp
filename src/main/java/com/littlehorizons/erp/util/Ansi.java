package com.littlehorizons.erp.util;

/**
 * Códigos ANSI que sirven para dar color a la consola.
 * <p>
 * Los colores se activan automáticamente solo cuando el programa corre en una terminal real
 * ({@link System#console()} no es null). Las ventanas de salida de los IDE, como la pestaña Output de
 * NetBeans, no son terminales, así que ahí los colores se desactivan para no mostrar códigos raros.
 * </p>
 * <p>Control manual: {@code -Dlh.colors=true} fuerza los colores, {@code -Dlh.colors=false} los apaga
 * y la variable de entorno {@code NO_COLOR} también los apaga.</p>
 */
public enum Ansi {

    // Cada constante guarda el número del código ANSI (estilo o color)
    RESET("0"), BOLD("1"), DIM("2"),
    RED("31"), GREEN("32"), YELLOW("33"), BLUE("34"), MAGENTA("35"), CYAN("36"), WHITE("97"), GRAY("90"),
    BRIGHT_BLUE("94"), BRIGHT_CYAN("96"), BRIGHT_YELLOW("93"), BRIGHT_GREEN("92"), BRIGHT_MAGENTA("95");

    // Se decide una sola vez al cargar la clase si hay soporte de colores
    private static final boolean ENABLED = detectSupport();

    // Secuencia de escape completa, por ejemplo ESC[31m para rojo
    private final String code;

    Ansi(String code) {
        this.code = "\u001B[" + code + "m";
    }

    /** Si hay soporte devuelve el código; si no, devuelve texto vacío (así el color simplemente no aparece). */
    @Override
    public String toString() {
        return ENABLED ? code : "";
    }

    /** Envuelve un texto con este estilo y luego restablece el color normal. */
    public String apply(String text) {
        return this + text + RESET;
    }

    /** @return {@code true} si la consola actual muestra colores */
    public static boolean isEnabled() {
        return ENABLED;
    }

    /** Decide si se usan colores según la propiedad -Dlh.colors, la variable NO_COLOR o si hay terminal real. */
    private static boolean detectSupport() {
        String forced = System.getProperty("lh.colors");
        // Si el usuario indicó true o false, se respeta su decisión
        if (forced != null) {
            return "true".equalsIgnoreCase(forced);
        }
        // Si no, hay colores únicamente cuando NO_COLOR no existe y hay una terminal real
        return System.getenv("NO_COLOR") == null && System.console() != null;
    }
}
