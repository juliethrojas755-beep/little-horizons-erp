package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.AttendanceStatus;
import java.time.LocalDate;

/**
 * Daily attendance of a student in a subject.
 * <p>Table: {@code asistencias}</p>
 */
public class Attendance {

    private int id;
    private int studentId;
    private int subjectId;
    private LocalDate date;
    private AttendanceStatus status;
    private String notes;
    private Integer recordedByTeacherId;  // nullable

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getRecordedByTeacherId() {
        return recordedByTeacherId;
    }

    public void setRecordedByTeacherId(Integer recordedByTeacherId) {
        this.recordedByTeacherId = recordedByTeacherId;
    }
}
