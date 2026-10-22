package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.service.TeacherService;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acciones de las pantallas de profesores: directorio, asignaciones y horarios.
 * <p>Un controlador NO tiene reglas de negocio ni SQL: llama al servicio y envuelve el resultado en un
 * {@link OperationResult} para que la vista nunca reciba excepciones.</p>
 */
public class TeacherController extends BaseController {

    // Servicio al que se delegan las reglas de negocio
    private final TeacherService service;

    // El servicio llega por el constructor (inyección de dependencias): lo entrega AppContext
    public TeacherController(TeacherService service) {
        this.service = service;
    }

    /** Registra un profesor nuevo. */
    public OperationResult<Integer> registerTeacher(Teacher teacher) {
        // execute(mensaje, acción): ejecuta la acción y convierte errores en un resultado fallido; null = sin mensaje de éxito
        return execute("Profesor registrado correctamente.", () -> service.registerTeacher(teacher));
    }

    /** Actualiza los datos de un profesor. */
    public OperationResult<Boolean> updateTeacher(Teacher teacher) {
        return execute("Datos del profesor actualizados.", () -> service.updateTeacher(teacher));
    }

    /** Lista todos los registros. */
    public OperationResult<List<Teacher>> findAll() {
        return execute(null, service::findAll);
    }

    /** Profesores por especialización. */
    public OperationResult<List<Teacher>> findBySpecialization(String text) {
        return execute(null, () -> service.findBySpecialization(text));
    }

    /** Busca un profesor por id. */
    public OperationResult<Teacher> getTeacher(int teacherId) {
        return execute(null, () -> service.getTeacher(teacherId));
    }

    /** Busca por número de documento. */
    public OperationResult<Optional<Teacher>> findByDocument(String document) {
        return execute(null, () -> service.findByDocument(document));
    }

    /** Tabla id -> profesor para mostrar nombres. */
    public OperationResult<Map<Integer, Teacher>> teachersById() {
        return execute(null, service::teachersById);
    }

    /** Asigna una materia a un profesor. */
    public OperationResult<Integer> assignSubject(int teacherId, int subjectId, int year, String section) {
        return execute("Materia asignada al profesor.", () -> service.assignSubject(teacherId, subjectId, year, section));
    }

    /** Materias asignadas a un profesor. */
    public OperationResult<List<TeacherAssignment>> getAssignments(int teacherId) {
        return execute(null, () -> service.getAssignments(teacherId));
    }

    /** Agrega un bloque de horario validando los conflictos. */
    public OperationResult<Integer> addSchedule(Schedule schedule) {
        return execute("Bloque de horario agregado sin conflictos.", () -> service.addSchedule(schedule));
    }

    /** Horario semanal de un profesor. */
    public OperationResult<List<Schedule>> getTeacherSchedule(int teacherId) {
        return execute(null, () -> service.getTeacherSchedule(teacherId));
    }

    /** Elimina un bloque de horario. */
    public OperationResult<Boolean> removeSchedule(int scheduleId) {
        return execute("Bloque de horario eliminado.", () -> service.removeSchedule(scheduleId));
    }
}
