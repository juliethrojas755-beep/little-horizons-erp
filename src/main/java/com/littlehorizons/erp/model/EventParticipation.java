package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.EventPosition;
import com.littlehorizons.erp.model.enums.ParticipationRole;

/**
 * Participación de un estudiante en un evento, con su posición si hay competencia.
 * <p>Tabla: {@code participantes_evento}</p>
 */
public class EventParticipation {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla participantes_evento) ----------
    // Identificador único de la participación
    private int id;
    // Id del evento (llave foránea hacia eventos)
    private int eventId;
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Rol dentro del evento (participante, competidor...)
    private ParticipationRole role;
    // Posición obtenida (null si el evento no tiene competencia)
    private EventPosition position;
    // Observaciones (texto libre, opcional)
    private String notes;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el identificador de la participación. */
    public int getId() {
        return id;
    }

    /** Asigna el identificador de la participación. */
    public void setId(int id) {
        this.id = id;
    }

    /** Devuelve el id del evento. */
    public int getEventId() {
        return eventId;
    }

    /** Asigna el id del evento. */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    /** Devuelve el id del estudiante. */
    public int getStudentId() {
        return studentId;
    }

    /** Asigna el id del estudiante. */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /** Devuelve el rol. */
    public ParticipationRole getRole() {
        return role;
    }

    /** Asigna el rol. */
    public void setRole(ParticipationRole role) {
        this.role = role;
    }

    /** Devuelve la posición. */
    public EventPosition getPosition() {
        return position;
    }

    /** Asigna la posición. */
    public void setPosition(EventPosition position) {
        this.position = position;
    }

    /** Devuelve las observaciones. */
    public String getNotes() {
        return notes;
    }

    /** Asigna las observaciones. */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
