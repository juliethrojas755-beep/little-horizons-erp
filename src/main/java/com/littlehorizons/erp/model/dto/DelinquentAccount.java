package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One overdue charge with the data needed to contact the family.
 */
public record DelinquentAccount(int chargeId, String studentName, String guardianName, String guardianPhone,
                                String conceptName, BigDecimal amount, LocalDate dueDate, long daysOverdue) {
}
