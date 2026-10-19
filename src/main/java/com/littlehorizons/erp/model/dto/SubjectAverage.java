package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;

/**
 * Average grade of one subject (for a student or for a whole course).
 *
 * @param subjectId   subject id
 * @param subjectName subject name to display
 * @param average     average rounded to one decimal
 * @param gradesCount how many grades were averaged
 */
public record SubjectAverage(int subjectId, String subjectName, BigDecimal average, long gradesCount) {
}
