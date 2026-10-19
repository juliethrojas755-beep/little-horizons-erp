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
 * Module 2: subjects, subject enrollment, grades, averages and attendance.
 */
public class AcademicView extends BaseView {

    private static final String MODULE = "Gestión Académica";
    private static final List<Term> TERMS = List.of(Term.I, Term.II, Term.III, Term.IV);

    public AcademicView(AppContext app, InputReader input) {
        super(app, input);
    }

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

    private void subjectsByGrade() {
        GradeLevel grade = chooseGrade();
        BaseView.<Subject>table()
                .column("Código", Subject::getCode)
                .column("Materia", Subject::getName)
                .numberColumn("Horas/sem", s -> String.valueOf(s.getWeeklyHours()))
                .column("Estado", s -> ConsoleUI.status(s.isActive() ? "Activa" : "Retirada"))
                .print(require(app.academic().findSubjectsByGrade(grade.getId())));
    }

    private void createSubject() {
        Subject subject = new Subject();
        subject.setGradeLevelId(chooseGrade().getId());
        subject.setCode(input.readText("Código (ej: CIE-5)").toUpperCase());
        subject.setName(input.readText("Nombre"));
        subject.setDescription(input.readOptionalText("Descripción"));
        subject.setWeeklyHours(input.readInt("Horas semanales", 1, 10));
        ConsoleUI.show(app.academic().createSubject(subject));
    }

    private void enrollSubjects() {
        Student student = selectStudent();
        Term term = input.readChoice("Período", TERMS, Term::getDbValue);
        if (input.readYesNo("¿Inscribir en TODAS las materias de su grado?")) {
            Long created = require(app.academic().enrollInAllGradeSubjects(student.getId(), currentYear(), term));
            ConsoleUI.success("Se crearon " + created + " inscripciones para el período " + term + ".");
        } else {
            Subject subject = selectSubjectOfGrade(gradeLevelOf(student));
            ConsoleUI.show(app.academic().enrollInSubject(student.getId(), subject.getId(), currentYear(), term));
        }
        ConsoleUI.section("Inscripciones del estudiante en " + currentYear());
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        BaseView.<SubjectEnrollment>table()
                .column("Período", e -> e.getTerm().getDbValue())
                .column("Materia", e -> subjects.get(e.getSubjectId()).getName())
                .column("Estado", e -> ConsoleUI.status(e.getStatus().getDbValue()))
                .print(require(app.academic().getSubjectEnrollments(student.getId())).stream()
                        .filter(e -> e.getAcademicYear() == currentYear()).toList());
    }

    private void recordGrade() {
        Student student = selectStudent();
        Subject subject = selectSubjectOfGrade(gradeLevelOf(student));
        Term term = input.readChoice("Período", TERMS, Term::getDbValue);
        BigDecimal grade = input.readGrade("Nota (0.0 a 5.0)");
        String notes = input.readOptionalText("Observaciones");
        if (ConsoleUI.show(app.academic().recordGrade(student.getId(), subject.getId(), currentYear(), term, grade, notes))) {
            ConsoleUI.info(student.getFullName() + " · " + subject.getName() + " · período " + term + " → "
                    + ConsoleUI.grade(grade));
        }
    }

    private void closeFinal() {
        Student student = selectStudent();
        Subject subject = selectSubjectOfGrade(gradeLevelOf(student));
        BigDecimal finalGrade = require(app.academic().closeFinalGrade(student.getId(), subject.getId(), currentYear()));
        ConsoleUI.success("Nota final de " + subject.getName() + ": " + ConsoleUI.grade(finalGrade));
    }

