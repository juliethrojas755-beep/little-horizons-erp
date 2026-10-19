package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.enums.ResourceStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data access for loanable resources ({@code recursos}).
 */
public interface ResourceDao extends CrudDao<Resource> {

    Optional<Resource> findByCode(String code);

    List<Resource> findByStatus(ResourceStatus status);

    /** Changes the status inside a loan or return transaction. */
    void updateStatus(Connection connection, int resourceId, ResourceStatus status) throws SQLException;
}
