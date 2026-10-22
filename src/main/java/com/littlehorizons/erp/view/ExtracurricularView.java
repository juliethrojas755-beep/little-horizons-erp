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
 * Módulo 4: clubes y eventos.
 * <p>Solo pide datos, llama al controlador y dibuja el resultado; el control de cupos y el podio están en el servicio.</p>
 */
public class ExtracurricularView extends BaseView {

    // Título que aparece en el encabezado del menú
    private static final String MODULE = "Actividades Extracurriculares";

    public ExtracurricularView(AppContext app, InputReader input) {
        super(app, input);
    }

    /**
     * Arma el menú del módulo: cada opción es un texto y un método (referencia con ::).
     */
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

    /**
     * Catálogo de clubes con su horario, barra de ocupación y cupo (rojo cuando está lleno).
     */
    private void clubCatalog() {
        // Tabla para consultar el horario de cada club a partir de su id
        Map<Integer, Club> clubs = clubsById();
        // La ocupación se muestra con una barra: verde si hay cupo, amarillo desde el 80 % y rojo cuando está lleno
        BaseView.<ClubOccupancy>table()
                .column("Club", ClubOccupancy::clubName)
                .column("Horario", o -> clubs.get(o.clubId()).getMeetingSchedule())
                .column("Ocupación", o -> ConsoleUI.bar(o.occupancyPercent() / 100, 16,
                        o.isFull() ? Ansi.RED : o.occupancyPercent() >= 80 ? Ansi.YELLOW : Ansi.GREEN))
                .numberColumn("Cupo", o -> o.activeMembers() + "/" + o.maxCapacity())
                .column("Estado", o -> o.isFull() ? Ansi.RED.apply("LLENO") : Ansi.GREEN.apply("Disponible"))
                .print(require(app.extracurricular().clubOccupancy()));
    }

    /**
     * Pide los datos de un club nuevo y lo crea.
     */
    private void createClub() {
        Club club = new Club();
        club.setName(input.readText("Nombre del club"));
        club.setDescription(input.readOptionalText("Descripción"));
        // El cupo debe ser un entero entre 1 y 200
        club.setMaxCapacity(input.readInt("Cupo máximo", 1, 200));
        club.setMeetingSchedule(input.readText("Horario de reunión (ej: Martes 3:00-5:00 PM)"));
        // Todo club tiene un profesor encargado
        ConsoleUI.info("Profesor encargado:");
        Teacher supervisor = selectTeacher();
        club.setSupervisorTeacherId(supervisor.getId());
        ConsoleUI.show(app.extracurricular().createClub(club));
    }

    /**
     * Inscribe a un estudiante en un club; el servicio valida el cupo.
     */
    private void joinClub() {
        Student student = selectStudent();
        Club club = chooseClub();
        // Si no escribe fecha, el ingreso es hoy
        LocalDate date = input.readDateOrToday("Fecha de ingreso");
        ConsoleUI.show(app.extracurricular().joinClub(student.getId(), club.getId(), date));
    }

