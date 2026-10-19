package com.littlehorizons.erp.model.dto;

import java.time.LocalDate;

/**
 * A student that exceeded the allowed absences in one week.
 *
 * @param weekStart Monday of the week with the pattern
 */
public record AbsencePattern(int studentId, String studentName, LocalDate weekStart, long absences) {
}
