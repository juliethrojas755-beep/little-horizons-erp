package com.littlehorizons.erp.view.components;

import com.littlehorizons.erp.util.Ansi;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Imprime una lista de objetos como una tabla con bordes y ancho de columna automático.
 * <p>Las columnas se declaran con lambdas: {@code table.column("Nombre", Student::getFullName)}.
 * La misma clase sirve para cualquier entidad (genéricos + interfaces funcionales).</p>
 *
 * @param <T> tipo de cada fila (Student, Payment...)
 */
public class TablePrinter<T> {

    // Ancho máximo de una columna para que la tabla no se salga de la pantalla
    private static final int MAX_COLUMN_WIDTH = 40;

    // Una columna: su título, cómo sacar el valor de cada fila (función) y si va alineada a la derecha
    private record Column<T>(String title, Function<T, String> value, boolean rightAligned) {
    }

    // Columnas en el orden en que se declararon
    private final List<Column<T>> columns = new ArrayList<>();

    // Agrega una columna de texto; devuelve this para encadenar: table.column(...).column(...).print(...)
    public TablePrinter<T> column(String title, Function<T, String> value) {
        columns.add(new Column<>(title, value, false));
        return this;
    }

    /** Columna alineada a la derecha (números y dinero). */
    public TablePrinter<T> numberColumn(String title, Function<T, String> value) {
        columns.add(new Column<>(title, value, true));
        return this;
    }

    // Dibuja la tabla completa: bordes, encabezado y filas
    public void print(List<T> rows) {
        // Si no hay datos solo se muestra un aviso
        if (rows.isEmpty()) {
            ConsoleUI.info("No hay registros para mostrar.");
            return;
        }
        // 1) Se calcula el texto de cada celda: por cada fila se aplica la función de cada columna
        List<List<String>> cells = rows.stream()
                .map(row -> columns.stream()
                        .map(column -> {
                            String text = column.value().apply(row);
                            return text == null ? "-" : text;
                        })
                        .toList())
                .toList();
        // 2) Se calcula el ancho de cada columna: el mayor entre el título y el contenido (máximo 40)
        int[] widths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            final int index = i;
            int contentWidth = cells.stream().mapToInt(line -> ConsoleUI.visibleLength(line.get(index))).max().orElse(0);
            widths[i] = Math.min(MAX_COLUMN_WIDTH, Math.max(columns.get(i).title().length(), contentWidth));
        }

        // 3) Borde superior
        System.out.println("  " + Ansi.GRAY.apply(border('┌', '┬', '┐', widths)));
        // 4) Fila de encabezado con los títulos en negrita
        StringBuilder header = new StringBuilder("  " + Ansi.GRAY.apply("│"));
        for (int i = 0; i < columns.size(); i++) {
            header.append(' ').append(Ansi.BOLD).append(Ansi.BRIGHT_CYAN.apply(ConsoleUI.pad(columns.get(i).title(), widths[i])))
                    .append(' ').append(Ansi.GRAY.apply("│"));
        }
        System.out.println(header);
        // 5) Línea que separa el encabezado de los datos
        System.out.println("  " + Ansi.GRAY.apply(border('├', '┼', '┤', widths)));
        // 6) Una línea de texto por cada fila de datos
        for (List<String> line : cells) {
            StringBuilder row = new StringBuilder("  " + Ansi.GRAY.apply("│"));
            for (int i = 0; i < columns.size(); i++) {
                // Se recorta si es muy largo y se alinea a la izquierda o a la derecha según la columna
                String value = fit(line.get(i), widths[i]);
                row.append(' ').append(columns.get(i).rightAligned() ? ConsoleUI.padLeft(value, widths[i])
                        : ConsoleUI.pad(value, widths[i])).append(' ').append(Ansi.GRAY.apply("│"));
            }
            System.out.println(row);
        }
        // 7) Borde inferior y total de registros
        System.out.println("  " + Ansi.GRAY.apply(border('└', '┴', '┘', widths)));
        System.out.println("  " + Ansi.GRAY.apply(rows.size() + (rows.size() == 1 ? " registro" : " registros")));
    }

    /** Corta el texto simple que sea demasiado largo; el texto con color es corto y se deja igual. */
    private String fit(String value, int width) {
        return ConsoleUI.visibleLength(value) > width && value.equals(value.replaceAll("\u001B\\[[;\\d]*m", ""))
                ? ConsoleUI.truncate(value, width)
                : value;
    }

    // Arma una línea horizontal del borde: esquina izquierda, tramos por columna, uniones y esquina derecha
    private String border(char left, char middle, char right, int[] widths) {
        StringBuilder line = new StringBuilder().append(left);
        for (int i = 0; i < widths.length; i++) {
            line.append("─".repeat(widths[i] + 2)).append(i == widths.length - 1 ? right : middle);
        }
        return line.toString();
    }
}
