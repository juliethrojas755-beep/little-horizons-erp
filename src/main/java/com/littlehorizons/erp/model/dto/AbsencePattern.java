package com.littlehorizons.erp.model.dto;

import java.time.LocalDate;

/**
 * Estudiante que superó el límite de ausencias en una misma semana.
 *
 * @param studentId   id del estudiante
 * @param studentName nombre completo del estudiante
 * @param weekStart   lunes de la semana en la que se presentó el patrón
 * @param absences    número de ausencias en esa semana
 */
public record AbsencePattern(int studentId, String studentName, LocalDate weekStart, long absences) {
}
