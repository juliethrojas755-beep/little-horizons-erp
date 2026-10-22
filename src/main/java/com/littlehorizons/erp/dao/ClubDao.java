package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;

/**
 * Acceso a datos de clubes (tabla {@code clubes}).
 */
public interface ClubDao extends CrudDao<Club> {

    /**
     * Cuenta los miembros con estado Activa; se compara con el cupo máximo.
     */
    int countActiveMembers(int clubId);
}
