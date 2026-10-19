package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.model.dto.ClubOccupancy;
import com.littlehorizons.erp.model.enums.EventPosition;
import com.littlehorizons.erp.model.enums.ParticipationRole;
import com.littlehorizons.erp.service.ExtracurricularService;
import java.time.LocalDate;
import java.util.List;

/**
 * Actions of the clubs and events screens.
 */
public class ExtracurricularController extends BaseController {

    private final ExtracurricularService service;

    public ExtracurricularController(ExtracurricularService service) {
        this.service = service;
    }

    public OperationResult<Integer> createClub(Club club) {
        return execute("Club creado correctamente.", () -> service.createClub(club));
    }

    public OperationResult<List<Club>> findAllClubs() {
        return execute(null, service::findAllClubs);
    }

    public OperationResult<Club> getClub(int clubId) {
        return execute(null, () -> service.getClub(clubId));
    }

    public OperationResult<Integer> joinClub(int studentId, int clubId, LocalDate date) {
        return execute("Estudiante inscrito en el club.", () -> service.joinClub(studentId, clubId, date));
    }

    public OperationResult<Void> leaveClub(int studentId, int clubId, String reason) {
        return run("Retiro del club registrado.", () -> service.leaveClub(studentId, clubId, reason));
    }

    public OperationResult<List<ClubMembership>> getMembers(int clubId) {
        return execute(null, () -> service.getMembers(clubId));
    }

    public OperationResult<List<ClubMembership>> getStudentClubs(int studentId) {
        return execute(null, () -> service.getStudentClubs(studentId));
    }

    public OperationResult<List<ClubOccupancy>> clubOccupancy() {
        return execute(null, service::clubOccupancy);
    }

    public OperationResult<Integer> createEvent(SchoolEvent event) {
        return execute("Evento creado correctamente.", () -> service.createEvent(event));
    }

    public OperationResult<List<SchoolEvent>> findAllEvents() {
        return execute(null, service::findAllEvents);
    }

    public OperationResult<SchoolEvent> getEvent(int eventId) {
        return execute(null, () -> service.getEvent(eventId));
    }

    public OperationResult<Integer> registerParticipant(int eventId, int studentId, ParticipationRole role) {
        return execute("Participante registrado.", () -> service.registerParticipant(eventId, studentId, role));
    }

    public OperationResult<Void> assignPosition(int eventId, int studentId, EventPosition position) {
        return run("Posición asignada: " + position + ".", () -> service.assignPosition(eventId, studentId, position));
    }

    public OperationResult<List<EventParticipation>> getParticipants(int eventId) {
        return execute(null, () -> service.getParticipants(eventId));
    }
}
