package com.littlehorizons.erp.view;

import com.littlehorizons.erp.AppContext;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.dto.AbsencePattern;
import com.littlehorizons.erp.model.dto.SubjectAverage;
import com.littlehorizons.erp.model.enums.AttendanceStatus;
import com.littlehorizons.erp.model.enums.Term;
import com.littlehorizons.erp.service.AttendanceService;
import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.Formats;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.view.components.ConsoleUI;
import com.littlehorizons.erp.view.components.Menu;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Módulo 2: materias, inscripción en materias, calificaciones, promedios y asistencia.
 * <p>Solo pide datos, llama al controlador y dibuja el resultado: los cálculos de promedios están en el servicio.</p>
 */
public class AcademicView extends BaseView {

    // Título que aparece en el encabezado del menú
    private static final String MODULE = "Gestión Académica";
    // Períodos en los que se puede inscribir y calificar (la nota FINAL se calcula aparte)
    private static final List<Term> TERMS = List.of(Term.I, Term.II, Term.III, Term.IV);

    public AcademicView(AppContext app, InputReader input) {
        super(app, input);
    }

    /**
     * Arma el menú del módulo: cada opción es un texto y un método (referencia con ::).
     */
    @Override
    public void show() {
        new Menu(MODULE, input)
                .option("☰", "Materias por grado", this::subjectsByGrade)
                .option("✚", "Crear materia", this::createSubject)
                .option("✎", "Inscribir estudiante en materias del período", this::enrollSubjects)
                .separator()
                .option("✎", "Registrar calificación", this::recordGrade)
                .option("★", "Calcular nota final de una materia", this::closeFinal)
                .option("▤", "Boletín del estudiante (promedios)", this::reportCard)
                .option("▤", "Promedio del curso por materia", this::courseAverage)
                .option("☰", "Historial académico completo", this::history)
                .separator()
                .option("✔", "Registrar asistencia", this::registerAttendance)
                .option("☰", "Asistencia de un estudiante", this::studentAttendance)
                .option("⚠", "Patrones de inasistencia (más de 3 por semana)", this::absencePatterns)
                .show();
    }

    /**
     * Muestra las materias de un grado en una tabla.
     */
    private void subjectsByGrade() {
        GradeLevel grade = chooseGrade();
        BaseView.<Subject>table()
                .column("Código", Subject::getCode)
                .column("Materia", Subject::getName)
                .numberColumn("Horas/sem", s -> String.valueOf(s.getWeeklyHours()))
                .column("Estado", s -> ConsoleUI.status(s.isActive() ? "Activa" : "Retirada"))
                .print(require(app.academic().findSubjectsByGrade(grade.getId())));
    }

    /**
     * Pide los datos de una materia nueva y la crea.
     */
    private void createSubject() {
        Subject subject = new Subject();
        subject.setGradeLevelId(chooseGrade().getId());
        // El código se guarda en mayúsculas (cie-5 pasa a CIE-5)
        subject.setCode(input.readText("Código (ej: CIE-5)").toUpperCase());
        subject.setName(input.readText("Nombre"));
        subject.setDescription(input.readOptionalText("Descripción"));
        subject.setWeeklyHours(input.readInt("Horas semanales", 1, 10));
        ConsoleUI.show(app.academic().createSubject(subject));
    }

    /**
     * Inscribe a un estudiante en las materias de un período (todas las de su grado o una sola) y muestra sus inscripciones.
     */
    private void enrollSubjects() {
        Student student = selectStudent();
        Term term = input.readChoice("Período", TERMS, Term::getDbValue);
        if (input.readYesNo("¿Inscribir en TODAS las materias de su grado?")) {
            // Inscribe en todas las materias de su grado que todavía no tenga en ese período
            Long created = require(app.academic().enrollInAllGradeSubjects(student.getId(), currentYear(), term));
            ConsoleUI.success("Se crearon " + created + " inscripciones para el período " + term + ".");
        } else {
            Subject subject = selectSubjectOfGrade(gradeLevelOf(student));
            ConsoleUI.show(app.academic().enrollInSubject(student.getId(), subject.getId(), currentYear(), term));
        }
        // Tabla con las inscripciones del año actual para confirmar lo que quedó registrado
        ConsoleUI.section("Inscripciones del estudiante en " + currentYear());
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        BaseView.<SubjectEnrollment>table()
                .column("Período", e -> e.getTerm().getDbValue())
                .column("Materia", e -> subjects.get(e.getSubjectId()).getName())
                .column("Estado", e -> ConsoleUI.status(e.getStatus().getDbValue()))
                .print(require(app.academic().getSubjectEnrollments(student.getId())).stream()
                        .filter(e -> e.getAcademicYear() == currentYear()).toList());
    }

