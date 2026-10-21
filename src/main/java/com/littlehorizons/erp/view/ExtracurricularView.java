package com.littlehorizons.erp.view;

import com.littlehorizons.erp.AppContext;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.dto.ClubOccupancy;
import com.littlehorizons.erp.model.enums.EventPosition;
import com.littlehorizons.erp.model.enums.EventType;
import com.littlehorizons.erp.model.enums.MembershipStatus;
import com.littlehorizons.erp.model.enums.ParticipationRole;
import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.Formats;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.view.components.ConsoleUI;
import com.littlehorizons.erp.view.components.Menu;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Module 4: clubs and events.
 */
public class ExtracurricularView extends BaseView {

    private static final String MODULE = "Actividades Extracurriculares";

    public ExtracurricularView(AppContext app, InputReader input) {
        super(app, input);
    }

    @Override
    public void show() {
        new Menu(MODULE, input)
                .option("☰", "Catálogo de clubes y cupos", this::clubCatalog)
                .option("✚", "Crear club", this::createClub)
                .option("✎", "Inscribir estudiante en un club", this::joinClub)
                .option("✖", "Retirar estudiante de un club", this::leaveClub)
                .option("☰", "Miembros de un club", this::clubMembers)
                .separator()
                .option("☰", "Eventos del colegio", this::eventList)
                .option("✚", "Crear evento", this::createEvent)
                .option("✎", "Registrar participante en un evento", this::registerParticipant)
                .option("★", "Asignar posición (1er, 2do, 3er lugar)", this::assignPosition)
                .option("▤", "Participantes y podio de un evento", this::eventResults)
                .show();
    }

    private void clubCatalog() {
        Map<Integer, Club> clubs = clubsById();
        BaseView.<ClubOccupancy>table()
                .column("Club", ClubOccupancy::clubName)
                .column("Horario", o -> clubs.get(o.clubId()).getMeetingSchedule())
                .column("Ocupación", o -> ConsoleUI.bar(o.occupancyPercent() / 100, 16,
                        o.isFull() ? Ansi.RED : o.occupancyPercent() >= 80 ? Ansi.YELLOW : Ansi.GREEN))
                .numberColumn("Cupo", o -> o.activeMembers() + "/" + o.maxCapacity())
                .column("Estado", o -> o.isFull() ? Ansi.RED.apply("LLENO") : Ansi.GREEN.apply("Disponible"))
                .print(require(app.extracurricular().clubOccupancy()));
    }

    private void createClub() {
        Club club = new Club();
        club.setName(input.readText("Nombre del club"));
        club.setDescription(input.readOptionalText("Descripción"));
        club.setMaxCapacity(input.readInt("Cupo máximo", 1, 200));
        club.setMeetingSchedule(input.readText("Horario de reunión (ej: Martes 3:00-5:00 PM)"));
        ConsoleUI.info("Profesor encargado:");
        Teacher supervisor = selectTeacher();
        club.setSupervisorTeacherId(supervisor.getId());
        ConsoleUI.show(app.extracurricular().createClub(club));
    }

    private void joinClub() {
        Student student = selectStudent();
        Club club = chooseClub();
        LocalDate date = input.readDateOrToday("Fecha de ingreso");
        ConsoleUI.show(app.extracurricular().joinClub(student.getId(), club.getId(), date));
    }

    private void leaveClub() {
        Student student = selectStudent();
        Map<Integer, Club> clubs = clubsById();
        List<ClubMembership> active = require(app.extracurricular().getStudentClubs(student.getId())).stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .toList();
        if (active.isEmpty()) {
            ConsoleUI.info(student.getFullName() + " no pertenece a ningún club.");
            return;
        }
        ClubMembership membership = input.readChoice("Club", active, m -> clubs.get(m.getClubId()).getName());
        String reason = input.readText("Motivo del retiro");
        ConsoleUI.show(app.extracurricular().leaveClub(student.getId(), membership.getClubId(), reason));
    }

    private void clubMembers() {
        Club club = chooseClub();
        Map<Integer, String> names = studentNames();
        List<ClubMembership> members = require(app.extracurricular().getMembers(club.getId()));
        ConsoleUI.info(club.getName() + " · " + Optional.ofNullable(club.getDescription()).orElse(""));
        BaseView.<ClubMembership>table()
                .column("Estudiante", m -> names.get(m.getStudentId()))
                .column("Ingreso", m -> Formats.date(m.getJoinDate()))
                .column("Estado", m -> ConsoleUI.status(m.getStatus().getDbValue()))
                .column("Retiro", m -> Formats.date(m.getWithdrawalDate()))
                .print(members);
    }

