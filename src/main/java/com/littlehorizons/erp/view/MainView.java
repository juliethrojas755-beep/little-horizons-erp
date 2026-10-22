package com.littlehorizons.erp.view;

import com.littlehorizons.erp.AppContext;
import com.littlehorizons.erp.controller.OperationResult;
import com.littlehorizons.erp.model.dto.DashboardStats;
import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.Formats;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.ReturnToMainMenuException;
import com.littlehorizons.erp.view.components.ConsoleUI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Menú principal con las 8 opciones del enunciado y un tablero de indicadores arriba.
 * <p>Cada opción del 1 al 7 abre la vista de un módulo; la 8 sale del sistema.</p>
 */
public class MainView {

    // Formato de la fecha de hoy en español: "lunes 19 de octubre de 2026"
    private static final DateTimeFormatter TODAY = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy",
            Locale.forLanguageTag("es-CO"));

    // Acceso a los controladores
    private final AppContext app;
    // Lector de teclado compartido
    private final InputReader input;
    // Vistas de los módulos 1 a 7 (todas heredan de BaseView, por eso caben en una misma lista: polimorfismo)
    private final List<BaseView> modules;

    // Constructor: arma las vistas de los siete módulos con las mismas dependencias
    public MainView(AppContext app, InputReader input) {
        this.app = app;
        this.input = input;
        // El orden de esta lista coincide con los números del menú, del 1 al 7
        this.modules = List.of(new StudentView(app, input), new AcademicView(app, input), new TeacherView(app, input),
                new ExtracurricularView(app, input), new FinanceView(app, input), new ResourceView(app, input),
                new ReportView(app, input));
    }

    /** Muestra el menú principal una y otra vez hasta que el usuario decide salir. */
    public void show() {
        // Ciclo principal del programa: dibuja, lee la opción y abre el módulo elegido
        while (true) {
            ConsoleUI.clear();
            ConsoleUI.banner();
            dashboard();
            printOptions();
            // Opción de 0 a 8 (el 0 también se trata como salir)
            int option = input.readMenuOption("Seleccione un módulo", 8);
            // Salir: se pide confirmación para evitar cerrar por error
            if (option == 8 || option == 0) {
                if (input.readYesNo("¿Seguro que desea salir del sistema?")) {
                    return;
                }
                continue;
            }
            try {
                // La opción 1 corresponde a la posición 0 de la lista de módulos
                modules.get(option - 1).show();
            } catch (ReturnToMainMenuException e) {
                // El usuario escribió "M" en algún punto de un módulo: simplemente se vuelve a dibujar el menú principal
            }
        }
    }

    /** Muestra la fecha y las seis cifras del tablero (estudiantes, profesores, clubes, recaudo, cargos y préstamos). */
    private void dashboard() {
        OperationResult<DashboardStats> result = app.reports().dashboard();
        String date = LocalDate.now().format(TODAY);
        System.out.println("  " + Ansi.GRAY.apply("Hoy es " + date));
        if (!result.success()) {
            ConsoleUI.error("No se pudo cargar el resumen: " + result.message());
            return;
        }
        DashboardStats stats = result.data();
        System.out.println();
        System.out.println("  " + tile("Estudiantes activos", String.valueOf(stats.activeStudents()), Ansi.BRIGHT_CYAN)
                + tile("Profesores activos", String.valueOf(stats.activeTeachers()), Ansi.BRIGHT_BLUE)
                + tile("Clubes", String.valueOf(stats.activeClubs()), Ansi.BRIGHT_MAGENTA));
        System.out.println("  " + tile("Recaudo del mes", Formats.money(stats.incomeThisMonth()), Ansi.BRIGHT_GREEN)
                + tile("Cargos pendientes", String.valueOf(stats.pendingCharges()), Ansi.BRIGHT_YELLOW)
                + tile("Préstamos abiertos", String.valueOf(stats.openLoans()), Ansi.YELLOW));
    }

    /** Dibuja una cifra con su etiqueta, en un ancho fijo de 28 caracteres para que las columnas queden alineadas. */
    private String tile(String label, String value, Ansi color) {
        return ConsoleUI.pad(Ansi.GRAY.apply("▌") + " " + Ansi.BOLD + color.apply(value) + " " + Ansi.GRAY.apply(label), 28);
    }

    /** Dibuja la caja con las ocho opciones del menú: número, nombre del módulo y descripción corta. */
    private void printOptions() {
        String[][] options = {
            {"1", "Gestión de Estudiantes", "Fichas, acudientes, matrículas"},
            {"2", "Gestión Académica", "Materias, notas, promedios, asistencia"},
            {"3", "Gestión de Profesores", "Directorio, asignaciones, horarios"},
            {"4", "Actividades Extracurriculares", "Clubes y eventos"},
            {"5", "Gestión Financiera", "Pagos, facturas PDF, morosos"},
            {"6", "Gestión de Recursos", "Biblioteca, laboratorios, préstamos"},
            {"7", "Reportes y Estadísticas", "Rankings, gráficos, indicadores"},
            {"8", "Salir", ""}
        };
        System.out.println();
        System.out.println(Ansi.BRIGHT_BLUE.apply("  ┌" + "─".repeat(ConsoleUI.WIDTH - 4) + "┐"));
        for (String[] option : options) {
            String line = " " + Ansi.BOLD + Ansi.BRIGHT_YELLOW.apply(option[0]) + "  " + ConsoleUI.pad(Ansi.BOLD
                    + Ansi.WHITE.apply(option[1]), 32) + Ansi.GRAY.apply(option[2]);
            System.out.println(Ansi.BRIGHT_BLUE.apply("  │") + ConsoleUI.pad(line, ConsoleUI.WIDTH - 4) + Ansi.BRIGHT_BLUE.apply("│"));
        }
        System.out.println(Ansi.BRIGHT_BLUE.apply("  └" + "─".repeat(ConsoleUI.WIDTH - 4) + "┘"));
        System.out.println("  " + Ansi.GRAY.apply("Atajos: 0 = volver/cancelar  ·  M = menú principal desde cualquier pantalla"));
        System.out.println();
    }
}
