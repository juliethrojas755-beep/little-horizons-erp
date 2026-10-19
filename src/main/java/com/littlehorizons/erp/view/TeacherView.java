package com.littlehorizons.erp.view;

import com.littlehorizons.erp.AppContext;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.model.enums.DocumentType;
import com.littlehorizons.erp.model.enums.WeekDay;
import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.Formats;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.OperationCancelledException;
import com.littlehorizons.erp.util.Validator;
import com.littlehorizons.erp.view.components.ConsoleUI;
import com.littlehorizons.erp.view.components.Menu;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Module 3: teacher directory, subject assignments and weekly schedules.
 */
public class TeacherView extends BaseView {

    private static final String MODULE = "Gestión de Profesores";
    private static final int DAY_COLUMN = 15;

    public TeacherView(AppContext app, InputReader input) {
        super(app, input);
    }

    @Override
    public void show() {
        new Menu(MODULE, input)
                .option("☰", "Directorio de profesores", this::directory)
                .option("⌕", "Buscar por especialización", this::searchBySpecialization)
                .option("✚", "Registrar profesor", this::register)
                .separator()
                .option("⇄", "Asignar materia a un profesor", this::assignSubject)
                .option("☰", "Materias asignadas a un profesor", this::assignments)
                .separator()
                .option("✚", "Agregar bloque de horario (valida conflictos)", this::addSchedule)
                .option("◷", "Ver horario semanal", this::weeklySchedule)
                .option("✖", "Eliminar bloque de horario", this::removeSchedule)
                .show();
    }

    private void directory() {
        printTeachers(require(app.teachers().findAll()));
    }

    private void searchBySpecialization() {
        printTeachers(require(app.teachers().findBySpecialization(input.readText("Especialización (ej: Matemáticas)"))));
    }

    private void register() {
        Teacher teacher = new Teacher();
        teacher.setDocumentType(input.readChoice("Tipo de documento",
                List.of(DocumentType.CC, DocumentType.CE, DocumentType.PASSPORT), DocumentType::getDbValue));
        teacher.setDocumentNumber(input.readValidated("Número de documento", Validator::isValidDocument,
                "Use de 5 a 20 letras o números."));
        teacher.setFirstName(input.readValidated("Primer nombre", Validator::isValidName, "Solo letras."));
        teacher.setMiddleName(input.readOptionalText("Segundo nombre"));
        teacher.setLastName(input.readValidated("Primer apellido", Validator::isValidName, "Solo letras."));
        teacher.setSecondLastName(input.readOptionalText("Segundo apellido"));
        teacher.setEmail(input.readValidated("Correo institucional", Validator::isValidEmail, "Correo inválido."));
        teacher.setPhone(input.readOptionalText("Teléfono"));
        teacher.setSpecialization(input.readText("Especialización"));
        teacher.setHireDate(input.readDateOrToday("Fecha de contratación"));
        ConsoleUI.show(app.teachers().registerTeacher(teacher));
    }

    private void assignSubject() {
        Teacher teacher = selectTeacher();
        GradeLevel grade = input.readChoice("Grado de la materia", require(app.students().getGradeLevels()), GradeLevel::getName);
        Subject subject = selectSubjectOfGrade(grade.getId());
        String section = input.readValidated("Sección (A, B, C)", s -> s.matches("[A-Za-z]"), "Una sola letra.");
        ConsoleUI.show(app.teachers().assignSubject(teacher.getId(), subject.getId(), currentYear(), section));
    }

    private void assignments() {
        Teacher teacher = selectTeacher();
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        BaseView.<TeacherAssignment>table()
                .numberColumn("Id", a -> String.valueOf(a.getId()))
                .column("Año", a -> String.valueOf(a.getAcademicYear()))
                .column("Materia", a -> subjects.get(a.getSubjectId()).toString())
                .column("Sección", TeacherAssignment::getSection)
                .print(require(app.teachers().getAssignments(teacher.getId())));
    }

    private void addSchedule() {
        Teacher teacher = selectTeacher();
        TeacherAssignment assignment = chooseAssignment(teacher);
        Schedule schedule = new Schedule();
        schedule.setAssignmentId(assignment.getId());
        schedule.setWeekDay(chooseEnum("Día", WeekDay.class));
        schedule.setStartTime(input.readTime("Hora de inicio"));
        schedule.setEndTime(input.readTime("Hora de fin"));
        schedule.setClassroom(input.readOptionalText("Salón"));
        ConsoleUI.show(app.teachers().addSchedule(schedule));
    }

