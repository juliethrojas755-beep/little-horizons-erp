package com.littlehorizons.erp.model;

/**
 * Link between a student and a guardian (many to many).
 * <p>Table: {@code estudiante_acudiente}</p>
 */
public class StudentGuardian {

    private int studentId;
    private int guardianId;
    private boolean primary;  // main contact for communications
    private boolean emergencyContact;

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(boolean emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
