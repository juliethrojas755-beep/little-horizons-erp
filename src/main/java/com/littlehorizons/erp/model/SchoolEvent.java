package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.EventType;
import java.time.LocalDate;

/**
 * Evento del colegio (se llama SchoolEvent para no confundirse con java.awt.Event).
 * <p>Tabla: {@code eventos}</p>
 */
public class SchoolEvent {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla eventos) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Nombre del evento
    private String name;
    // Descripción (texto libre)
    private String description;
    // Tipo (se guarda como texto en español en la base de datos)
    private EventType type;
    // Fecha del evento
    private LocalDate date;
    // Lugar donde se realiza
    private String place;
    // true si el evento tiene competencia con posiciones
    private boolean competitive;
    // Id del profesor organizador (puede ser null)
    private Integer organizerTeacherId;
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

    /** Devuelve el tipo. */
    public EventType getType() {
        return type;
    }

    /** Asigna el tipo. */
    public void setType(EventType type) {
        this.type = type;
    }

    /** Devuelve la fecha. */
    public LocalDate getDate() {
        return date;
    }

    /** Asigna la fecha. */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /** Devuelve el lugar. */
    public String getPlace() {
        return place;
    }

    /** Asigna el lugar. */
    public void setPlace(String place) {
        this.place = place;
    }

    /** Devuelve si tiene competencia. */
    public boolean isCompetitive() {
        return competitive;
    }

    /** Asigna si tiene competencia. */
    public void setCompetitive(boolean competitive) {
        this.competitive = competitive;
    }

    /** Devuelve el id del profesor organizador. */
    public Integer getOrganizerTeacherId() {
        return organizerTeacherId;
    }

    /** Asigna el id del profesor organizador. */
    public void setOrganizerTeacherId(Integer organizerTeacherId) {
        this.organizerTeacherId = organizerTeacherId;
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
