package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.service.TeacherService;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Actions of the teacher screens: directory, assignments and schedules.
 */
public class TeacherController extends BaseController {

    private final TeacherService service;

    public TeacherController(TeacherService service) {
        this.service = service;
    }

    public OperationResult<Integer> registerTeacher(Teacher teacher) {
        return execute("Profesor registrado correctamente.", () -> service.registerTeacher(teacher));
    }

    public OperationResult<Boolean> updateTeacher(Teacher teacher) {
        return execute("Datos del profesor actualizados.", () -> service.updateTeacher(teacher));
    }

    public OperationResult<List<Teacher>> findAll() {
        return execute(null, service::findAll);
    }

    public OperationResult<List<Teacher>> findBySpecialization(String text) {
        return execute(null, () -> service.findBySpecialization(text));
    }

    public OperationResult<Teacher> getTeacher(int teacherId) {
        return execute(null, () -> service.getTeacher(teacherId));
    }

    public OperationResult<Optional<Teacher>> findByDocument(String document) {
        return execute(null, () -> service.findByDocument(document));
    }

    public OperationResult<Map<Integer, Teacher>> teachersById() {
        return execute(null, service::teachersById);
    }

    public OperationResult<Integer> assignSubject(int teacherId, int subjectId, int year, String section) {
        return execute("Materia asignada al profesor.", () -> service.assignSubject(teacherId, subjectId, year, section));
    }

    public OperationResult<List<TeacherAssignment>> getAssignments(int teacherId) {
        return execute(null, () -> service.getAssignments(teacherId));
    }

    public OperationResult<Integer> addSchedule(Schedule schedule) {
        return execute("Bloque de horario agregado sin conflictos.", () -> service.addSchedule(schedule));
    }

    public OperationResult<List<Schedule>> getTeacherSchedule(int teacherId) {
        return execute(null, () -> service.getTeacherSchedule(teacherId));
    }

    public OperationResult<Boolean> removeSchedule(int scheduleId) {
        return execute("Bloque de horario eliminado.", () -> service.removeSchedule(scheduleId));
    }
}
