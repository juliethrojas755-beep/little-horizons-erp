package com.littlehorizons.erp.model.dto;

/**
 * Carga académica de un profesor en un año.
 *
 * @param teacherName    nombre del profesor
 * @param specialization su área de especialización
 * @param subjects       cuántas materias tiene asignadas
 * @param weeklyBlocks   cuántos bloques de clase tiene por semana
 */
public record TeacherLoad(String teacherName, String specialization, long subjects, long weeklyBlocks) {
}
