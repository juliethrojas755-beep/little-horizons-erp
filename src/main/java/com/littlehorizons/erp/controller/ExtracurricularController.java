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
 * Acciones de las pantallas de clubes y eventos.
 * <p>Un controlador NO tiene reglas de negocio ni SQL: llama al servicio y envuelve el resultado en un
 * {@link OperationResult} para que la vista nunca reciba excepciones.</p>
 */
public class ExtracurricularController extends BaseController {

    // Servicio al que se delegan las reglas de negocio
    private final ExtracurricularService service;

    // El servicio llega por el constructor (inyección de dependencias): lo entrega AppContext
    public ExtracurricularController(ExtracurricularService service) {
        this.service = service;
    }

    /** Crea un club. */
    public OperationResult<Integer> createClub(Club club) {
        // execute(mensaje, acción): ejecuta la acción y convierte errores en un resultado fallido; null = sin mensaje de éxito
        return execute("Club creado correctamente.", () -> service.createClub(club));
    }

    /** Todos los clubes. */
    public OperationResult<List<Club>> findAllClubs() {
        return execute(null, service::findAllClubs);
    }

    /** Busca un club por id. */
    public OperationResult<Club> getClub(int clubId) {
        return execute(null, () -> service.getClub(clubId));
    }

    /** Inscribe a un estudiante en un club (valida el cupo). */
    public OperationResult<Integer> joinClub(int studentId, int clubId, LocalDate date) {
        return execute("Estudiante inscrito en el club.", () -> service.joinClub(studentId, clubId, date));
    }

    /** Retira a un estudiante de un club. */
    public OperationResult<Void> leaveClub(int studentId, int clubId, String reason) {
        return run("Retiro del club registrado.", () -> service.leaveClub(studentId, clubId, reason));
    }

    /** Miembros de un club. */
    public OperationResult<List<ClubMembership>> getMembers(int clubId) {
        return execute(null, () -> service.getMembers(clubId));
    }

    /** Clubes de un estudiante. */
    public OperationResult<List<ClubMembership>> getStudentClubs(int studentId) {
        return execute(null, () -> service.getStudentClubs(studentId));
    }

    /** Ocupación (miembros y cupo) de cada club. */
    public OperationResult<List<ClubOccupancy>> clubOccupancy() {
        return execute(null, service::clubOccupancy);
    }

    /** Crea un evento. */
    public OperationResult<Integer> createEvent(SchoolEvent event) {
        return execute("Evento creado correctamente.", () -> service.createEvent(event));
    }

    /** Todos los eventos. */
    public OperationResult<List<SchoolEvent>> findAllEvents() {
        return execute(null, service::findAllEvents);
    }

    /** Busca un evento por id. */
    public OperationResult<SchoolEvent> getEvent(int eventId) {
        return execute(null, () -> service.getEvent(eventId));
    }

    /** Registra a un estudiante como participante de un evento. */
    public OperationResult<Integer> registerParticipant(int eventId, int studentId, ParticipationRole role) {
        return execute("Participante registrado.", () -> service.registerParticipant(eventId, studentId, role));
    }

    /** Asigna el lugar obtenido en un evento. */
    public OperationResult<Void> assignPosition(int eventId, int studentId, EventPosition position) {
        return run("Posición asignada: " + position + ".", () -> service.assignPosition(eventId, studentId, position));
    }

    /** Participantes de un evento. */
    public OperationResult<List<EventParticipation>> getParticipants(int eventId) {
        return execute(null, () -> service.getParticipants(eventId));
    }
}
