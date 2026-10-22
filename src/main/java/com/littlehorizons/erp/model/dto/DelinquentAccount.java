package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un cargo vencido (moroso) con los datos necesarios para contactar a la familia.
 *
 * @param chargeId      id del cargo pendiente
 * @param studentName   nombre del estudiante
 * @param guardianName  nombre del acudiente principal
 * @param guardianPhone teléfono del acudiente
 * @param conceptName   concepto que se debe (pensión, matrícula...)
 * @param amount        valor que se debe
 * @param dueDate       fecha en que venció
 * @param daysOverdue   días de mora desde el vencimiento
 */
public record DelinquentAccount(int chargeId, String studentName, String guardianName, String guardianPhone,
                                String conceptName, BigDecimal amount, LocalDate dueDate, long daysOverdue) {
}
