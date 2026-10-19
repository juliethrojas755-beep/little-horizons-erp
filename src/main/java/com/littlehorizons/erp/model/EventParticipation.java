package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.EventPosition;
import com.littlehorizons.erp.model.enums.ParticipationRole;

/**
 * Participation of a student in an event, with the position when it is competitive.
 * <p>Table: {@code participantes_evento}</p>
 */
public class EventParticipation {

    private int id;
    private int eventId;
    private int studentId;
    private ParticipationRole role;
    private EventPosition position;  // null when the event has no competition
    private String notes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public ParticipationRole getRole() {
        return role;
    }

    public void setRole(ParticipationRole role) {
        this.role = role;
    }

    public EventPosition getPosition() {
        return position;
    }

    public void setPosition(EventPosition position) {
        this.position = position;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
