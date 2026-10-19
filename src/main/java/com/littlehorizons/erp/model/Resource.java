package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.ResourceStatus;
import java.time.LocalDate;

/**
 * Loanable school resource (library and labs).
 * <p>Table: {@code recursos}</p>
 */
public class Resource {

    private int id;
    private String code;  // unique inventory code
    private String name;
    private int categoryId;
    private String description;
    private LocalDate acquisitionDate;
    private String location;
    private ResourceStatus status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    /** @return {@code true} when the resource can be loaned right now */
    public boolean isAvailable() {
        return status == ResourceStatus.AVAILABLE;
    }
}
