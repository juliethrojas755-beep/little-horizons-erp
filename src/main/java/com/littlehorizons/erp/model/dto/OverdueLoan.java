package com.littlehorizons.erp.model.dto;

import java.time.LocalDate;

/**
 * Préstamo que no se devolvió a tiempo (de un estudiante o de un profesor).
 *
 * @param loanId       id del préstamo
 * @param borrowerType "Estudiante" o "Profesor"
 * @param borrowerName nombre de quien tiene el recurso
 * @param resourceCode código de inventario del recurso
 * @param resourceName nombre del recurso
 * @param dueDate      fecha en la que debía devolverse
 * @param daysOverdue  días de retraso
 */
public record OverdueLoan(int loanId, String borrowerType, String borrowerName, String resourceCode,
                          String resourceName, LocalDate dueDate, long daysOverdue) {
}
