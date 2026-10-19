package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.enums.EventPosition;
import java.util.List;

/**
 * Data access for event participants ({@code participantes_evento}).
 */
public interface EventParticipationDao {

    int insert(EventParticipation participation);

    boolean updatePosition(int participationId, EventPosition position);

    List<EventParticipation> findByEvent(int eventId);

    List<EventParticipation> findByStudent(int studentId);

    List<EventParticipation> findAll();
}
