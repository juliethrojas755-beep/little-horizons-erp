package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.WeekDay;
import java.time.LocalTime;

/**
 * Bloque semanal de horario (día y hora) de una asignación de profesor.
 * <p>Tabla: {@code horarios}</p>
 */
public class Schedule {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla horarios) ----------
    // Identificador único del bloque de horario
    private int id;
    // Id de la asignación profesor-materia a la que pertenece
    private int assignmentId;
    // Día de la semana (lunes a viernes)
    private WeekDay weekDay;
    // Hora en que empieza la clase
    private LocalTime startTime;
    // Hora en que termina la clase
    private LocalTime endTime;
    // Salón donde se dicta
    private String classroom;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el identificador del bloque. */
    public int getId() {
        return id;
    }

    /** Asigna el identificador del bloque. */
    public void setId(int id) {
        this.id = id;
    }

    /** Devuelve el id de la asignación. */
    public int getAssignmentId() {
        return assignmentId;
    }

    /** Asigna el id de la asignación. */
    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    /** Devuelve el día de la semana. */
    public WeekDay getWeekDay() {
        return weekDay;
    }

    /** Asigna el día de la semana. */
    public void setWeekDay(WeekDay weekDay) {
        this.weekDay = weekDay;
    }

    /** Devuelve la hora de inicio. */
    public LocalTime getStartTime() {
        return startTime;
    }

    /** Asigna la hora de inicio. */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /** Devuelve la hora de fin. */
    public LocalTime getEndTime() {
        return endTime;
    }

    /** Asigna la hora de fin. */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /** Devuelve el salón. */
    public String getClassroom() {
        return classroom;
    }

    /** Asigna el salón. */
    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    // ---------- Regla de negocio de la entidad ----------

    /**
     * Dos bloques se cruzan cuando son el mismo día y cada uno empieza antes de que el otro termine.
     *
     * @param other bloque con el que se compara
     * @return {@code true} cuando comparten al menos un minuto
     */
    public boolean overlapsWith(Schedule other) {
        return weekDay == other.weekDay                    // 1) mismo día de la semana
                && startTime.isBefore(other.endTime)       // 2) este empieza antes de que el otro termine
                && other.startTime.isBefore(endTime);      // 3) el otro empieza antes de que este termine
    }
}
