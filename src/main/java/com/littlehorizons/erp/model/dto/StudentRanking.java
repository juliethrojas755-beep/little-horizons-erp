package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;

/**
 * Puesto de un estudiante en el ranking por promedio general (cuadro de honor).
 *
 * @param position    puesto en el ranking (1 = mejor promedio)
 * @param studentId   id del estudiante
 * @param studentName nombre completo del estudiante
 * @param gradeName   grado en el que está matriculado
 * @param average     promedio general
 */
public record StudentRanking(int position, int studentId, String studentName, String gradeName, BigDecimal average) {
}
