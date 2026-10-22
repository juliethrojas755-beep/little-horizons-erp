package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/**
 * Ítem del catálogo que se puede cobrar (matrícula, pensión, uniforme...).
 * <p>Tabla: {@code conceptos_pago}</p>
 */
public class PaymentConcept {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla conceptos_pago) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Código único del concepto, por ejemplo PENSION
    private String code;
    // Nombre del concepto de pago
    private String name;
    // Descripción (texto libre)
    private String description;
    // Valor base en pesos colombianos
    private BigDecimal baseAmount;
    // true si se cobra todos los meses (pensión, transporte...)
    private boolean recurring;
    // true si sigue activo; false si fue desactivado
    private boolean active;

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

    /** Devuelve la descripción. */
    public String getDescription() {
        return description;
    }

    /** Asigna la descripción. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Devuelve el valor base. */
    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    /** Asigna el valor base. */
    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    /** Devuelve si es recurrente. */
    public boolean isRecurring() {
        return recurring;
    }

    /** Asigna si es recurrente. */
    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    /** Devuelve si está activo. */
    public boolean isActive() {
        return active;
    }

    /** Asigna si está activo. */
    public void setActive(boolean active) {
        this.active = active;
    }
}
