package com.littlehorizons.erp.util;

/**
 * ANSI escape codes used to color the console.
 * <p>
 * Colors are turned on automatically only when the program runs in a real terminal
 * ({@link System#console()} is not null). IDE output windows such as the NetBeans Output
 * tab are not terminals, so colors are disabled there to avoid raw escape codes on screen.
 * </p>
 * <p>Manual control: {@code -Dlh.colors=true} forces colors on, {@code -Dlh.colors=false} forces
 * them off, and the {@code NO_COLOR} environment variable also turns them off.</p>
 */
public enum Ansi {

    RESET("0"), BOLD("1"), DIM("2"),
    RED("31"), GREEN("32"), YELLOW("33"), BLUE("34"), MAGENTA("35"), CYAN("36"), WHITE("97"), GRAY("90"),
    BRIGHT_BLUE("94"), BRIGHT_CYAN("96"), BRIGHT_YELLOW("93"), BRIGHT_GREEN("92"), BRIGHT_MAGENTA("95");

    private static final boolean ENABLED = detectSupport();

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

    private static boolean detectSupport() {
        String forced = System.getProperty("lh.colors");
        if (forced != null) {
            return "true".equalsIgnoreCase(forced);
        }
        return System.getenv("NO_COLOR") == null && System.console() != null;
    }
}
