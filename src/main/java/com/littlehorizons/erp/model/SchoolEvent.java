package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.EventType;
import java.time.LocalDate;

/**
 * School event (named SchoolEvent to avoid confusion with java.awt.Event).
 * <p>Table: {@code eventos}</p>
 */
public class SchoolEvent {

    private int id;
    private String name;
    private String description;
    private EventType type;
    private LocalDate date;
    private String place;
    private boolean competitive;
    private Integer organizerTeacherId;  // nullable
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

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public boolean isCompetitive() {
        return competitive;
    }

    public void setCompetitive(boolean competitive) {
        this.competitive = competitive;
    }

    public Integer getOrganizerTeacherId() {
        return organizerTeacherId;
    }

    public void setOrganizerTeacherId(Integer organizerTeacherId) {
        this.organizerTeacherId = organizerTeacherId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