    /** Weekly grid: one column per day, one row per start time. */
    private void weeklySchedule() {
        Teacher teacher = selectTeacher();
        List<Schedule> blocks = require(app.teachers().getTeacherSchedule(teacher.getId()));
        if (blocks.isEmpty()) {
            ConsoleUI.info(teacher.getFullName() + " no tiene horario registrado.");
            return;
        }
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        Map<Integer, Integer> subjectOfAssignment = require(app.teachers().getAssignments(teacher.getId())).stream()
                .collect(Collectors.toMap(TeacherAssignment::getId, TeacherAssignment::getSubjectId));
        TreeSet<LocalTime> starts = blocks.stream().map(Schedule::getStartTime).collect(Collectors.toCollection(TreeSet::new));

        ConsoleUI.section("Horario semanal · " + teacher.getFullName());
        StringBuilder head = new StringBuilder("  " + ConsoleUI.pad("", 13));
        for (WeekDay day : WeekDay.values()) {
            head.append(Ansi.BOLD).append(Ansi.BRIGHT_CYAN.apply(ConsoleUI.pad(day.getDbValue(), DAY_COLUMN)));
        }
        System.out.println(head);
        for (LocalTime start : starts) {
            String end = blocks.stream().filter(b -> b.getStartTime().equals(start)).findFirst()
                    .map(b -> b.getEndTime().toString()).orElse("");
            StringBuilder row = new StringBuilder("  " + Ansi.GRAY.apply(ConsoleUI.pad(start + "-" + end, 13)));
            for (WeekDay day : WeekDay.values()) {
                String cell = blocks.stream()
                        .filter(b -> b.getWeekDay() == day && b.getStartTime().equals(start))
                        .findFirst()
                        .map(b -> ConsoleUI.truncate(subjects.get(subjectOfAssignment.get(b.getAssignmentId())).getName(),
                                DAY_COLUMN - 2))
                        .orElse("·");
                row.append(ConsoleUI.pad(cell.equals("·") ? Ansi.GRAY.apply(cell) : Ansi.YELLOW.apply(cell), DAY_COLUMN));
            }
            System.out.println(row);
        }
        ConsoleUI.section("Bloques registrados");
        BaseView.<Schedule>table()
                .numberColumn("Id", s -> String.valueOf(s.getId()))
                .column("Día", s -> s.getWeekDay().getDbValue())
                .column("Inicio", s -> s.getStartTime().toString())
                .column("Fin", s -> s.getEndTime().toString())
                .column("Materia", s -> subjects.get(subjectOfAssignment.get(s.getAssignmentId())).getName())
                .column("Salón", Schedule::getClassroom)
                .print(blocks);
    }

    private void removeSchedule() {
        weeklySchedule();
        int id = input.readInt("Id del bloque a eliminar", 1, Integer.MAX_VALUE);
        if (input.readYesNo("¿Confirma la eliminación del bloque " + id + "?")) {
            ConsoleUI.show(app.teachers().removeSchedule(id));
        }
    }

    // ---------- helpers ----------

    private TeacherAssignment chooseAssignment(Teacher teacher) {
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        List<TeacherAssignment> list = require(app.teachers().getAssignments(teacher.getId())).stream()
                .filter(a -> a.getAcademicYear() == currentYear())
                .toList();
        if (list.isEmpty()) {
            ConsoleUI.warning("El profesor no tiene materias asignadas en " + currentYear() + ". Asígnele una primero.");
            throw new OperationCancelledException();
        }
        return input.readChoice("Asignación", list,
                a -> subjects.get(a.getSubjectId()).getName() + " · sección " + a.getSection());
    }

    private void printTeachers(List<Teacher> teachers) {
        BaseView.<Teacher>table()
                .numberColumn("Id", t -> String.valueOf(t.getId()))
                .column("Nombre", Teacher::getFullName)
                .column("Especialización", Teacher::getSpecialization)
                .column("Correo", Teacher::getEmail)
                .column("Contratación", t -> Formats.date(t.getHireDate()))
                .column("Estado", t -> ConsoleUI.status(t.getStatus().getDbValue()))
                .print(teachers);
        teachers.stream()
                .mapToInt(t -> t.getHireDate().until(LocalDate.now()).getYears())
                .average()
                .ifPresent(average -> ConsoleUI.info(String.format("Antigüedad promedio: %.1f años.", average)));
    }
}
