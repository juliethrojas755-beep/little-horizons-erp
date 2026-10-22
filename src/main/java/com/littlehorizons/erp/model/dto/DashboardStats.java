package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;

/**
 * Cifras principales que se muestran en el tablero del menú principal.
 *
 * @param activeStudents estudiantes con estado Activo
 * @param activeTeachers profesores con estado Activo
 * @param activeClubs    clubes activos
 * @param incomeThisMonth dinero recaudado en el mes actual
 * @param pendingCharges cargos que aún no se han pagado
 * @param openLoans      préstamos sin devolver (estudiantes y profesores)
 */
public record DashboardStats(long activeStudents, long activeTeachers, long activeClubs, BigDecimal incomeThisMonth,
                             long pendingCharges, long openLoans) {
}
