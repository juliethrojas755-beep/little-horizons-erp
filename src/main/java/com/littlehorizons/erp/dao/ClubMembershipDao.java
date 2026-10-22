package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ClubMembership;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de membresías en clubes (tabla {@code membresias_club}).
 */
public interface ClubMembershipDao {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(ClubMembership membership);

    /**
     * Marca la membresía como Retirada con su fecha y motivo.
     */
    boolean withdraw(int membershipId, LocalDate date, String reason);

    /**
     * Membresías de un club.
     */
    List<ClubMembership> findByClub(int clubId);

    /**
     * Membresías de un estudiante.
     */
    List<ClubMembership> findByStudent(int studentId);

    /**
     * Membresía ACTIVA de un estudiante en un club (para no inscribirlo dos veces).
     */
    Optional<ClubMembership> findActive(int studentId, int clubId);

    /**
     * Devuelve todos los registros.
     */
    List<ClubMembership> findAll();
}