    /**
     * Registra la nota de un estudiante en una materia y período.
     */
    private void recordGrade() {
        Student student = selectStudent();
        Subject subject = selectSubjectOfGrade(gradeLevelOf(student));
        Term term = input.readChoice("Período", TERMS, Term::getDbValue);
        // readGrade solo acepta notas de 0.0 a 5.0 con un decimal y repite la pregunta si no es válida
        BigDecimal grade = input.readGrade("Nota (0.0 a 5.0)");
        String notes = input.readOptionalText("Observaciones");
        // show() imprime el mensaje del controlador y devuelve true si se guardó la nota
        if (ConsoleUI.show(app.academic().recordGrade(student.getId(), subject.getId(), currentYear(), term, grade, notes))) {
            ConsoleUI.info(student.getFullName() + " · " + subject.getName() + " · período " + term + " → "
                    + ConsoleUI.grade(grade));
        }
    }

    /**
     * Calcula la nota final de una materia como promedio de los períodos.
     */
    private void closeFinal() {
        Student student = selectStudent();
        Subject subject = selectSubjectOfGrade(gradeLevelOf(student));
        // El servicio promedia las notas de los períodos y guarda el resultado como nota FINAL
        BigDecimal finalGrade = require(app.academic().closeFinalGrade(student.getId(), subject.getId(), currentYear()));
        ConsoleUI.success("Nota final de " + subject.getName() + ": " + ConsoleUI.grade(finalGrade));
    }

    /**
     * Boletín: promedio por materia con barras y promedio general por período.
     */
    private void reportCard() {
        Student student = selectStudent();
        int year = currentYear();
        ConsoleUI.section("Boletín " + year + " · " + student.getFullName());
        // Promedio de cada materia (los períodos I a IV)
        List<SubjectAverage> averages = require(app.academic().averageBySubject(student.getId(), year));
        if (averages.isEmpty()) {
            ConsoleUI.info("El estudiante no tiene notas en " + year + ".");
            return;
        }
        // Una línea por materia: nombre, barra proporcional a la nota (de 0 a 5) y el promedio con color
        for (SubjectAverage average : averages) {
            System.out.printf("  %s %s  %s %s%n", ConsoleUI.pad(ConsoleUI.truncate(average.subjectName(), 34), 34),
                    ConsoleUI.bar(average.average().doubleValue() / 5.0, 30, barColor(average.average())),
                    ConsoleUI.grade(average.average()), Ansi.GRAY.apply("(" + average.gradesCount() + " notas)"));
        }
        // Promedio general de todas las materias en cada período
        ConsoleUI.section("Promedio general por período");
        for (Term term : TERMS) {
            // Optional vacío significa que el estudiante todavía no tiene notas en ese período
            Optional<BigDecimal> average = require(app.academic().termAverage(student.getId(), year, term));
            System.out.printf("  Período %-4s %s%n", term.getDbValue(),
                    average.map(value -> ConsoleUI.bar(value.doubleValue() / 5.0, 30, barColor(value)) + "  "
                            + ConsoleUI.grade(value)).orElse(Ansi.GRAY.apply("sin notas")));
        }
    }

    /**
     * Promedio de todo un grado en cada materia.
     */
    private void courseAverage() {
        GradeLevel grade = chooseGrade();
        // Promedio de todos los estudiantes del grado, materia por materia
        List<SubjectAverage> averages = require(app.academic().courseAverageBySubject(grade.getId(), currentYear()));
        ConsoleUI.section("Promedio del curso " + grade.getName() + " · " + currentYear());
        BaseView.<SubjectAverage>table()
                .column("Materia", SubjectAverage::subjectName)
                .column("Rendimiento", a -> ConsoleUI.bar(a.average().doubleValue() / 5.0, 20, barColor(a.average())))
                .numberColumn("Promedio", a -> ConsoleUI.grade(a.average()))
                .numberColumn("Notas", a -> String.valueOf(a.gradesCount()))
                .print(averages);
    }

