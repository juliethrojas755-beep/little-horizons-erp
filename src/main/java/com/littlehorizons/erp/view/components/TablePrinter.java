package com.littlehorizons.erp.view.components;

import com.littlehorizons.erp.util.Ansi;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Prints a list of objects as a boxed table with automatic column widths.
 * <p>Columns are declared with lambdas: {@code table.column("Nombre", Student::getFullName)}.
 * The same printer works for any entity (generics + functional interfaces).</p>
 *
 * @param <T> type of each row
 */
public class TablePrinter<T> {

    private static final int MAX_COLUMN_WIDTH = 40;

    private record Column<T>(String title, Function<T, String> value, boolean rightAligned) {
    }

    private final List<Column<T>> columns = new ArrayList<>();

    public TablePrinter<T> column(String title, Function<T, String> value) {
        columns.add(new Column<>(title, value, false));
        return this;
    }

    /** Column aligned to the right (numbers, money). */
    public TablePrinter<T> numberColumn(String title, Function<T, String> value) {
        columns.add(new Column<>(title, value, true));
        return this;
    }

    public void print(List<T> rows) {
        if (rows.isEmpty()) {
            ConsoleUI.info("No hay registros para mostrar.");
            return;
        }
        List<List<String>> cells = rows.stream()
                .map(row -> columns.stream()
                        .map(column -> {
                            String text = column.value().apply(row);
                            return text == null ? "-" : text;
                        })
                        .toList())
                .toList();
        int[] widths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            final int index = i;
            int contentWidth = cells.stream().mapToInt(line -> ConsoleUI.visibleLength(line.get(index))).max().orElse(0);
            widths[i] = Math.min(MAX_COLUMN_WIDTH, Math.max(columns.get(i).title().length(), contentWidth));
        }

        System.out.println("  " + Ansi.GRAY.apply(border('┌', '┬', '┐', widths)));
        StringBuilder header = new StringBuilder("  " + Ansi.GRAY.apply("│"));
        for (int i = 0; i < columns.size(); i++) {
            header.append(' ').append(Ansi.BOLD).append(Ansi.BRIGHT_CYAN.apply(ConsoleUI.pad(columns.get(i).title(), widths[i])))
                    .append(' ').append(Ansi.GRAY.apply("│"));
        }
        System.out.println(header);
        System.out.println("  " + Ansi.GRAY.apply(border('├', '┼', '┤', widths)));
        for (List<String> line : cells) {
            StringBuilder row = new StringBuilder("  " + Ansi.GRAY.apply("│"));
            for (int i = 0; i < columns.size(); i++) {
                String value = fit(line.get(i), widths[i]);
                row.append(' ').append(columns.get(i).rightAligned() ? ConsoleUI.padLeft(value, widths[i])
                        : ConsoleUI.pad(value, widths[i])).append(' ').append(Ansi.GRAY.apply("│"));
            }
            System.out.println(row);
        }
        System.out.println("  " + Ansi.GRAY.apply(border('└', '┴', '┘', widths)));
        System.out.println("  " + Ansi.GRAY.apply(rows.size() + (rows.size() == 1 ? " registro" : " registros")));
    }

    /** Cuts plain text that is too long; colored text is short and kept as is. */
    private String fit(String value, int width) {
        return ConsoleUI.visibleLength(value) > width && value.equals(value.replaceAll("\u001B\\[[;\\d]*m", ""))
                ? ConsoleUI.truncate(value, width)
                : value;
    }

    private String border(char left, char middle, char right, int[] widths) {
        StringBuilder line = new StringBuilder().append(left);
        for (int i = 0; i < widths.length; i++) {
            line.append("─".repeat(widths[i] + 2)).append(i == widths.length - 1 ? right : middle);
        }
        return line.toString();
    }
}
