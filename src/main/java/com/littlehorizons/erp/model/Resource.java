package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.ResourceStatus;
import java.time.LocalDate;

/**
 * Recurso prestable del colegio (biblioteca y laboratorios).
 * <p>Tabla: {@code recursos}</p>
 */
public class Resource {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla recursos) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Código único de inventario, por ejemplo LIB-001
    private String code;
    // Nombre del recurso
    private String name;
    // Id de la categoría (llave foránea hacia categorias_recurso)
    private int categoryId;
    // Descripción (texto libre)
    private String description;
    // Fecha en que el colegio adquirió el recurso
    private LocalDate acquisitionDate;
    // Ubicación física (Biblioteca, Laboratorio 1...)
    private String location;
    // Estado del recurso: Disponible, Prestado, En Mantenimiento o Dado de Baja
    private ResourceStatus status;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el identificador único. */
    public int getId() {
        return id;
    }

    /** Asigna el identificador único. */
    public void setId(int id) {
        this.id = id;
    }

    /** Devuelve el código. */
    public String getCode() {
        return code;
    }

    /** Asigna el código. */
    public void setCode(String code) {
        this.code = code;
    }

    /** Devuelve el nombre. */
    public String getName() {
        return name;
    }

    /** Asigna el nombre. */
    public void setName(String name) {
        this.name = name;
    }

    /** Devuelve el id de la categoría. */
    public int getCategoryId() {
        return categoryId;
    }

    /** Asigna el id de la categoría. */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /** Devuelve la descripción. */
    public String getDescription() {
        return description;
    }

    /** Asigna la descripción. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Devuelve la fecha de adquisición. */
    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    /** Asigna la fecha de adquisición. */
    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    /** Devuelve la ubicación. */
    public String getLocation() {
        return location;
    }

    /** Asigna la ubicación. */
    public void setLocation(String location) {
        this.location = location;
    }

    /** Devuelve el estado. */
    public ResourceStatus getStatus() {
        return status;
    }

    /** Asigna el estado. */
    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    // ---------- Regla de negocio de la entidad ----------

    /** @return {@code true} cuando el recurso se puede prestar en este momento */
    public boolean isAvailable() {
        return status == ResourceStatus.AVAILABLE;
    }
}
