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
 * Módulo 3: directorio de profesores, asignación de materias y horarios semanales.
 * <p>Solo pide datos, llama al controlador y dibuja el resultado; la validación de conflictos de horario está en el servicio.</p>
 */
public class TeacherView extends BaseView {

    // Título que aparece en el encabezado del menú
    private static final String MODULE = "Gestión de Profesores";
    // Ancho en caracteres de cada columna de día en la cuadrícula del horario
    private static final int DAY_COLUMN = 15;

    public TeacherView(AppContext app, InputReader input) {
        super(app, input);
    }

    /**
     * Arma el menú del módulo: cada opción es un texto y un método (referencia con ::).
     */
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

    /**
     * Muestra el directorio con todos los profesores.
     */
    private void directory() {
        printTeachers(require(app.teachers().findAll()));
    }

    /**
     * Busca profesores por especialización (por ejemplo, Matemáticas).
     */
    private void searchBySpecialization() {
        printTeachers(require(app.teachers().findBySpecialization(input.readText("Especialización (ej: Matemáticas)"))));
    }

    /**
     * Pide los datos de un profesor nuevo y lo registra.
     */
    private void register() {
        Teacher teacher = new Teacher();
        // Un profesor es adulto: no se ofrece TI (tarjeta de identidad)
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
        // Si no escribe fecha, se usa hoy como fecha de contratación
        teacher.setHireDate(input.readDateOrToday("Fecha de contratación"));
        ConsoleUI.show(app.teachers().registerTeacher(teacher));
    }

    /**
     * Asigna una materia de un grado a un profesor para el año actual.
     */
    private void assignSubject() {
        Teacher teacher = selectTeacher();
        // Primero se escoge el grado y luego una de sus materias
        GradeLevel grade = input.readChoice("Grado de la materia", require(app.students().getGradeLevels()), GradeLevel::getName);
        Subject subject = selectSubjectOfGrade(grade.getId());
        // La sección es una sola letra (A, B, C)
        String section = input.readValidated("Sección (A, B, C)", s -> s.matches("[A-Za-z]"), "Una sola letra.");
        ConsoleUI.show(app.teachers().assignSubject(teacher.getId(), subject.getId(), currentYear(), section));
    }

    /**
     * Muestra las materias asignadas a un profesor.
     */
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

    /**
     * Agrega un bloque de horario a una materia del profesor; el servicio valida que no haya conflictos.
     */
    private void addSchedule() {
        Teacher teacher = selectTeacher();
        // El bloque de horario siempre pertenece a una materia ya asignada al profesor
        TeacherAssignment assignment = chooseAssignment(teacher);
        Schedule schedule = new Schedule();
        schedule.setAssignmentId(assignment.getId());
        schedule.setWeekDay(chooseEnum("Día", WeekDay.class));
        schedule.setStartTime(input.readTime("Hora de inicio"));
        schedule.setEndTime(input.readTime("Hora de fin"));
        // El salón es opcional; si se escribe, el servicio también verifica que no esté ocupado
        schedule.setClassroom(input.readOptionalText("Salón"));
        ConsoleUI.show(app.teachers().addSchedule(schedule));
    }

    /**
     * Horario semanal en forma de cuadrícula: una columna por día y una fila por hora de inicio.
     */
    private void weeklySchedule() {
        Teacher teacher = selectTeacher();
        // Todos los bloques del profesor
        List<Schedule> blocks = require(app.teachers().getTeacherSchedule(teacher.getId()));
        if (blocks.isEmpty()) {
            ConsoleUI.info(teacher.getFullName() + " no tiene horario registrado.");
            return;
        }
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        // Tabla asignación -> materia, para poner el nombre de la materia en cada celda
        Map<Integer, Integer> subjectOfAssignment = require(app.teachers().getAssignments(teacher.getId())).stream()
                .collect(Collectors.toMap(TeacherAssignment::getId, TeacherAssignment::getSubjectId));
        // Horas de inicio distintas y ordenadas (TreeSet ordena y no repite): cada una es una fila de la cuadrícula
        TreeSet<LocalTime> starts = blocks.stream().map(Schedule::getStartTime).collect(Collectors.toCollection(TreeSet::new));

        ConsoleUI.section("Horario semanal · " + teacher.getFullName());
        // Encabezado de la cuadrícula: un título por cada día de la semana
        StringBuilder head = new StringBuilder("  " + ConsoleUI.pad("", 13));
        for (WeekDay day : WeekDay.values()) {
            head.append(Ansi.BOLD).append(Ansi.BRIGHT_CYAN.apply(ConsoleUI.pad(day.getDbValue(), DAY_COLUMN)));
        }
        System.out.println(head);
        // Una fila por hora de inicio
        for (LocalTime start : starts) {
            String end = blocks.stream().filter(b -> b.getStartTime().equals(start)).findFirst()
                    .map(b -> b.getEndTime().toString()).orElse("");
            StringBuilder row = new StringBuilder("  " + Ansi.GRAY.apply(ConsoleUI.pad(start + "-" + end, 13)));
            for (WeekDay day : WeekDay.values()) {
                // Si hay un bloque ese día y a esa hora se escribe la materia; si no, un punto
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

    /**
     * Elimina un bloque de horario pidiendo confirmación.
     */
    private void removeSchedule() {
        weeklySchedule();
        // Se muestra el horario para que el usuario vea los ids y escoja cuál borrar
        int id = input.readInt("Id del bloque a eliminar", 1, Integer.MAX_VALUE);
        // Confirmación antes de borrar
        if (input.readYesNo("¿Confirma la eliminación del bloque " + id + "?")) {
            ConsoleUI.show(app.teachers().removeSchedule(id));
        }
    }

    // ====================== AYUDAS ======================

    /**
     * Deja escoger una de las materias que el profesor tiene asignadas este año.
     */
    private TeacherAssignment chooseAssignment(Teacher teacher) {
        Map<Integer, Subject> subjects = require(app.academic().subjectsById());
        // Solo las asignaciones del año actual
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

    /**
     * Imprime una lista de profesores en una tabla y la antigüedad promedio.
     */
    private void printTeachers(List<Teacher> teachers) {
        BaseView.<Teacher>table()
                .numberColumn("Id", t -> String.valueOf(t.getId()))
                .column("Nombre", Teacher::getFullName)
                .column("Especialización", Teacher::getSpecialization)
                .column("Correo", Teacher::getEmail)
                .column("Contratación", t -> Formats.date(t.getHireDate()))
                .column("Estado", t -> ConsoleUI.status(t.getStatus().getDbValue()))
                .print(teachers);
        // Antigüedad promedio: años entre la contratación y hoy, promediados con average()
        teachers.stream()
                .mapToInt(t -> t.getHireDate().until(LocalDate.now()).getYears())
                .average()
                .ifPresent(average -> ConsoleUI.info(String.format("Antigüedad promedio: %.1f años.", average)));
    }
}
