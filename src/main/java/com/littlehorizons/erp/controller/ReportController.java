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
 * Acciones de la pantalla de reportes y estadísticas.
 * <p>Un controlador NO tiene reglas de negocio ni SQL: llama al servicio y envuelve el resultado en un
 * {@link OperationResult} para que la vista nunca reciba excepciones.</p>
 */
public class ReportController extends BaseController {

    // Servicio al que se delegan las reglas de negocio
    private final ReportService service;

    // El servicio llega por el constructor (inyección de dependencias): lo entrega AppContext
    public ReportController(ReportService service) {
        this.service = service;
    }

    /** Cifras del tablero del menú principal. */
    public OperationResult<DashboardStats> dashboard() {
        // execute(mensaje, acción): ejecuta la acción y convierte errores en un resultado fallido; null = sin mensaje de éxito
        return execute(null, () -> service.dashboard(LocalDate.now()));
    }

    /** Ranking de los mejores promedios del año (cuadro de honor). */
    public OperationResult<List<StudentRanking>> topStudents(int year, int limit) {
        return execute(null, () -> service.topStudents(year, limit));
    }

    /** Estudiantes con materias por debajo de 3.0. */
    public OperationResult<Map<String, List<SubjectAverage>>> studentsAtRisk(int year) {
        return execute(null, () -> service.studentsAtRisk(year));
    }

    /** Cuántas notas hay en cada nivel de desempeño. */
    public OperationResult<Map<String, Long>> performanceDistribution(int year) {
        return execute(null, () -> service.performanceDistribution(year));
    }

    /** Promedio general de cada grado. */
    public OperationResult<Map<String, BigDecimal>> averageByGradeLevel(int year) {
        return execute(null, () -> service.averageByGradeLevel(year));
    }

    /** Cantidad de estudiantes matriculados por grado. */
    public OperationResult<Map<String, Long>> studentsByGradeLevel(int year) {
        return execute(null, () -> service.studentsByGradeLevel(year));
    }

    /** Ingresos del año por concepto. */
    public OperationResult<Map<String, BigDecimal>> incomeByConcept(int year) {
        return execute(null, () -> service.incomeByConcept(year));
    }

    /** Ingresos del año por mes. */
    public OperationResult<Map<Month, BigDecimal>> incomeByMonth(int year) {
        return execute(null, () -> service.incomeByMonth(year));
    }

    /** Número de pagos por método. */
    public OperationResult<Map<PaymentMethod, Long>> paymentsByMethod(int year) {
        return execute(null, () -> service.paymentsByMethod(year));
    }

    /** Carga académica de cada profesor (materias y bloques por semana). */
    public OperationResult<List<TeacherLoad>> teacherLoad(int year) {
        return execute(null, () -> service.teacherLoad(year));
    }

    /** Medallero de los eventos con competencia. */
    public OperationResult<List<MedalCount>> medalTable() {
        return execute(null, service::medalTable);
    }

    /** Cantidad de recursos por estado. */
    public OperationResult<Map<ResourceStatus, Long>> resourcesByStatus() {
        return execute(null, service::resourcesByStatus);
    }
}
