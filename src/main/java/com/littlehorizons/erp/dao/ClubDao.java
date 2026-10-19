package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;

/**
 * Data access for clubs ({@code clubes}).
 */
public interface ClubDao extends CrudDao<Club> {

    /** @return number of members with status Activa */
    int countActiveMembers(int clubId);
}
