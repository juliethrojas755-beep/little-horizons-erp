package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;

/**
 * Promedio de una materia (de un estudiante o de todo un curso).
 * <p>
 * Es un {@code record}: una clase inmutable que Java construye sola con constructor,
 * getters (por ejemplo {@code average()}), equals, hashCode y toString.
 * Se usa para llevar los resultados de un cálculo desde el servicio hasta la vista.
 * </p>
 *
 * @param subjectId   id de la materia
 * @param subjectName nombre de la materia para mostrar
 * @param average     promedio redondeado a un decimal
 * @param gradesCount cuántas notas se promediaron
 */
public record SubjectAverage(int subjectId, String subjectName, BigDecimal average, long gradesCount) {
}