    private void eventList() {
        BaseView.<SchoolEvent>table()
                .numberColumn("Id", e -> String.valueOf(e.getId()))
                .column("Fecha", e -> Formats.date(e.getDate()))
                .column("Evento", SchoolEvent::getName)
                .column("Tipo", e -> e.getType().getDbValue())
                .column("Lugar", SchoolEvent::getPlace)
                .column("Competencia", e -> e.isCompetitive() ? Ansi.YELLOW.apply("Sí") : "No")
                .print(require(app.extracurricular().findAllEvents()));
    }

    private void createEvent() {
        SchoolEvent event = new SchoolEvent();
        event.setName(input.readText("Nombre del evento"));
        event.setDescription(input.readOptionalText("Descripción"));
        event.setType(chooseEnum("Tipo", EventType.class));
        event.setDate(input.readDate("Fecha"));
        event.setPlace(input.readText("Lugar"));
        event.setCompetitive(input.readYesNo("¿Tiene competencia con posiciones?"));
        if (input.readYesNo("¿Desea indicar el profesor organizador?")) {
            event.setOrganizerTeacherId(selectTeacher().getId());
        }
        ConsoleUI.show(app.extracurricular().createEvent(event));
    }

    private void registerParticipant() {
        SchoolEvent event = chooseEvent();
        Student student = selectStudent();
        ParticipationRole role = chooseEnum("Rol", ParticipationRole.class);
        ConsoleUI.show(app.extracurricular().registerParticipant(event.getId(), student.getId(), role));
    }

    private void assignPosition() {
        SchoolEvent event = chooseEvent();
        Map<Integer, String> names = studentNames();
        List<EventParticipation> participants = require(app.extracurricular().getParticipants(event.getId()));
        if (participants.isEmpty()) {
            ConsoleUI.info("El evento no tiene participantes.");
            return;
        }
        EventParticipation participation = input.readChoice("Participante", participants, p -> names.get(p.getStudentId()));
        EventPosition position = chooseEnum("Posición", EventPosition.class);
        ConsoleUI.show(app.extracurricular().assignPosition(event.getId(), participation.getStudentId(), position));
    }

    private void eventResults() {
        SchoolEvent event = chooseEvent();
        Map<Integer, String> names = studentNames();
        List<EventParticipation> participants = require(app.extracurricular().getParticipants(event.getId()));
        ConsoleUI.card(event.getName(), List.of(
                new String[]{"Fecha", Formats.date(event.getDate())},
                new String[]{"Tipo", event.getType().getDbValue()},
                new String[]{"Lugar", event.getPlace()},
                new String[]{"Participantes", String.valueOf(participants.size())}));
        if (event.isCompetitive()) {
            ConsoleUI.section("Podio");
            participants.stream()
                    .filter(p -> p.getPosition() != null && p.getPosition() != EventPosition.PARTICIPANT)
                    .sorted(Comparator.comparing(EventParticipation::getPosition))
                    .forEach(p -> System.out.println("   " + medal(p.getPosition()) + "  " + Ansi.BOLD
                            + names.get(p.getStudentId()) + Ansi.RESET));
        }
        ConsoleUI.section("Todos los participantes");
        BaseView.<EventParticipation>table()
                .column("Estudiante", p -> names.get(p.getStudentId()))
                .column("Rol", p -> p.getRole().getDbValue())
                .column("Posición", p -> p.getPosition() == null ? "-" : p.getPosition().getDbValue())
                .print(participants);
    }

    // ---------- helpers ----------

    private String medal(EventPosition position) {
        return switch (position) {
            case FIRST_PLACE -> Ansi.BRIGHT_YELLOW.apply(ConsoleUI.symbol("🥇 ", "") + "1er Lugar");
            case SECOND_PLACE -> Ansi.WHITE.apply(ConsoleUI.symbol("🥈 ", "") + "2do Lugar");
            case THIRD_PLACE -> Ansi.YELLOW.apply(ConsoleUI.symbol("🥉 ", "") + "3er Lugar");
            case PARTICIPANT -> "Participante";
        };
    }

    private Club chooseClub() {
        return input.readChoice("Club", require(app.extracurricular().findAllClubs()), Club::getName);
    }

    private SchoolEvent chooseEvent() {
        return input.readChoice("Evento", require(app.extracurricular().findAllEvents()),
                e -> e.getName() + " (" + Formats.date(e.getDate()) + ")");
    }

    private Map<Integer, Club> clubsById() {
        return require(app.extracurricular().findAllClubs()).stream()
                .collect(Collectors.toMap(Club::getId, Function.identity()));
    }

    private Map<Integer, String> studentNames() {
        return require(app.students().findAll()).stream()
                .collect(Collectors.toMap(Student::getId, Student::getFullName));
    }
}
