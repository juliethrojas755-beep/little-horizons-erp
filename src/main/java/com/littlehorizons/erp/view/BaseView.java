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
 * Comportamiento común de todas las pantallas: acceso a los controladores, lectura del teclado y los
 * diálogos de "elegir un estudiante / profesor / acudiente" que usan varios módulos.
 * <p>Las vistas solo leen datos, llaman a un controlador e imprimen. Nunca tienen reglas de negocio.</p>
 */
public abstract class BaseView {

    // Acceso a todos los controladores (estudiantes, finanzas, etc.)
    protected final AppContext app;
    // Lector de teclado con validación
    protected final InputReader input;

    // Constructor protegido: solo lo usan las vistas hijas
    protected BaseView(AppContext app, InputReader input) {
        this.app = app;
        this.input = input;
    }

    /** Abre el menú del módulo (cada vista lo implementa a su manera). */
    public abstract void show();

    // Año actual: se usa como año académico por defecto
    protected int currentYear() {
        return LocalDate.now().getYear();
    }

    /**
     * Devuelve los datos cuando la operación salió bien; si no, imprime el error y cancela la pantalla
     * (lanza OperationCancelledException, que el menú captura para volver).
     */
    protected <T> T require(OperationResult<T> result) {
        if (!result.success()) {
            ConsoleUI.error(result.message());
            throw new OperationCancelledException();
        }
        return result.data();
    }

    /** Deja elegir una constante de un enum por su número, mostrando su texto en español. */
    protected <E extends Enum<E> & DbEnum> E chooseEnum(String prompt, Class<E> type) {
        return input.readChoice(prompt, Arrays.asList(type.getEnumConstants()), DbEnum::getDbValue);
    }

    // ====================== SELECTORES (se reutilizan en varios módulos) ======================

    /** Busca un estudiante por número de documento o por nombre. */
    protected Student selectStudent() {
        // Primero se intenta por documento (único); si no coincide, se busca por nombre
        String query = input.readText("Documento o nombre del estudiante");
        Optional<Student> byDocument = require(app.students().findByDocument(query));
        if (byDocument.isPresent()) {
            printSelected("Estudiante", byDocument.get().toString());
            return byDocument.get();
        }
        // Búsqueda por nombre: puede haber varias coincidencias
        List<Student> matches = require(app.students().search(query));
        // Si hay una sola coincidencia se usa; si hay varias, el usuario escoge de una lista
        return pickFromList(matches, "estudiante", Student::toString);
    }

    protected Guardian selectGuardian() {
        String query = input.readText("Documento o nombre del acudiente");
        Optional<Guardian> byDocument = require(app.students().findGuardianByDocument(query));
        if (byDocument.isPresent()) {
            printSelected("Acudiente", byDocument.get().toString());
            return byDocument.get();
        }
        // Se compara en minúsculas para no distinguir mayúsculas de minúsculas
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

    /** Muestra las materias de un grado y deja elegir una. */
    protected Subject selectSubjectOfGrade(int gradeLevelId) {
        List<Subject> subjects = require(app.academic().findSubjectsByGrade(gradeLevelId));
        if (subjects.isEmpty()) {
            ConsoleUI.warning("El grado no tiene materias registradas.");
            throw new OperationCancelledException();
        }
        return input.readChoice("Materia", subjects, Subject::toString);
    }

    /** Grado del estudiante en el año actual; se necesita para listar sus materias. */
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

    // Decide qué hacer con las coincidencias: ninguna (cancela), una (la usa) o varias (deja escoger, máximo 20)
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

    // Imprime en verde la persona elegida para que el usuario confirme que es la correcta
    private void printSelected(String noun, String value) {
        System.out.println("  " + Ansi.GREEN.apply("➜ ") + Ansi.GRAY.apply(noun + ": ") + Ansi.BOLD + value + Ansi.RESET);
    }

    // Convierte la primera letra en mayúscula (estudiante -> Estudiante)
    private static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /** Ayuda para que las vistas hijas construyan tablas en una sola línea. */
    protected static <T> TablePrinter<T> table() {
        return new TablePrinter<>();
    }
}
