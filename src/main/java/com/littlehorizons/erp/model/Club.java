package com.littlehorizons.erp.model;

/**
 * Extracurricular club with a maximum capacity.
 * <p>Table: {@code clubes}</p>
 */
public class Club {

    private int id;
    private String name;
    private String description;
    private Integer supervisorTeacherId;  // nullable
    private int maxCapacity;
    private String meetingSchedule;
    private boolean active;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSupervisorTeacherId() {
        return supervisorTeacherId;
    }

    public void setSupervisorTeacherId(Integer supervisorTeacherId) {
        this.supervisorTeacherId = supervisorTeacherId;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getMeetingSchedule() {
        return meetingSchedule;
    }

    public void setMeetingSchedule(String meetingSchedule) {
        this.meetingSchedule = meetingSchedule;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
