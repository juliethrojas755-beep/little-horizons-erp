package com.littlehorizons.erp.view;

import com.littlehorizons.erp.AppContext;
import com.littlehorizons.erp.model.dto.MedalCount;
import com.littlehorizons.erp.model.dto.StudentRanking;
import com.littlehorizons.erp.model.dto.SubjectAverage;
import com.littlehorizons.erp.model.dto.TeacherLoad;
import com.littlehorizons.erp.model.enums.PaymentMethod;
import com.littlehorizons.erp.model.enums.ResourceStatus;
import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.Formats;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.view.components.ConsoleUI;
import com.littlehorizons.erp.view.components.Menu;
import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Módulo 7: reportes y estadísticas con gráficos de barras en la consola.
 * <p>Los datos ya vienen calculados desde el servicio con Streams; esta vista solo los dibuja.</p>
 */
public class ReportView extends BaseView {

    // Título que aparece en el encabezado del menú
    private static final String MODULE = "Reportes y Estadísticas";
    // Configuración regional para escribir los nombres de los meses en español
    private static final Locale SPANISH = Locale.forLanguageTag("es-CO");
    // Largo en caracteres de las barras de los gráficos
    private static final int CHART_WIDTH = 34;

    public ReportView(AppContext app, InputReader input) {
        super(app, input);
    }

    /**
     * Arma el menú del módulo: cada opción es un texto y un método (referencia con ::).
     */
    @Override
    public void show() {
        new Menu(MODULE, input)
                .option("★", "Cuadro de honor (mejores promedios)", this::ranking)
                .option("⚠", "Estudiantes en riesgo académico", this::atRisk)
                .option("▤", "Distribución por nivel de desempeño", this::distribution)
                .option("▤", "Promedio por grado", this::averageByGrade)
                .option("▤", "Estudiantes matriculados por grado", this::studentsByGrade)
                .separator()
                .option("$", "Ingresos por concepto", this::incomeByConcept)
                .option("$", "Ingresos por mes", this::incomeByMonth)
                .option("$", "Pagos por método", this::paymentsByMethod)
                .separator()
                .option("☰", "Carga académica de profesores", this::teacherLoad)
                .option("★", "Medallero de eventos", this::medals)
                .option("▤", "Estado del inventario de recursos", this::resources)
                .show();
    }

    /**
     * Cuadro de honor: los mejores promedios del año con medallas para los tres primeros.
     */
    private void ranking() {
        // Cuántos estudiantes se muestran en el ranking
        int limit = input.readInt("¿Cuántos estudiantes mostrar?", 3, 40);
        // El servicio ya devuelve el ranking ordenado por promedio (con Streams)
        List<StudentRanking> ranking = require(app.reports().topStudents(currentYear(), limit));
        BaseView.<StudentRanking>table()
                .numberColumn("#", r -> trophy(r.position()))
                .column("Estudiante", StudentRanking::studentName)
                .column("Grado", StudentRanking::gradeName)
                .column("Rendimiento", r -> ConsoleUI.bar(r.average().doubleValue() / 5, 20, Ansi.GREEN))
                .numberColumn("Promedio", r -> ConsoleUI.grade(r.average()))
                .print(ranking);
    }

    /**
     * Estudiantes con materias por debajo de 3.0 y cuáles son.
     */
    private void atRisk() {
        // Mapa nombre del estudiante -> materias reprobadas
        Map<String, List<SubjectAverage>> risk = require(app.reports().studentsAtRisk(currentYear()));
        if (risk.isEmpty()) {
            ConsoleUI.success("Ningún estudiante tiene materias con promedio inferior a 3.0.");
            return;
        }
        ConsoleUI.warning(risk.size() + " estudiante(s) con materias por debajo de 3.0:");
        // forEach recorre el mapa: un bloque por cada estudiante en riesgo
        risk.forEach((student, subjects) -> {
            System.out.println("\n  " + Ansi.BOLD + Ansi.WHITE.apply(student));
            subjects.forEach(s -> System.out.printf("     %s %s%n", ConsoleUI.pad(s.subjectName(), 36), ConsoleUI.grade(s.average())));
        });
    }

