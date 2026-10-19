package com.littlehorizons.erp.util;

/**
 * ANSI escape codes used to color the console.
 * <p>Colors can be turned off with {@code -Dlh.colors=false} or the {@code NO_COLOR}
 * environment variable, for consoles that do not render ANSI codes.</p>
 */
public enum Ansi {

    RESET("0"), BOLD("1"), DIM("2"),
    RED("31"), GREEN("32"), YELLOW("33"), BLUE("34"), MAGENTA("35"), CYAN("36"), WHITE("97"), GRAY("90"),
    BRIGHT_BLUE("94"), BRIGHT_CYAN("96"), BRIGHT_YELLOW("93"), BRIGHT_GREEN("92"), BRIGHT_MAGENTA("95");

    private static final boolean ENABLED = !"false".equalsIgnoreCase(System.getProperty("lh.colors"))
            && System.getenv("NO_COLOR") == null;

    private final String code;

    Ansi(String code) {
        this.code = "\u001B[" + code + "m";
    }

    @Override
    public String toString() {
        return ENABLED ? code : "";
    }

    /** Wraps a text with this style and resets it afterwards. */
    public String apply(String text) {
        return this + text + RESET;
    }

    public static boolean isEnabled() {
        return ENABLED;
    }
}
