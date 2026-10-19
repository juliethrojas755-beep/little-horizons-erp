package com.littlehorizons.erp.view;

import com.littlehorizons.erp.AppContext;
import com.littlehorizons.erp.controller.OperationResult;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.enums.DbEnum;
import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.OperationCancelledException;
import com.littlehorizons.erp.view.components.ConsoleUI;
import com.littlehorizons.erp.view.components.TablePrinter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Shared behavior of every screen: access to controllers, input reading and the
 * "pick a student / teacher / guardian" dialogs used by several modules.
 * <p>Views only read input, call a controller and print. They never contain business rules.</p>
 */
public abstract class BaseView {

    protected final AppContext app;
    protected final InputReader input;

    protected BaseView(AppContext app, InputReader input) {
        this.app = app;
        this.input = input;
    }

    /** Opens the module menu. */
    public abstract void show();

    protected int currentYear() {
        return LocalDate.now().getYear();
    }

    /** Returns the data when the operation worked; otherwise prints the error and cancels the screen. */
    protected <T> T require(OperationResult<T> result) {
        if (!result.success()) {
            ConsoleUI.error(result.message());
            throw new OperationCancelledException();
        }
        return result.data();
    }

    /** Lets the user pick an enum constant by number, showing its Spanish database value. */
    protected <E extends Enum<E> & DbEnum> E chooseEnum(String prompt, Class<E> type) {
        return input.readChoice(prompt, Arrays.asList(type.getEnumConstants()), DbEnum::getDbValue);
    }

    // ---------- selectors ----------

    /** Finds a student by document number or by name. */
    protected Student selectStudent() {
        String query = input.readText("Documento o nombre del estudiante");
        Optional<Student> byDocument = require(app.students().findByDocument(query));
        if (byDocument.isPresent()) {
            printSelected("Estudiante", byDocument.get().toString());
            return byDocument.get();
        }
        List<Student> matches = require(app.students().search(query));
        return pickFromList(matches, "estudiante", Student::toString);
    }

    protected Guardian selectGuardian() {
        String query = input.readText("Documento o nombre del acudiente");
        Optional<Guardian> byDocument = require(app.students().findGuardianByDocument(query));
        if (byDocument.isPresent()) {
            printSelected("Acudiente", byDocument.get().toString());
            return byDocument.get();
        }
        String needle = query.toLowerCase();
        List<Guardian> matches = require(app.students().findAllGuardians()).stream()
                .filter(g -> g.getFullName().toLowerCase().contains(needle))
                .toList();
        return pickFromList(matches, "acudiente", Guardian::toString);
    }

    protected Teacher selectTeacher() {
        String query = input.readText("Documento o nombre del profesor");
        Optional<Teacher> byDocument = require(app.teachers().findByDocument(query));
        if (byDocument.isPresent()) {
            printSelected("Profesor", byDocument.get().toString());
            return byDocument.get();
        }
        String needle = query.toLowerCase();
        List<Teacher> matches = require(app.teachers().findAll()).stream()
                .filter(t -> t.getFullName().toLowerCase().contains(needle))
                .toList();
        return pickFromList(matches, "profesor", t -> t.getFullName() + " · " + t.getSpecialization());
    }

    /** Shows the subjects of a grade and lets the user pick one. */
    protected Subject selectSubjectOfGrade(int gradeLevelId) {
        List<Subject> subjects = require(app.academic().findSubjectsByGrade(gradeLevelId));
        if (subjects.isEmpty()) {
            ConsoleUI.warning("El grado no tiene materias registradas.");
            throw new OperationCancelledException();
        }
        return input.readChoice("Materia", subjects, Subject::toString);
    }

    /** Grade level of the student in the current year, needed to list his subjects. */
    protected int gradeLevelOf(Student student) {
        return require(app.students().getEnrollments(student.getId())).stream()
                .filter(e -> e.getAcademicYear() == currentYear())
                .findFirst()
                .map(e -> e.getGradeLevelId())
                .orElseThrow(() -> {
                    ConsoleUI.error(student.getFullName() + " no tiene matrícula en " + currentYear() + ".");
                    return new OperationCancelledException();
                });
    }

    private <T> T pickFromList(List<T> matches, String noun, Function<T, String> label) {
        if (matches.isEmpty()) {
            ConsoleUI.warning("No se encontró ningún " + noun + " con ese dato.");
            throw new OperationCancelledException();
        }
        if (matches.size() == 1) {
            printSelected(capitalize(noun), label.apply(matches.get(0)));
            return matches.get(0);
        }
        ConsoleUI.info("Se encontraron " + matches.size() + " coincidencias:");
        return input.readChoice("Elija el " + noun, matches.subList(0, Math.min(matches.size(), 20)), label);
    }

    private void printSelected(String noun, String value) {
        System.out.println("  " + Ansi.GREEN.apply("➜ ") + Ansi.GRAY.apply(noun + ": ") + Ansi.BOLD + value + Ansi.RESET);
    }

    private static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /** Small helper so subclasses can build tables in one line. */
    protected static <T> TablePrinter<T> table() {
        return new TablePrinter<>();
    }
}
