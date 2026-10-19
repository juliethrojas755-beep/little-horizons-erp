package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;

/**
 * Position of a student in a ranking by general average.
 */
public record StudentRanking(int position, int studentId, String studentName, String gradeName, BigDecimal average) {
}
