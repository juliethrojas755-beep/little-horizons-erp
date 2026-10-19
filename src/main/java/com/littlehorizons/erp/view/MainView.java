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
 * Main menu with the 8 options of the requirement and a live dashboard on top.
 */
public class MainView {

    private static final DateTimeFormatter TODAY = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy",
            Locale.forLanguageTag("es-CO"));

    private final AppContext app;
    private final InputReader input;
    private final List<BaseView> modules;

    public MainView(AppContext app, InputReader input) {
        this.app = app;
        this.input = input;
        // The order matches the menu numbers 1 to 7
        this.modules = List.of(new StudentView(app, input), new AcademicView(app, input), new TeacherView(app, input),
                new ExtracurricularView(app, input), new FinanceView(app, input), new ResourceView(app, input),
                new ReportView(app, input));
    }

    public void show() {
        while (true) {
            ConsoleUI.clear();
            ConsoleUI.banner();
            dashboard();
            printOptions();
            int option = input.readMenuOption("Seleccione un módulo", 8);
            if (option == 8 || option == 0) {
                if (input.readYesNo("¿Seguro que desea salir del sistema?")) {
                    return;
                }
                continue;
            }
            try {
                modules.get(option - 1).show();
            } catch (ReturnToMainMenuException e) {
                // "M" typed somewhere inside a module: just redraw the main menu
            }
        }
    }

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

    private String tile(String label, String value, Ansi color) {
        return ConsoleUI.pad(Ansi.GRAY.apply("▌") + " " + Ansi.BOLD + color.apply(value) + " " + Ansi.GRAY.apply(label), 28);
    }

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
