package com.littlehorizons.erp.view.components;

import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.OperationCancelledException;
import java.util.ArrayList;
import java.util.List;

/**
 * Menú reutilizable: las opciones se registran con un texto y una acción (lambda o referencia a método).
 * <p>
 * El ciclo dibuja el menú, lee una opción válida y la ejecuta. La opción 0 vuelve atrás.
 * Si una acción se cancela con 0, el menú muestra un aviso y se queda en pantalla.
 * </p>
 * <p>Usa una "interfaz fluida": cada método devuelve el mismo Menu para encadenar llamadas:
 * {@code new Menu(...).option(...).option(...).show()}.</p>
 */
public class Menu {

    // Una opción del menú: ícono, texto y la acción que se ejecuta al elegirla (Runnable = código sin parámetros)
    private record Option(String icon, String label, Runnable action) {
    }

    // Título del menú y camino de navegación (migas de pan) que se muestran arriba
    private final String title;
    private final String[] breadcrumb;
    // Lector de teclado compartido por todas las pantallas
    private final InputReader input;
    // Opciones en el orden en que se agregaron (el número que ve el usuario es su posición + 1)
    private final List<Option> options = new ArrayList<>();
    // Posiciones donde se dibuja una línea separadora
    private final List<Integer> separators = new ArrayList<>();
    // Acción opcional que se ejecuta antes de dibujar las opciones (por defecto no hace nada)
    private Runnable beforeDraw = () -> { };
    // Texto de la opción 0
    private String exitLabel = "Volver al menú anterior";

    // Constructor: el camino de navegación es opcional (varargs = cero o más textos)
    public Menu(String title, InputReader input, String... breadcrumb) {
        this.title = title;
        this.input = input;
        this.breadcrumb = breadcrumb;
    }

    // Agrega una opción con su ícono, texto y acción; devuelve this para poder encadenar llamadas
    public Menu option(String icon, String label, Runnable action) {
        options.add(new Option(icon, label, action));
        return this;
    }

    /** Dibuja una línea separadora antes de la siguiente opción, para agrupar acciones relacionadas. */
    public Menu separator() {
        separators.add(options.size());
        return this;
    }

    /** Algo que se dibuja encima de las opciones cada vez (por ejemplo, el tablero). */
    public Menu beforeDraw(Runnable action) {
        this.beforeDraw = action;
        return this;
    }

    // Cambia el texto de la opción 0 (por ejemplo "Salir")
    public Menu exitLabel(String label) {
        this.exitLabel = label;
        return this;
    }

    /** Muestra el menú una y otra vez hasta que el usuario elige 0. */
    public void show() {
        // Se repite hasta que el usuario elija 0 (volver)
        while (true) {
            ConsoleUI.clear();
            ConsoleUI.header(title, breadcrumb);
            beforeDraw.run();
            draw();
            // Lee un número válido entre 0 y la cantidad de opciones
            int choice = input.readMenuOption("Seleccione una opción", options.size());
            if (choice == 0) {
                return;
            }
            // La opción 1 está en la posición 0 de la lista, por eso se resta 1
            Option option = options.get(choice - 1);
            ConsoleUI.clear();
            // Cada acción abre su propia pantalla con su camino: Inicio › Módulo › Opción
            ConsoleUI.header(option.label(), append(breadcrumb, title));
            ConsoleUI.info(Ansi.GRAY.apply("En cualquier pregunta: 0 = cancelar y volver  ·  M = menú principal"));
            try {
                // Se ejecuta la acción elegida (la lambda o referencia a método que se registró)
                option.action().run();
            } catch (OperationCancelledException e) {
                // El usuario escribió 0 en alguna pregunta: se avisa y se vuelve al menú
                ConsoleUI.warning("Operación cancelada.");
            }
            // Pausa para que alcance a leer el resultado antes de redibujar el menú
            input.pause();
        }
    }

    // Dibuja la lista numerada de opciones (con separadores) y la opción 0 al final
    private void draw() {
        System.out.println();
        for (int i = 0; i < options.size(); i++) {
            // Línea separadora para agrupar opciones relacionadas
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

    // Devuelve una copia del camino de navegación con un elemento más al final
    private static String[] append(String[] path, String last) {
        String[] result = new String[path.length + 1];
        System.arraycopy(path, 0, result, 0, path.length);
        result[path.length] = last;
        return result;
    }
}