    /**
     * Retira a un estudiante de uno de sus clubes activos.
     */
    private void leaveClub() {
        Student student = selectStudent();
        Map<Integer, Club> clubs = clubsById();
        // Solo las membresías activas se pueden retirar (filter con una lambda)
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

    /**
     * Muestra los miembros de un club.
     */
    private void clubMembers() {
        Club club = chooseClub();
        Map<Integer, String> names = studentNames();
        // Miembros del club, activos y retirados
        List<ClubMembership> members = require(app.extracurricular().getMembers(club.getId()));
        ConsoleUI.info(club.getName() + " · " + Optional.ofNullable(club.getDescription()).orElse(""));
        BaseView.<ClubMembership>table()
                .column("Estudiante", m -> names.get(m.getStudentId()))
                .column("Ingreso", m -> Formats.date(m.getJoinDate()))
                .column("Estado", m -> ConsoleUI.status(m.getStatus().getDbValue()))
                .column("Retiro", m -> Formats.date(m.getWithdrawalDate()))
                .print(members);
    }

    /**
     * Lista todos los eventos del colegio.
     */
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

    /**
     * Pide los datos de un evento nuevo y lo crea.
     */
    private void createEvent() {
        SchoolEvent event = new SchoolEvent();
        event.setName(input.readText("Nombre del evento"));
        event.setDescription(input.readOptionalText("Descripción"));
        event.setType(chooseEnum("Tipo", EventType.class));
        event.setDate(input.readDate("Fecha"));
        event.setPlace(input.readText("Lugar"));
        // Solo los eventos con competencia tienen posiciones
        event.setCompetitive(input.readYesNo("¿Tiene competencia con posiciones?"));
        // El organizador es opcional
        if (input.readYesNo("¿Desea indicar el profesor organizador?")) {
            event.setOrganizerTeacherId(selectTeacher().getId());
        }
        ConsoleUI.show(app.extracurricular().createEvent(event));
    }

    /**
     * Registra a un estudiante como participante de un evento.
     */
    private void registerParticipant() {
        SchoolEvent event = chooseEvent();
        Student student = selectStudent();
        ParticipationRole role = chooseEnum("Rol", ParticipationRole.class);
        ConsoleUI.show(app.extracurricular().registerParticipant(event.getId(), student.getId(), role));
    }

    /**
     * Asigna el lugar (1°, 2°, 3° o participante) a un participante de un evento con competencia.
     */
    private void assignPosition() {
        SchoolEvent event = chooseEvent();
        Map<Integer, String> names = studentNames();
        List<EventParticipation> participants = require(app.extracurricular().getParticipants(event.getId()));
        if (participants.isEmpty()) {
            ConsoleUI.info("El evento no tiene participantes.");
            return;
        }
        // Se escoge a qué participante se le asigna el lugar
        EventParticipation participation = input.readChoice("Participante", participants, p -> names.get(p.getStudentId()));
        EventPosition position = chooseEnum("Posición", EventPosition.class);
        ConsoleUI.show(app.extracurricular().assignPosition(event.getId(), participation.getStudentId(), position));
    }

    /**
     * Muestra el evento, su podio con medallas y la lista de todos los participantes.
     */
    private void eventResults() {
        SchoolEvent event = chooseEvent();
        Map<Integer, String> names = studentNames();
        List<EventParticipation> participants = require(app.extracurricular().getParticipants(event.getId()));
        ConsoleUI.card(event.getName(), List.of(
                new String[]{"Fecha", Formats.date(event.getDate())},
                new String[]{"Tipo", event.getType().getDbValue()},
                new String[]{"Lugar", event.getPlace()},
                new String[]{"Participantes", String.valueOf(participants.size())}));
        // El podio solo se muestra en eventos con competencia
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

    // ====================== AYUDAS ======================

    /**
     * Texto con la medalla que corresponde a cada posición.
     */
    private String medal(EventPosition position) {
        // switch con flechas: cada posición devuelve su medalla
        return switch (position) {
            case FIRST_PLACE -> Ansi.BRIGHT_YELLOW.apply(ConsoleUI.symbol("🥇 ", "") + "1er Lugar");
            case SECOND_PLACE -> Ansi.WHITE.apply(ConsoleUI.symbol("🥈 ", "") + "2do Lugar");
            case THIRD_PLACE -> Ansi.YELLOW.apply(ConsoleUI.symbol("🥉 ", "") + "3er Lugar");
            case PARTICIPANT -> "Participante";
        };
    }

    /**
     * Deja escoger un club de la lista.
     */
    private Club chooseClub() {
        return input.readChoice("Club", require(app.extracurricular().findAllClubs()), Club::getName);
    }

    /**
     * Deja escoger un evento de la lista.
     */
    private SchoolEvent chooseEvent() {
        return input.readChoice("Evento", require(app.extracurricular().findAllEvents()),
                e -> e.getName() + " (" + Formats.date(e.getDate()) + ")");
    }

    /**
     * Tabla id -> club.
     */
    private Map<Integer, Club> clubsById() {
        return require(app.extracurricular().findAllClubs()).stream()
                .collect(Collectors.toMap(Club::getId, Function.identity()));
    }

    /**
     * Tabla id de estudiante -> nombre completo.
     */
    private Map<Integer, String> studentNames() {
        return require(app.students().findAll()).stream()
                .collect(Collectors.toMap(Student::getId, Student::getFullName));
    }
}
