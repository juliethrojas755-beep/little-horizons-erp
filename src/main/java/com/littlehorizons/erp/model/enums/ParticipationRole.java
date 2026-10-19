package com.littlehorizons.erp.model.enums;

/**
 * Role of a student in an event.
 */
public enum ParticipationRole implements DbEnum {
    PARTICIPANT("Participante"),
    COMPETITOR("Competidor"),
    ORGANIZER("Organizador"),
    ATTENDEE("Asistente");

    private final String dbValue;

    ParticipationRole(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static ParticipationRole fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(ParticipationRole.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
