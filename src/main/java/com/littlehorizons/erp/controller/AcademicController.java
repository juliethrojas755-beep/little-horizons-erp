package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.dto.AbsencePattern;
import com.littlehorizons.erp.model.dto.SubjectAverage;
import com.littlehorizons.erp.model.enums.AttendanceStatus;
import com.littlehorizons.erp.model.enums.Term;
import com.littlehorizons.erp.service.AcademicService;
import com.littlehorizons.erp.service.AttendanceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acciones de las pantallas académicas: materias, inscripciones, notas, promedios y asistencia.
 * <p>Un controlador NO tiene reglas de negocio ni SQL: llama al servicio y envuelve el resultado en un
 * {@link OperationResult} para que la vista nunca reciba excepciones.</p>
 */
public class AcademicController extends BaseController {

    // Servicio al que se delegan las reglas de negocio
    private final AcademicService academicService;
    private final AttendanceService attendanceService;

    // El servicio llega por el constructor (inyección de dependencias): lo entrega AppContext
    public AcademicController(AcademicService academicService, AttendanceService attendanceService) {
        this.academicService = academicService;
        this.attendanceService = attendanceService;
    }

    /** Crea una materia. */
    public OperationResult<Integer> createSubject(Subject subject) {
        // execute(mensaje, acción): ejecuta la acción y convierte errores en un resultado fallido; null = sin mensaje de éxito
        return execute("Materia creada correctamente.", () -> academicService.createSubject(subject));
    }

    /** Todas las materias. */
    public OperationResult<List<Subject>> findAllSubjects() {
        return execute(null, academicService::findAllSubjects);
    }

    /** Materias de un grado. */
    public OperationResult<List<Subject>> findSubjectsByGrade(int gradeLevelId) {
        return execute(null, () -> academicService.findSubjectsByGrade(gradeLevelId));
    }

    /** Tabla id -> materia para mostrar nombres. */
    public OperationResult<Map<Integer, Subject>> subjectsById() {
        return execute(null, academicService::subjectsById);
    }

    /** Inscribe a un estudiante en una materia de su grado. */
    public OperationResult<Integer> enrollInSubject(int studentId, int subjectId, int year, Term term) {
        return execute("Inscripción en la materia registrada.", () -> academicService.enrollInSubject(studentId, subjectId, year, term));
    }

    /** Inscribe a un estudiante en todas las materias de su grado. */
    public OperationResult<Long> enrollInAllGradeSubjects(int studentId, int year, Term term) {
        return execute(null, () -> academicService.enrollInAllGradeSubjects(studentId, year, term));
    }

    /** Inscripciones de un estudiante en materias. */
    public OperationResult<List<SubjectEnrollment>> getSubjectEnrollments(int studentId) {
        return execute(null, () -> academicService.getSubjectEnrollments(studentId));
    }

    /** Registra o actualiza una nota. */
    public OperationResult<Integer> recordGrade(int studentId, int subjectId, int year, Term term, BigDecimal grade, String notes) {
        return execute("Nota registrada correctamente.",
                () -> academicService.recordGrade(studentId, subjectId, year, term, grade, notes));
    }

    /** Calcula y guarda la nota final de una materia. */
    public OperationResult<BigDecimal> closeFinalGrade(int studentId, int subjectId, int year) {
        return execute("Nota final calculada y guardada.", () -> academicService.closeFinalGrade(studentId, subjectId, year));
    }

    /** Promedio de cada materia de un estudiante. */
    public OperationResult<List<SubjectAverage>> averageBySubject(int studentId, int year) {
        return execute(null, () -> academicService.averageBySubject(studentId, year));
    }

    /** Promedio general del estudiante en un período. */
    public OperationResult<Optional<BigDecimal>> termAverage(int studentId, int year, Term term) {
        return execute(null, () -> academicService.termAverage(studentId, year, term));
    }

    /** Promedio del curso en cada materia. */
    public OperationResult<List<SubjectAverage>> courseAverageBySubject(int gradeLevelId, int year) {
        return execute(null, () -> academicService.courseAverageBySubject(gradeLevelId, year));
    }

    /** Historial académico completo agrupado por año y período. */
    public OperationResult<Map<Integer, Map<Term, List<AcademicRecord>>>> academicHistory(int studentId) {
        return execute(null, () -> academicService.academicHistory(studentId));
    }

    public OperationResult<Integer> registerAttendance(int studentId, int subjectId, LocalDate date, AttendanceStatus status,
                                                       Integer teacherId, String notes) {
        return execute("Asistencia registrada.",
                () -> attendanceService.register(studentId, subjectId, date, status, teacherId, notes));
    }

    /** Registros de asistencia de un estudiante. */
    public OperationResult<List<Attendance>> attendanceOf(int studentId) {
        return execute(null, () -> attendanceService.findByStudent(studentId));
    }

    /** Resumen de asistencia por estado. */
    public OperationResult<Map<AttendanceStatus, Long>> attendanceSummary(int studentId) {
        return execute(null, () -> attendanceService.summaryByStatus(studentId));
    }

    /** Estudiantes con más de 3 ausencias en una semana. */
    public OperationResult<List<AbsencePattern>> absencePatterns(LocalDate from, LocalDate to) {
        return execute(null, () -> attendanceService.absencePatterns(from, to));
    }
}
