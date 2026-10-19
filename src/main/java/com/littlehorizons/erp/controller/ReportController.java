package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.dto.DashboardStats;
import com.littlehorizons.erp.model.dto.MedalCount;
import com.littlehorizons.erp.model.dto.StudentRanking;
import com.littlehorizons.erp.model.dto.SubjectAverage;
import com.littlehorizons.erp.model.dto.TeacherLoad;
import com.littlehorizons.erp.model.enums.PaymentMethod;
import com.littlehorizons.erp.model.enums.ResourceStatus;
import com.littlehorizons.erp.service.ReportService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

/**
 * Actions of the reports and statistics screen.
 */
public class ReportController extends BaseController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    public OperationResult<DashboardStats> dashboard() {
        return execute(null, () -> service.dashboard(LocalDate.now()));
    }

    public OperationResult<List<StudentRanking>> topStudents(int year, int limit) {
        return execute(null, () -> service.topStudents(year, limit));
    }

    public OperationResult<Map<String, List<SubjectAverage>>> studentsAtRisk(int year) {
        return execute(null, () -> service.studentsAtRisk(year));
    }

    public OperationResult<Map<String, Long>> performanceDistribution(int year) {
        return execute(null, () -> service.performanceDistribution(year));
    }

    public OperationResult<Map<String, BigDecimal>> averageByGradeLevel(int year) {
        return execute(null, () -> service.averageByGradeLevel(year));
    }

    public OperationResult<Map<String, Long>> studentsByGradeLevel(int year) {
        return execute(null, () -> service.studentsByGradeLevel(year));
    }

    public OperationResult<Map<String, BigDecimal>> incomeByConcept(int year) {
        return execute(null, () -> service.incomeByConcept(year));
    }

    public OperationResult<Map<Month, BigDecimal>> incomeByMonth(int year) {
        return execute(null, () -> service.incomeByMonth(year));
    }

    public OperationResult<Map<PaymentMethod, Long>> paymentsByMethod(int year) {
        return execute(null, () -> service.paymentsByMethod(year));
    }

    public OperationResult<List<TeacherLoad>> teacherLoad(int year) {
        return execute(null, () -> service.teacherLoad(year));
    }

    public OperationResult<List<MedalCount>> medalTable() {
        return execute(null, service::medalTable);
    }

    public OperationResult<Map<ResourceStatus, Long>> resourcesByStatus() {
        return execute(null, service::resourcesByStatus);
    }
}