    /**
     * Distribución de las notas según la escala nacional de desempeño, con barras y porcentajes.
     */
    private void distribution() {
        ConsoleUI.section("Escala nacional de desempeño (Decreto 1290) · " + currentYear());
        Map<String, Long> data = require(app.reports().performanceDistribution(currentYear()));
        // Total de notas: suma de todos los conteos
        long total = data.values().stream().mapToLong(Long::longValue).sum();
        // Un color por nivel, de verde (Superior) a rojo (Bajo)
        Ansi[] colors = {Ansi.BRIGHT_GREEN, Ansi.GREEN, Ansi.YELLOW, Ansi.RED};
        int index = 0;
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            // Proporción de este nivel sobre el total (evita dividir entre cero)
            double ratio = total == 0 ? 0 : entry.getValue() / (double) total;
            System.out.printf("  %s %s %s%n", ConsoleUI.pad(entry.getKey(), 22), ConsoleUI.bar(ratio, CHART_WIDTH, colors[index++]),
                    Ansi.BOLD + String.format("%3d  (%4.1f%%)", entry.getValue(), ratio * 100) + Ansi.RESET);
        }
        ConsoleUI.info("Total de notas analizadas: " + total);
    }

    /**
     * Promedio general de cada grado en un gráfico de barras.
     */
    private void averageByGrade() {
        ConsoleUI.section("Promedio general por grado · " + currentYear());
        barChart(require(app.reports().averageByGradeLevel(currentYear())), v -> v.doubleValue() / 5.0,
                v -> ConsoleUI.grade(v));
    }

    /**
     * Cantidad de estudiantes matriculados en cada grado.
     */
    private void studentsByGrade() {
        Map<String, Long> data = require(app.reports().studentsByGradeLevel(currentYear()));
        // El valor más grande sirve como 100 % de la barra
        long max = data.values().stream().mapToLong(Long::longValue).max().orElse(1);
        ConsoleUI.section("Estudiantes matriculados · " + currentYear());
        barChart(data, v -> v / (double) Math.max(1, max), v -> String.valueOf(v));
    }

    /**
     * Ingresos del año por concepto de pago.
     */
    private void incomeByConcept() {
        Map<String, BigDecimal> data = require(app.reports().incomeByConcept(currentYear()));
        ConsoleUI.section("Ingresos por concepto · " + currentYear());
        moneyChart(data);
    }

    /**
     * Ingresos del año por mes.
     */
    private void incomeByMonth() {
        Map<Month, BigDecimal> data = require(app.reports().incomeByMonth(currentYear()));
        // Se cambia el nombre del mes en inglés (Month) por su nombre en español, conservando el orden
        Map<String, BigDecimal> labeled = new LinkedHashMap<>();
        // capitalize pone la primera letra en mayúscula
        data.forEach((month, value) -> labeled.put(capitalize(month.getDisplayName(TextStyle.FULL, SPANISH)), value));
        ConsoleUI.section("Ingresos por mes · " + currentYear());
        moneyChart(labeled);
    }

    /**
     * Número de pagos por método de pago.
     */
    private void paymentsByMethod() {
        Map<PaymentMethod, Long> data = require(app.reports().paymentsByMethod(currentYear()));
        // Se cambia la llave del enum por el texto en español del método de pago
        Map<String, Long> labeled = new LinkedHashMap<>();
        data.forEach((method, count) -> labeled.put(method.getDbValue(), count));
        long max = labeled.values().stream().mapToLong(Long::longValue).max().orElse(1);
        ConsoleUI.section("Pagos por método · " + currentYear());
        barChart(labeled, v -> v / (double) max, v -> v + " pagos");
    }

    /**
     * Carga académica de cada profesor en una tabla.
     */
    private void teacherLoad() {
        BaseView.<TeacherLoad>table()
                .column("Profesor", TeacherLoad::teacherName)
                .column("Especialización", TeacherLoad::specialization)
                .numberColumn("Materias", t -> String.valueOf(t.subjects()))
                .numberColumn("Bloques/sem", t -> String.valueOf(t.weeklyBlocks()))
                .print(require(app.reports().teacherLoad(currentYear())));
    }

    /**
     * Medallero de los eventos con competencia.
     */
    private void medals() {
        BaseView.<MedalCount>table()
                .column("Estudiante", MedalCount::studentName)
                .numberColumn(ConsoleUI.symbol("🥇 ", "") + "Oro", m -> String.valueOf(m.gold()))
                .numberColumn(ConsoleUI.symbol("🥈 ", "") + "Plata", m -> String.valueOf(m.silver()))
                .numberColumn(ConsoleUI.symbol("🥉 ", "") + "Bronce", m -> String.valueOf(m.bronze()))
                .numberColumn("Total", m -> Ansi.BOLD.apply(String.valueOf(m.total())))
                .print(require(app.reports().medalTable()));
    }

    /**
     * Estado del inventario de recursos con barras.
     */
    private void resources() {
        // Conteo de recursos por estado
        Map<ResourceStatus, Long> data = require(app.reports().resourcesByStatus());
        long total = data.values().stream().mapToLong(Long::longValue).sum();
        ConsoleUI.section("Inventario: " + total + " recursos");
        // Se recorren todos los estados para mostrar también los que tienen 0 recursos
        for (ResourceStatus status : ResourceStatus.values()) {
            long count = data.getOrDefault(status, 0L);
            System.out.printf("  %s %s %s%n", ConsoleUI.pad(ConsoleUI.status(status.getDbValue()), 18),
                    ConsoleUI.bar(total == 0 ? 0 : count / (double) total, CHART_WIDTH, Ansi.BRIGHT_BLUE), count);
        }
    }

    // ====================== AYUDAS PARA LOS GRÁFICOS ======================

    /**
     * Gráfico de barras horizontal genérico: etiqueta, barra y valor formateado.
     * Es genérico (V): sirve para números, dinero o conteos según las funciones que reciba.
     *
     * @param ratio  función que convierte cada valor en una proporción de 0 a 1 (largo de la barra)
     * @param format función que convierte cada valor en el texto que se muestra al lado
     */
    private <V> void barChart(Map<String, V> data, Function<V, Double> ratio, Function<V, String> format) {
        // Si no hay datos solo se avisa
        if (data.isEmpty()) {
            ConsoleUI.info("No hay datos para el año " + currentYear() + ".");
            return;
        }
        // Una línea por dato: etiqueta, barra y valor
        data.forEach((label, value) -> System.out.printf("  %s %s %s%n", ConsoleUI.pad(label, 22),
                ConsoleUI.bar(ratio.apply(value), CHART_WIDTH, Ansi.BRIGHT_CYAN), format.apply(value)));
    }

    /**
     * Gráfico de barras para valores de dinero, con el total al final.
     */
    private void moneyChart(Map<String, BigDecimal> data) {
        // El mayor ingreso equivale a la barra completa
        BigDecimal max = data.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        barChart(data, v -> v.doubleValue() / max.doubleValue(), v -> Ansi.BOLD + Formats.money(v) + Ansi.RESET);
        BigDecimal total = data.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        ConsoleUI.info("Total: " + Ansi.BOLD + Ansi.BRIGHT_GREEN.apply(Formats.money(total)));
    }

    /**
     * Número del puesto; los tres primeros llevan medalla.
     */
    private static String trophy(int position) {
        // switch con flechas: devuelve el número con su medalla según el puesto
        return switch (position) {
            case 1 -> Ansi.BRIGHT_YELLOW.apply(ConsoleUI.symbol("🥇 ", "") + "1");
            case 2 -> ConsoleUI.symbol("🥈 ", "") + "2";
            case 3 -> Ansi.YELLOW.apply(ConsoleUI.symbol("🥉 ", "") + "3");
            default -> String.valueOf(position);
        };
    }

    /**
     * Convierte la primera letra en mayúscula (enero -> Enero).
     */
    private static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
