package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.enums.EventPosition;
import java.util.List;

/**
 * Acceso a datos de participantes en eventos (tabla {@code participantes_evento}).
 */
public interface EventParticipationDao {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(EventParticipation participation);

    /**
     * Asigna la posición obtenida (1er, 2do, 3er lugar o participante).
     */
    boolean updatePosition(int participationId, EventPosition position);

    /**
     * Participantes de un evento.
     */
    List<EventParticipation> findByEvent(int eventId);

    /**
     * Eventos en los que participó un estudiante.
     */
    List<EventParticipation> findByStudent(int studentId);

    /**
     * Devuelve todos los registros.
     */
    List<EventParticipation> findAll();
}
