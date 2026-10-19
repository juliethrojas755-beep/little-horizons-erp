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
 * Daily attendance and detection of absence patterns.
 */
public class AttendanceService {

    /** More than this number of absences in one week is reported. */
    public static final int WEEKLY_ABSENCE_LIMIT = 3;

    private final AttendanceDao attendanceDao;
    private final StudentDao studentDao;

    public AttendanceService(AttendanceDao attendanceDao, StudentDao studentDao) {
        this.attendanceDao = attendanceDao;
        this.studentDao = studentDao;
    }

    public int register(int studentId, int subjectId, LocalDate date, AttendanceStatus status, Integer teacherId, String notes) {
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException("No se puede registrar asistencia en una fecha futura.");
        }
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BusinessException("La asistencia solo se registra de lunes a viernes.");
        }
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

    /** How many records of each status the student has. */
    public Map<AttendanceStatus, Long> summaryByStatus(int studentId) {
        return attendanceDao.findByStudent(studentId).stream()
                .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
    }

    /**
     * Students with more than {@link #WEEKLY_ABSENCE_LIMIT} absences in the same week.
     * Groups by (student, Monday of the week) and keeps the groups above the limit.
     */
    public List<AbsencePattern> absencePatterns(LocalDate from, LocalDate to) {
        Map<Integer, Student> students = studentDao.findAll().stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        record StudentWeek(int studentId, LocalDate weekStart) {
        }

        return attendanceDao.findBetween(from, to).stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .collect(Collectors.groupingBy(
                        a -> new StudentWeek(a.getStudentId(), a.getDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))),
                        Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > WEEKLY_ABSENCE_LIMIT)
                .map(entry -> new AbsencePattern(entry.getKey().studentId(),
                        nameOf(students.get(entry.getKey().studentId())), entry.getKey().weekStart(), entry.getValue()))
                .sorted(Comparator.comparingLong(AbsencePattern::absences).reversed())
                .toList();
    }

    private String nameOf(Student student) {
        return student == null ? "Desconocido" : student.getFullName();
    }
}
