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
 * Actions of the academic screens: subjects, enrollments, grades, averages and attendance.
 */
public class AcademicController extends BaseController {

    private final AcademicService academicService;
    private final AttendanceService attendanceService;

    public AcademicController(AcademicService academicService, AttendanceService attendanceService) {
        this.academicService = academicService;
        this.attendanceService = attendanceService;
    }

    public OperationResult<Integer> createSubject(Subject subject) {
        return execute("Materia creada correctamente.", () -> academicService.createSubject(subject));
    }

    public OperationResult<List<Subject>> findAllSubjects() {
        return execute(null, academicService::findAllSubjects);
    }

    public OperationResult<List<Subject>> findSubjectsByGrade(int gradeLevelId) {
        return execute(null, () -> academicService.findSubjectsByGrade(gradeLevelId));
    }

    public OperationResult<Map<Integer, Subject>> subjectsById() {
        return execute(null, academicService::subjectsById);
    }

    public OperationResult<Integer> enrollInSubject(int studentId, int subjectId, int year, Term term) {
        return execute("Inscripción en la materia registrada.", () -> academicService.enrollInSubject(studentId, subjectId, year, term));
    }

    public OperationResult<Long> enrollInAllGradeSubjects(int studentId, int year, Term term) {
        return execute(null, () -> academicService.enrollInAllGradeSubjects(studentId, year, term));
    }

    public OperationResult<List<SubjectEnrollment>> getSubjectEnrollments(int studentId) {
        return execute(null, () -> academicService.getSubjectEnrollments(studentId));
    }

    public OperationResult<Integer> recordGrade(int studentId, int subjectId, int year, Term term, BigDecimal grade, String notes) {
        return execute("Nota registrada correctamente.",
                () -> academicService.recordGrade(studentId, subjectId, year, term, grade, notes));
    }

    public OperationResult<BigDecimal> closeFinalGrade(int studentId, int subjectId, int year) {
        return execute("Nota final calculada y guardada.", () -> academicService.closeFinalGrade(studentId, subjectId, year));
    }

    public OperationResult<List<SubjectAverage>> averageBySubject(int studentId, int year) {
        return execute(null, () -> academicService.averageBySubject(studentId, year));
    }

    public OperationResult<Optional<BigDecimal>> termAverage(int studentId, int year, Term term) {
        return execute(null, () -> academicService.termAverage(studentId, year, term));
    }

    public OperationResult<List<SubjectAverage>> courseAverageBySubject(int gradeLevelId, int year) {
        return execute(null, () -> academicService.courseAverageBySubject(gradeLevelId, year));
    }

    public OperationResult<Map<Integer, Map<Term, List<AcademicRecord>>>> academicHistory(int studentId) {
        return execute(null, () -> academicService.academicHistory(studentId));
    }

    public OperationResult<Integer> registerAttendance(int studentId, int subjectId, LocalDate date, AttendanceStatus status,
                                                       Integer teacherId, String notes) {
        return execute("Asistencia registrada.",
                () -> attendanceService.register(studentId, subjectId, date, status, teacherId, notes));
    }

    public OperationResult<List<Attendance>> attendanceOf(int studentId) {
        return execute(null, () -> attendanceService.findByStudent(studentId));
    }

    public OperationResult<Map<AttendanceStatus, Long>> attendanceSummary(int studentId) {
        return execute(null, () -> attendanceService.summaryByStatus(studentId));
    }

    public OperationResult<List<AbsencePattern>> absencePatterns(LocalDate from, LocalDate to) {
        return execute(null, () -> attendanceService.absencePatterns(from, to));
    }
}
