package com.littlehorizons.erp.view.components;

import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.OperationCancelledException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable menu: options are registered with a label and an action (lambda).
 * <p>
 * The loop draws the menu, reads a valid option and runs it. Option 0 goes back.
 * If an action is cancelled with 0, the menu shows a notice and stays on screen.
 * </p>
 */
public class Menu {

    private record Option(String icon, String label, Runnable action) {
    }

    private final String title;
    private final String[] breadcrumb;
    private final InputReader input;
    private final List<Option> options = new ArrayList<>();
    private final List<Integer> separators = new ArrayList<>();
    private Runnable beforeDraw = () -> { };
    private String exitLabel = "Volver al menú anterior";

    public Menu(String title, InputReader input, String... breadcrumb) {
        this.title = title;
        this.input = input;
        this.breadcrumb = breadcrumb;
    }

    public Menu option(String icon, String label, Runnable action) {
        options.add(new Option(icon, label, action));
        return this;
    }

    /** Draws a separator line before the next option, to group related actions. */
    public Menu separator() {
        separators.add(options.size());
        return this;
    }

    /** Something drawn above the options every time (e.g. the dashboard). */
    public Menu beforeDraw(Runnable action) {
        this.beforeDraw = action;
        return this;
    }

    public Menu exitLabel(String label) {
        this.exitLabel = label;
        return this;
    }

    /** Shows the menu until the user picks 0. */
    public void show() {
        while (true) {
            ConsoleUI.clear();
            ConsoleUI.header(title, breadcrumb);
            beforeDraw.run();
            draw();
            int choice = input.readMenuOption("Seleccione una opción", options.size());
            if (choice == 0) {
                return;
            }
            Option option = options.get(choice - 1);
            ConsoleUI.clear();
            ConsoleUI.header(option.label(), append(breadcrumb, title));
            ConsoleUI.info(Ansi.GRAY.apply("En cualquier pregunta: 0 = cancelar y volver  ·  M = menú principal"));
            try {
                option.action().run();
            } catch (OperationCancelledException e) {
                ConsoleUI.warning("Operación cancelada.");
            }
            input.pause();
        }
    }

    private void draw() {
        System.out.println();
        for (int i = 0; i < options.size(); i++) {
            if (separators.contains(i) && i > 0) {
                System.out.println("    " + Ansi.GRAY.apply("·".repeat(40)));
            }
            Option option = options.get(i);
            System.out.printf("   %s  %s  %s%n", Ansi.BOLD + Ansi.BRIGHT_YELLOW.apply(String.format("%2d", i + 1)),
                    option.icon(), option.label());
        }
        System.out.println();
        System.out.printf("   %s  %s  %s%n", Ansi.BOLD + Ansi.GRAY.apply(" 0"), "←", Ansi.GRAY.apply(exitLabel));
        System.out.println();
    }

    private static String[] append(String[] path, String last) {
        String[] result = new String[path.length + 1];
        System.arraycopy(path, 0, result, 0, path.length);
        result[path.length] = last;
        return result;
    }
}
