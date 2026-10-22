package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.MembershipStatus;
import java.time.LocalDate;

/**
 * Membresía de un estudiante en un club.
 * <p>Tabla: {@code membresias_club}</p>
 */
public class ClubMembership {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla membresias_club) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Id del club (llave foránea hacia clubes)
    private int clubId;
    // Fecha en que entró al club
    private LocalDate joinDate;
    // Estado de la membresía: Activa o Retirada
    private MembershipStatus status;
    // Fecha de retiro (null mientras siga activo)
    private LocalDate withdrawalDate;
    // Motivo del retiro (opcional)
    private String withdrawalReason;

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

    /** Devuelve el id del estudiante. */
    public int getStudentId() {
        return studentId;
    }

    /** Asigna el id del estudiante. */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /** Devuelve el id del club. */
    public int getClubId() {
        return clubId;
    }

    /** Asigna el id del club. */
    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    /** Devuelve la fecha de ingreso al club. */
    public LocalDate getJoinDate() {
        return joinDate;
    }

    /** Asigna la fecha de ingreso al club. */
    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    /** Devuelve el estado. */
    public MembershipStatus getStatus() {
        return status;
    }

    /** Asigna el estado. */
    public void setStatus(MembershipStatus status) {
        this.status = status;
    }

    /** Devuelve la fecha de retiro. */
    public LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    /** Asigna la fecha de retiro. */
    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    /** Devuelve el motivo del retiro. */
    public String getWithdrawalReason() {
        return withdrawalReason;
    }

    /** Asigna el motivo del retiro. */
    public void setWithdrawalReason(String withdrawalReason) {
        this.withdrawalReason = withdrawalReason;
    }
}
