package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.dto.AbsencePattern;
import com.littlehorizons.erp.model.enums.AttendanceStatus;
import com.littlehorizons.erp.util.BusinessException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Asistencia diaria y detección de patrones de inasistencia.
 */
public class AttendanceService {

    /** Más ausencias que este número en una misma semana se reporta como patrón de inasistencia. */
    public static final int WEEKLY_ABSENCE_LIMIT = 3;

    private final AttendanceDao attendanceDao;
    private final StudentDao studentDao;

    public AttendanceService(AttendanceDao attendanceDao, StudentDao studentDao) {
        this.attendanceDao = attendanceDao;
        this.studentDao = studentDao;
    }

    /** Registra la asistencia de un estudiante en una materia y fecha. */
    public int register(int studentId, int subjectId, LocalDate date, AttendanceStatus status, Integer teacherId, String notes) {
        // No se puede registrar asistencia del futuro
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException("No se puede registrar asistencia en una fecha futura.");
        }
        // Solo hay clases de lunes a viernes
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BusinessException("La asistencia solo se registra de lunes a viernes.");
        }
        // Un estudiante solo puede tener un registro por materia y día
        boolean duplicated = attendanceDao.findByStudent(studentId).stream()
                .anyMatch(a -> a.getSubjectId() == subjectId && a.getDate().equals(date));
        if (duplicated) {
            throw new BusinessException("Ya existe asistencia de ese estudiante en esa materia para el " + date + ".");
        }
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setSubjectId(subjectId);
        attendance.setDate(date);
        attendance.setStatus(status);
        attendance.setRecordedByTeacherId(teacherId);
        attendance.setNotes(notes);
        return attendanceDao.insert(attendance);
    }

    public List<Attendance> findByStudent(int studentId) {
        return attendanceDao.findByStudent(studentId);
    }

    /** Cuántos registros de cada estado tiene el estudiante (presente, ausente, tardanza...). */
    public Map<AttendanceStatus, Long> summaryByStatus(int studentId) {
        return attendanceDao.findByStudent(studentId).stream()
                // groupingBy + counting: cuenta cuántas veces aparece cada estado
                .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
    }

    /**
     * Estudiantes con más de {@link #WEEKLY_ABSENCE_LIMIT} ausencias en una misma semana.
     * Agrupa por (estudiante, lunes de la semana) y se queda con los grupos que superan el límite.
     */
    public List<AbsencePattern> absencePatterns(LocalDate from, LocalDate to) {
        // Tabla id -> estudiante para poder mostrar el nombre en el resultado
        Map<Integer, Student> students = studentDao.findAll().stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        // Record local: la "llave" de agrupación es la pareja (estudiante, semana)
        record StudentWeek(int studentId, LocalDate weekStart) {
        }

        return attendanceDao.findBetween(from, to).stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)         // solo las ausencias
                .collect(Collectors.groupingBy(
                        // la semana se identifica por su lunes (previousOrSame devuelve el mismo día si ya es lunes)
                        a -> new StudentWeek(a.getStudentId(), a.getDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))),
                        Collectors.counting()))                                 // cuenta las ausencias de cada (estudiante, semana)
                .entrySet().stream()
                .filter(entry -> entry.getValue() > WEEKLY_ABSENCE_LIMIT)       // solo los que superan el límite
                .map(entry -> new AbsencePattern(entry.getKey().studentId(),
                        nameOf(students.get(entry.getKey().studentId())), entry.getKey().weekStart(), entry.getValue()))
                .sorted(Comparator.comparingLong(AbsencePattern::absences).reversed())   // primero los que más faltaron
                .toList();
    }

    /** Nombre del estudiante, o "Desconocido" si no se encuentra. */
    private String nameOf(Student student) {
        return student == null ? "Desconocido" : student.getFullName();
    }
}
