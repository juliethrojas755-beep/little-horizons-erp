package com.littlehorizons.erp.model;

/**
 * Club extracurricular con cupo máximo.
 * <p>Tabla: {@code clubes}</p>
 */
public class Club {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla clubes) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Nombre del club (es único)
    private String name;
    // Descripción (texto libre)
    private String description;
    // Id del profesor encargado (puede ser null)
    private Integer supervisorTeacherId;
    // Cupo máximo de estudiantes
    private int maxCapacity;
    // Días y hora de reunión, por ejemplo Martes 3:00-5:00 PM
    private String meetingSchedule;
    // true si sigue activo; false si fue desactivado
    private boolean active;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el identificador único. */
    public int getId() {
        return id;
    }

    /** Asigna el identificador único. */
    public void setId(int id) {
        this.id = id;
    }

    /** Devuelve el nombre. */
    public String getName() {
        return name;
    }

    /** Asigna el nombre. */
    public void setName(String name) {
        this.name = name;
    }

    /** Devuelve la descripción. */
    public String getDescription() {
        return description;
    }

    /** Asigna la descripción. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Devuelve el id del profesor encargado. */
    public Integer getSupervisorTeacherId() {
        return supervisorTeacherId;
    }

    /** Asigna el id del profesor encargado. */
    public void setSupervisorTeacherId(Integer supervisorTeacherId) {
        this.supervisorTeacherId = supervisorTeacherId;
    }

    /** Devuelve el cupo máximo. */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /** Asigna el cupo máximo. */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /** Devuelve el horario de reunión. */
    public String getMeetingSchedule() {
        return meetingSchedule;
    }

    /** Asigna el horario de reunión. */
    public void setMeetingSchedule(String meetingSchedule) {
        this.meetingSchedule = meetingSchedule;
    }

    /** Devuelve si está activo. */
    public boolean isActive() {
        return active;
    }

    /** Asigna si está activo. */
    public void setActive(boolean active) {
        this.active = active;
    }
}
