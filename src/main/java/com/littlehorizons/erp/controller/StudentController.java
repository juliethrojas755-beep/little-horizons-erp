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
 * Receives the actions of the student screens and delegates to {@link StudentService}.
 */
public class StudentController extends BaseController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    public OperationResult<Integer> registerStudent(Student student, int guardianId) {
        return execute("Estudiante registrado correctamente.", () -> service.registerStudent(student, guardianId));
    }

    public OperationResult<Integer> registerGuardian(Guardian guardian) {
        return execute("Acudiente registrado correctamente.", () -> service.registerGuardian(guardian));
    }

    public OperationResult<Void> linkGuardian(int studentId, int guardianId, boolean primary) {
        return run("Acudiente vinculado al estudiante.", () -> service.linkGuardian(studentId, guardianId, primary));
    }

    public OperationResult<Boolean> updateStudent(Student student) {
        return execute("Datos del estudiante actualizados.", () -> service.updateStudent(student));
    }

    public OperationResult<Void> changeStatus(int studentId, StudentStatus status, int year) {
        return run("Estado actualizado a " + status + ".", () -> service.changeStatus(studentId, status, year));
    }

    public OperationResult<Integer> enroll(int studentId, int gradeLevelId, int year) {
        return execute("Matrícula creada para el año " + year + ".", () -> service.enroll(studentId, gradeLevelId, year));
    }

    public OperationResult<Student> getStudent(int studentId) {
        return execute(null, () -> service.getStudent(studentId));
    }

    public OperationResult<Optional<Student>> findByDocument(String document) {
        return execute(null, () -> service.findByDocument(document));
    }

    public OperationResult<List<Student>> findAll() {
        return execute(null, service::findAll);
    }

    public OperationResult<List<Student>> search(String text) {
        return execute(null, () -> service.search(text));
    }

    public OperationResult<List<Student>> findByGrade(int gradeLevelId, int year) {
        return execute(null, () -> service.findByGrade(gradeLevelId, year));
    }

    public OperationResult<List<Guardian>> getGuardians(int studentId) {
        return execute(null, () -> service.getGuardians(studentId));
    }

    public OperationResult<List<Guardian>> findAllGuardians() {
        return execute(null, service::findAllGuardians);
    }

    public OperationResult<Optional<Guardian>> findGuardianByDocument(String document) {
        return execute(null, () -> service.findGuardianByDocument(document));
    }

    public OperationResult<List<Student>> getStudentsOfGuardian(int guardianId) {
        return execute(null, () -> service.getStudentsOfGuardian(guardianId));
    }

    public OperationResult<List<Student>> getSiblings(int studentId) {
        return execute(null, () -> service.getSiblings(studentId));
    }

    public OperationResult<List<Enrollment>> getEnrollments(int studentId) {
        return execute(null, () -> service.getEnrollments(studentId));
    }

    public OperationResult<List<GradeLevel>> getGradeLevels() {
        return execute(null, service::getGradeLevels);
    }
}
