package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.WeekDay;
import java.time.LocalTime;

/**
 * Weekly time block of a teacher assignment.
 * <p>Table: {@code horarios}</p>
 */
public class Schedule {

    private int id;
    private int assignmentId;
    private WeekDay weekDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private String classroom;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public WeekDay getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(WeekDay weekDay) {
        this.weekDay = weekDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    /**
     * Two blocks overlap when they are on the same day and each one starts before the other ends.
     *
     * @param other block to compare with
     * @return {@code true} when both blocks share at least one minute
     */
    public boolean overlapsWith(Schedule other) {
        return weekDay == other.weekDay
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }
}
