package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.Term;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Grade obtained by a student in a subject and term (0.0 to 5.0).
 * <p>Table: {@code registros_academicos}</p>
 */
public class AcademicRecord {

    private int id;
    private int studentId;
    private int subjectId;
    private int teacherId;
    private int academicYear;
    private Term term;
    private BigDecimal grade;  // DECIMAL(2,1)
    private String notes;
    private LocalDate recordDate;

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

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public BigDecimal getGrade() {
        return grade;
    }

    public void setGrade(BigDecimal grade) {
        this.grade = grade;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    /** Minimum grade to pass a subject in the Colombian scale. */
    public static final BigDecimal PASSING_GRADE = new BigDecimal("3.0");

    /** @return {@code true} when the grade is 3.0 or higher */
    public boolean isPassing() {
        return grade.compareTo(PASSING_GRADE) >= 0;
    }
}
