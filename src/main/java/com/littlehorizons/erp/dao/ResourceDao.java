package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.enums.ResourceStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de recursos prestables (tabla {@code recursos}).
 */
public interface ResourceDao extends CrudDao<Resource> {

    /**
     * Busca un recurso por su código de inventario.
     */
    Optional<Resource> findByCode(String code);

    /**
     * Recursos en un estado (por ejemplo, los Disponibles).
     */
    List<Resource> findByStatus(ResourceStatus status);

    /**
     * Cambia el estado dentro de una transacción de préstamo o devolución.
     */
    void updateStatus(Connection connection, int resourceId, ResourceStatus status) throws SQLException;
}