    /**
     * Historial completo de notas agrupado por año y período.
     */
    private void history() {
        Student student = selectStudent();
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        // Mapa de dos niveles: año -> período -> lista de notas
        Map<Integer, Map<Term, List<AcademicRecord>>> history = require(app.academic().academicHistory(student.getId()));
        if (history.isEmpty()) {
            ConsoleUI.info("El estudiante no tiene calificaciones registradas.");
            return;
        }
        // forEach recorre el mapa: primero cada año y dentro de él cada período
        history.forEach((year, terms) -> {
            ConsoleUI.section("Año " + year);
            terms.forEach((term, records) -> {
                System.out.println("  " + Ansi.BOLD + Ansi.BRIGHT_YELLOW.apply("Período " + term.getDbValue()));
                records.forEach(r -> System.out.printf("    • %s %s%n",
                        ConsoleUI.pad(subjects.get(r.getSubjectId()).getName(), 40), ConsoleUI.grade(r.getGrade())));
            });
        });
    }

    /**
     * Registra la asistencia de un estudiante en una materia y fecha.
     */
    private void registerAttendance() {
        Student student = selectStudent();
        Subject subject = selectSubjectOfGrade(gradeLevelOf(student));
        LocalDate date = input.readDateOrToday("Fecha");
        AttendanceStatus status = chooseEnum("Estado", AttendanceStatus.class);
        // El profesor que registra es opcional: si dice que no, queda null
        Teacher teacher = input.readYesNo("¿Desea indicar el profesor que registra?") ? selectTeacher() : null;
        String notes = input.readOptionalText("Observaciones");
        ConsoleUI.show(app.academic().registerAttendance(student.getId(), subject.getId(), date, status,
                teacher == null ? null : teacher.getId(), notes));
    }

    /**
     * Resumen y detalle de la asistencia de un estudiante.
     */
    private void studentAttendance() {
        Student student = selectStudent();
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        // Resumen: cuántos registros hay de cada estado de asistencia
        Map<AttendanceStatus, Long> summary = require(app.academic().attendanceSummary(student.getId()));
        ConsoleUI.section("Resumen");
        // values() devuelve todas las constantes del enum; getOrDefault pone 0 si no hay registros de ese estado
        for (AttendanceStatus status : AttendanceStatus.values()) {
            System.out.printf("  %s %s%n", ConsoleUI.pad(ConsoleUI.status(status.getDbValue()), 16),
                    Ansi.BOLD + summary.getOrDefault(status, 0L).toString() + Ansi.RESET);
        }
        ConsoleUI.section("Detalle");
        BaseView.<Attendance>table()
                .column("Fecha", a -> Formats.date(a.getDate()))
                .column("Materia", a -> subjects.get(a.getSubjectId()).getName())
                .column("Estado", a -> ConsoleUI.status(a.getStatus().getDbValue()))
                .column("Observaciones", Attendance::getNotes)
                .print(require(app.academic().attendanceOf(student.getId())));
    }

    /**
     * Reporte de estudiantes con más de 3 ausencias en una misma semana.
     */
    private void absencePatterns() {
        LocalDate from = input.readDate("Desde");
        // Si no se escribe fecha final, se usa hoy
        LocalDate to = input.readDateOrToday("Hasta");
        // El servicio agrupa las ausencias por estudiante y semana y se queda con las de más de 3
        List<AbsencePattern> patterns = require(app.academic().absencePatterns(from, to));
        ConsoleUI.section("Estudiantes con más de " + AttendanceService.WEEKLY_ABSENCE_LIMIT + " ausencias en una semana");
        BaseView.<AbsencePattern>table()
                .column("Estudiante", AbsencePattern::studentName)
                .column("Semana del", p -> Formats.date(p.weekStart()))
                .numberColumn("Ausencias", p -> Ansi.RED.apply(String.valueOf(p.absences())))
                .print(patterns);
        if (!patterns.isEmpty()) {
            ConsoleUI.warning("Se recomienda contactar a los acudientes de estos estudiantes.");
        }
    }

    // ====================== AYUDAS ======================

    /**
     * Deja escoger un grado de la lista.
     */
    private GradeLevel chooseGrade() {
        return input.readChoice("Grado", require(app.students().getGradeLevels()), GradeLevel::getName);
    }

    /**
     * Color de la barra según la nota: rojo bajo 3.0, amarillo bajo 4.0 y verde desde 4.0.
     */
    private static Ansi barColor(BigDecimal grade) {
        double value = grade.doubleValue();
        return value < 3.0 ? Ansi.RED : value < 4.0 ? Ansi.YELLOW : Ansi.GREEN;
    }
}
