package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;

/**
 * Key numbers shown in the main menu header.
 */
public record DashboardStats(long activeStudents, long activeTeachers, long activeClubs, BigDecimal incomeThisMonth,
                             long pendingCharges, long openLoans) {
}