    /** Report card: average per subject with bars and the general average per term. */
    private void reportCard() {
        Student student = selectStudent();
        int year = currentYear();
        ConsoleUI.section("Boletín " + year + " · " + student.getFullName());
        List<SubjectAverage> averages = require(app.academic().averageBySubject(student.getId(), year));
        if (averages.isEmpty()) {
            ConsoleUI.info("El estudiante no tiene notas en " + year + ".");
            return;
        }
        for (SubjectAverage average : averages) {
            System.out.printf("  %s %s  %s %s%n", ConsoleUI.pad(ConsoleUI.truncate(average.subjectName(), 34), 34),
                    ConsoleUI.bar(average.average().doubleValue() / 5.0, 30, barColor(average.average())),
                    ConsoleUI.grade(average.average()), Ansi.GRAY.apply("(" + average.gradesCount() + " notas)"));
        }
        ConsoleUI.section("Promedio general por período");
        for (Term term : TERMS) {
            Optional<BigDecimal> average = require(app.academic().termAverage(student.getId(), year, term));
            System.out.printf("  Período %-4s %s%n", term.getDbValue(),
                    average.map(value -> ConsoleUI.bar(value.doubleValue() / 5.0, 30, barColor(value)) + "  "
                            + ConsoleUI.grade(value)).orElse(Ansi.GRAY.apply("sin notas")));
        }
    }

    private void courseAverage() {
        GradeLevel grade = chooseGrade();
        List<SubjectAverage> averages = require(app.academic().courseAverageBySubject(grade.getId(), currentYear()));
        ConsoleUI.section("Promedio del curso " + grade.getName() + " · " + currentYear());
        BaseView.<SubjectAverage>table()
                .column("Materia", SubjectAverage::subjectName)
                .column("Rendimiento", a -> ConsoleUI.bar(a.average().doubleValue() / 5.0, 20, barColor(a.average())))
                .numberColumn("Promedio", a -> ConsoleUI.grade(a.average()))
                .numberColumn("Notas", a -> String.valueOf(a.gradesCount()))
                .print(averages);
    }

    /** Complete history grouped by year and term. */
    private void history() {
        Student student = selectStudent();
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        Map<Integer, Map<Term, List<AcademicRecord>>> history = require(app.academic().academicHistory(student.getId()));
        if (history.isEmpty()) {
            ConsoleUI.info("El estudiante no tiene calificaciones registradas.");
            return;
        }
        history.forEach((year, terms) -> {
            ConsoleUI.section("Año " + year);
            terms.forEach((term, records) -> {
                System.out.println("  " + Ansi.BOLD + Ansi.BRIGHT_YELLOW.apply("Período " + term.getDbValue()));
                records.forEach(r -> System.out.printf("    • %s %s%n",
                        ConsoleUI.pad(subjects.get(r.getSubjectId()).getName(), 40), ConsoleUI.grade(r.getGrade())));
            });
        });
    }

    private void registerAttendance() {
        Student student = selectStudent();
        Subject subject = selectSubjectOfGrade(gradeLevelOf(student));
        LocalDate date = input.readDateOrToday("Fecha");
        AttendanceStatus status = chooseEnum("Estado", AttendanceStatus.class);
        Teacher teacher = input.readYesNo("¿Desea indicar el profesor que registra?") ? selectTeacher() : null;
        String notes = input.readOptionalText("Observaciones");
        ConsoleUI.show(app.academic().registerAttendance(student.getId(), subject.getId(), date, status,
                teacher == null ? null : teacher.getId(), notes));
    }

    private void studentAttendance() {
        Student student = selectStudent();
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        Map<AttendanceStatus, Long> summary = require(app.academic().attendanceSummary(student.getId()));
        ConsoleUI.section("Resumen");
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

    private void absencePatterns() {
        LocalDate from = input.readDate("Desde");
        LocalDate to = input.readDateOrToday("Hasta");
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

    // ---------- helpers ----------

    private GradeLevel chooseGrade() {
        return input.readChoice("Grado", require(app.students().getGradeLevels()), GradeLevel::getName);
    }

    private static Ansi barColor(BigDecimal grade) {
        double value = grade.doubleValue();
        return value < 3.0 ? Ansi.RED : value < 4.0 ? Ansi.YELLOW : Ansi.GREEN;
    }
}
