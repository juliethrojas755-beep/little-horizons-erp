package com.littlehorizons.erp.model.dto;

import java.time.LocalDate;

/**
 * Loan not returned on time, for students or teachers.
 *
 * @param borrowerType "Estudiante" or "Profesor"
 */
public record OverdueLoan(int loanId, String borrowerType, String borrowerName, String resourceCode,
                          String resourceName, LocalDate dueDate, long daysOverdue) {
}
