package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.enums.StudentStatus;
import com.littlehorizons.erp.service.StudentService;
import java.util.List;
import java.util.Optional;

/**
 * Recibe las acciones de las pantallas de estudiantes y las delega en {@link StudentService}.
 * <p>Un controlador NO tiene reglas de negocio ni SQL: llama al servicio y envuelve el resultado en un
 * {@link OperationResult} para que la vista nunca reciba excepciones.</p>
 */
public class StudentController extends BaseController {

    // Servicio al que se delegan las reglas de negocio
    private final StudentService service;

    // El servicio llega por el constructor (inyección de dependencias): lo entrega AppContext
    public StudentController(StudentService service) {
        this.service = service;
    }

    /** Registra un estudiante nuevo vinculado a un acudiente. */
    public OperationResult<Integer> registerStudent(Student student, int guardianId) {
        // execute(mensaje, acción): ejecuta la acción y convierte errores en un resultado fallido; null = sin mensaje de éxito
        return execute("Estudiante registrado correctamente.", () -> service.registerStudent(student, guardianId));
    }

    /** Registra un acudiente nuevo. */
    public OperationResult<Integer> registerGuardian(Guardian guardian) {
        return execute("Acudiente registrado correctamente.", () -> service.registerGuardian(guardian));
    }

    /** Vincula otro acudiente a un estudiante. */
    public OperationResult<Void> linkGuardian(int studentId, int guardianId, boolean primary) {
        return run("Acudiente vinculado al estudiante.", () -> service.linkGuardian(studentId, guardianId, primary));
    }

    /** Actualiza los datos de un estudiante. */
    public OperationResult<Boolean> updateStudent(Student student) {
        return execute("Datos del estudiante actualizados.", () -> service.updateStudent(student));
    }

    /** Cambia el estado. */
    public OperationResult<Void> changeStatus(int studentId, StudentStatus status, int year) {
        return run("Estado actualizado a " + status + ".", () -> service.changeStatus(studentId, status, year));
    }

    /** Matricula a un estudiante en un grado y año. */
    public OperationResult<Integer> enroll(int studentId, int gradeLevelId, int year) {
        return execute("Matrícula creada para el año " + year + ".", () -> service.enroll(studentId, gradeLevelId, year));
    }

    /** Busca un estudiante por id. */
    public OperationResult<Student> getStudent(int studentId) {
        return execute(null, () -> service.getStudent(studentId));
    }

    /** Busca por número de documento. */
    public OperationResult<Optional<Student>> findByDocument(String document) {
        return execute(null, () -> service.findByDocument(document));
    }

    /** Lista todos los registros. */
    public OperationResult<List<Student>> findAll() {
        return execute(null, service::findAll);
    }

    /** Busca estudiantes por nombre o apellido. */
    public OperationResult<List<Student>> search(String text) {
        return execute(null, () -> service.search(text));
    }

    /** Estudiantes de un grado y año. */
    public OperationResult<List<Student>> findByGrade(int gradeLevelId, int year) {
        return execute(null, () -> service.findByGrade(gradeLevelId, year));
    }

    /** Acudientes de un estudiante. */
    public OperationResult<List<Guardian>> getGuardians(int studentId) {
        return execute(null, () -> service.getGuardians(studentId));
    }

    /** Todos los acudientes. */
    public OperationResult<List<Guardian>> findAllGuardians() {
        return execute(null, service::findAllGuardians);
    }

    /** Busca un acudiente por número de documento. */
    public OperationResult<Optional<Guardian>> findGuardianByDocument(String document) {
        return execute(null, () -> service.findGuardianByDocument(document));
    }

    /** Estudiantes de un acudiente. */
    public OperationResult<List<Student>> getStudentsOfGuardian(int guardianId) {
        return execute(null, () -> service.getStudentsOfGuardian(guardianId));
    }

    /** Hermanos de un estudiante. */
    public OperationResult<List<Student>> getSiblings(int studentId) {
        return execute(null, () -> service.getSiblings(studentId));
    }

    /** Matrículas de un estudiante. */
    public OperationResult<List<Enrollment>> getEnrollments(int studentId) {
        return execute(null, () -> service.getEnrollments(studentId));
    }

    /** Grados de Preescolar a Once. */
    public OperationResult<List<GradeLevel>> getGradeLevels() {
        return execute(null, service::getGradeLevels);
    }
}
