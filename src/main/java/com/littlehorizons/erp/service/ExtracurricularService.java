package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.dao.ClubMembershipDao;
import com.littlehorizons.erp.dao.EventParticipationDao;
import com.littlehorizons.erp.dao.SchoolEventDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.dto.ClubOccupancy;
import com.littlehorizons.erp.model.enums.EventPosition;
import com.littlehorizons.erp.model.enums.MembershipStatus;
import com.littlehorizons.erp.model.enums.ParticipationRole;
import com.littlehorizons.erp.model.enums.StudentStatus;
import com.littlehorizons.erp.util.BusinessException;
import com.littlehorizons.erp.util.Validator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Clubs (with capacity control) and school events (with competition positions).
 */
public class ExtracurricularService {

    private final ClubDao clubDao;
    private final ClubMembershipDao membershipDao;
    private final SchoolEventDao eventDao;
    private final EventParticipationDao participationDao;
    private final StudentDao studentDao;

    public ExtracurricularService(ClubDao clubDao, ClubMembershipDao membershipDao, SchoolEventDao eventDao,
                                  EventParticipationDao participationDao, StudentDao studentDao) {
        this.clubDao = clubDao;
        this.membershipDao = membershipDao;
        this.eventDao = eventDao;
        this.participationDao = participationDao;
        this.studentDao = studentDao;
    }

    // ---------- clubs ----------

    public int createClub(Club club) {
        if (!Validator.isNotBlank(club.getName())) {
            throw new BusinessException("El nombre del club es obligatorio.");
        }
        if (club.getMaxCapacity() <= 0) {
            throw new BusinessException("El cupo máximo debe ser mayor que cero.");
        }
        club.setActive(true);
        return clubDao.insert(club);
    }

    public List<Club> findAllClubs() {
        return clubDao.findAll();
    }

    public Club getClub(int clubId) {
        return clubDao.findById(clubId).orElseThrow(() -> new BusinessException("No existe el club con id " + clubId + "."));
    }

    /**
     * Enrolls a student in a club. Rules: active student, active club, not already a member
     * and free capacity.
     */
    public int joinClub(int studentId, int clubId, LocalDate joinDate) {
        Student student = activeStudent(studentId);
        Club club = getClub(clubId);
        if (!club.isActive()) {
            throw new BusinessException("El club " + club.getName() + " no está activo.");
        }
        if (membershipDao.findActive(studentId, clubId).isPresent()) {
            throw new BusinessException(student.getFullName() + " ya es miembro activo de " + club.getName() + ".");
        }
        int members = clubDao.countActiveMembers(clubId);
        if (members >= club.getMaxCapacity()) {
            throw new BusinessException("Cupo lleno: " + club.getName() + " tiene " + members + "/" + club.getMaxCapacity()
                    + " estudiantes.");
        }
        ClubMembership membership = new ClubMembership();
        membership.setStudentId(studentId);
        membership.setClubId(clubId);
        membership.setJoinDate(joinDate);
        membership.setStatus(MembershipStatus.ACTIVE);
        return membershipDao.insert(membership);
    }

    public void leaveClub(int studentId, int clubId, String reason) {
        ClubMembership membership = membershipDao.findActive(studentId, clubId)
                .orElseThrow(() -> new BusinessException("El estudiante no es miembro activo de ese club."));
        membershipDao.withdraw(membership.getId(), LocalDate.now(), reason);
    }

    public List<ClubMembership> getMembers(int clubId) {
        return membershipDao.findByClub(clubId);
    }

    public List<ClubMembership> getStudentClubs(int studentId) {
        return membershipDao.findByStudent(studentId);
    }

    /** Occupancy of every club, fullest first. */
    public List<ClubOccupancy> clubOccupancy() {
        return clubDao.findAll().stream()
                .map(club -> new ClubOccupancy(club.getId(), club.getName(), clubDao.countActiveMembers(club.getId()),
                        club.getMaxCapacity()))
                .sorted(Comparator.comparingDouble(ClubOccupancy::occupancyPercent).reversed())
                .toList();
    }

    // ---------- events ----------

    public int createEvent(SchoolEvent event) {
        if (!Validator.isNotBlank(event.getName()) || event.getDate() == null) {
            throw new BusinessException("El nombre y la fecha del evento son obligatorios.");
        }
        event.setActive(true);
        return eventDao.insert(event);
    }

    public List<SchoolEvent> findAllEvents() {
        return eventDao.findAll();
    }

    public SchoolEvent getEvent(int eventId) {
        return eventDao.findById(eventId).orElseThrow(() -> new BusinessException("No existe el evento con id " + eventId + "."));
    }

    public int registerParticipant(int eventId, int studentId, ParticipationRole role) {
        SchoolEvent event = getEvent(eventId);
        activeStudent(studentId);
        boolean duplicated = participationDao.findByEvent(eventId).stream().anyMatch(p -> p.getStudentId() == studentId);
        if (duplicated) {
            throw new BusinessException("El estudiante ya está registrado en " + event.getName() + ".");
        }
        EventParticipation participation = new EventParticipation();
        participation.setEventId(eventId);
        participation.setStudentId(studentId);
        participation.setRole(role);
        participation.setPosition(event.isCompetitive() ? EventPosition.PARTICIPANT : null);
        return participationDao.insert(participation);
    }

    /** Sets the position; 1st, 2nd and 3rd place can only be given once per event. */
    public void assignPosition(int eventId, int studentId, EventPosition position) {
        SchoolEvent event = getEvent(eventId);
        if (!event.isCompetitive()) {
            throw new BusinessException("El evento " + event.getName() + " no tiene competencia.");
        }
        List<EventParticipation> participants = participationDao.findByEvent(eventId);
        EventParticipation participation = participants.stream()
                .filter(p -> p.getStudentId() == studentId)
                .findFirst()
                .orElseThrow(() -> new BusinessException("El estudiante no participa en este evento."));
        boolean podiumTaken = position != EventPosition.PARTICIPANT && participants.stream()
                .anyMatch(p -> p.getPosition() == position && p.getStudentId() != studentId);
        if (podiumTaken) {
            throw new BusinessException("La posición " + position + " ya fue asignada en este evento.");
        }
        participationDao.updatePosition(participation.getId(), position);
    }

    public List<EventParticipation> getParticipants(int eventId) {
        return participationDao.findByEvent(eventId);
    }

    public List<EventParticipation> findAllParticipations() {
        return participationDao.findAll();
    }

    private Student activeStudent(int studentId) {
        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new BusinessException("No existe un estudiante con id " + studentId + "."));
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException(student.getFullName() + " no está activo.");
        }
        return student;
    }
}
