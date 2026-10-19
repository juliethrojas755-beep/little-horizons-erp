package com.littlehorizons.erp.view;

import com.littlehorizons.erp.AppContext;
import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.model.dto.OverdueLoan;
import com.littlehorizons.erp.model.enums.ResourceStatus;
import com.littlehorizons.erp.service.ResourceService;
import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.Formats;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.view.components.ConsoleUI;
import com.littlehorizons.erp.view.components.Menu;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Module 6: library and laboratory resources and their loans.
 */
public class ResourceView extends BaseView {

    private static final String MODULE = "Gestión de Recursos";

    public ResourceView(AppContext app, InputReader input) {
        super(app, input);
    }

    @Override
    public void show() {
        new Menu(MODULE, input)
                .option("☰", "Catálogo de recursos", this::catalog)
                .option("⌕", "Recursos por estado", this::byStatus)
                .option("✚", "Registrar recurso", this::register)
                .option("✎", "Enviar a mantenimiento / dar de baja / habilitar", this::changeStatus)
                .separator()
                .option("⇄", "Prestar recurso a un estudiante", this::loanToStudent)
                .option("⇄", "Prestar recurso a un profesor", this::loanToTeacher)
                .option("↺", "Registrar devolución", this::returnLoan)
                .separator()
                .option("☰", "Préstamos abiertos", this::openLoans)
                .option("⚠", "Préstamos vencidos", this::overdue)
                .show();
    }

    private void catalog() {
        printResources(require(app.resources().findAll()));
    }

    private void byStatus() {
        printResources(require(app.resources().findByStatus(chooseEnum("Estado", ResourceStatus.class))));
    }

    private void register() {
        Resource resource = new Resource();
        resource.setCode(input.readText("Código de inventario (ej: LIB-050)").toUpperCase());
        resource.setName(input.readText("Nombre"));
        resource.setCategoryId(input.readChoice("Categoría", require(app.resources().getCategories()), ResourceCategory::getName).getId());
        resource.setDescription(input.readOptionalText("Descripción"));
        resource.setAcquisitionDate(input.readDateOrToday("Fecha de adquisición"));
        resource.setLocation(input.readText("Ubicación (ej: Biblioteca, Laboratorio 1)"));
        ConsoleUI.show(app.resources().registerResource(resource));
    }

    private void changeStatus() {
        String code = input.readText("Código del recurso").toUpperCase();
        ResourceStatus status = input.readChoice("Nuevo estado",
                List.of(ResourceStatus.AVAILABLE, ResourceStatus.IN_MAINTENANCE, ResourceStatus.DECOMMISSIONED),
                ResourceStatus::getDbValue);
        ConsoleUI.show(app.resources().changeStatus(code, status));
    }

    private void loanToStudent() {
        Resource resource = chooseAvailable();
        Student student = selectStudent();
        int days = input.readInt("Días de préstamo (1 a " + ResourceService.MAX_LOAN_DAYS + ")", 1, ResourceService.MAX_LOAN_DAYS);
        if (ConsoleUI.show(app.resources().loanToStudent(resource.getCode(), student.getId(), days))) {
            ConsoleUI.info("Debe devolverse el " + Ansi.BOLD + Formats.date(LocalDate.now().plusDays(days)) + Ansi.RESET + ".");
        }
    }

    private void loanToTeacher() {
        Resource resource = chooseAvailable();
        Teacher teacher = selectTeacher();
        int days = input.readInt("Días de préstamo (1 a " + ResourceService.MAX_LOAN_DAYS + ")", 1, ResourceService.MAX_LOAN_DAYS);
        if (ConsoleUI.show(app.resources().loanToTeacher(resource.getCode(), teacher.getId(), days))) {
            ConsoleUI.info("Debe devolverse el " + Ansi.BOLD + Formats.date(LocalDate.now().plusDays(days)) + Ansi.RESET + ".");
        }
    }

    private void returnLoan() {
        boolean student = input.readChoice("¿Quién devuelve?", List.of("Estudiante", "Profesor"), s -> s).equals("Estudiante");
        Map<Integer, Resource> resources = resourcesById();
        if (student) {
            Map<Integer, String> names = studentNames();
            List<StudentLoan> open = require(app.resources().openStudentLoans());
            StudentLoan loan = input.readChoice("Préstamo", open, l -> describe(l, resources, names::get));
            ConsoleUI.show(app.resources().returnStudentLoan(loan.getId()));
        } else {
            Map<Integer, String> names = teacherNames();
            List<TeacherLoan> open = require(app.resources().openTeacherLoans());
            TeacherLoan loan = input.readChoice("Préstamo", open, l -> describe(l, resources, names::get));
            ConsoleUI.show(app.resources().returnTeacherLoan(loan.getId()));
        }
    }

    private void openLoans() {
        Map<Integer, Resource> resources = resourcesById();
        ConsoleUI.section("Estudiantes");
        printLoans(require(app.resources().openStudentLoans()), resources, studentNames()::get);
        ConsoleUI.section("Profesores");
        printLoans(require(app.resources().openTeacherLoans()), resources, teacherNames()::get);
    }

    private void overdue() {
        BaseView.<OverdueLoan>table()
                .column("Tipo", OverdueLoan::borrowerType)
                .column("Persona", OverdueLoan::borrowerName)
                .column("Código", OverdueLoan::resourceCode)
                .column("Recurso", OverdueLoan::resourceName)
                .column("Debía devolver", l -> Formats.date(l.dueDate()))
                .numberColumn("Días de retraso", l -> Ansi.RED.apply(String.valueOf(l.daysOverdue())))
                .print(require(app.resources().overdueLoans()));
    }

    // ---------- helpers ----------

    private Resource chooseAvailable() {
        List<Resource> available = require(app.resources().findByStatus(ResourceStatus.AVAILABLE));
        String code = input.readText("Código del recurso (ej: " +
                (available.isEmpty() ? "LIB-001" : available.get(0).getCode()) + ")").toUpperCase();
        return available.stream().filter(r -> r.getCode().equals(code)).findFirst().orElseGet(() -> {
            ConsoleUI.warning("Ese código no está disponible. Recursos disponibles:");
            printResources(available);
            return chooseAvailable();
        });
    }

    private <T extends Loan> void printLoans(List<T> loans, Map<Integer, Resource> resources, IntFunction<String> names) {
        LocalDate today = LocalDate.now();
        BaseView.<T>table()
                .numberColumn("Id", l -> String.valueOf(l.getId()))
                .column("Persona", l -> names.apply(l.getBorrowerId()))
                .column("Recurso", l -> resources.get(l.getResourceId()).getName())
                .column("Prestado", l -> Formats.date(l.getLoanDate()))
                .column("Devolver", l -> Formats.date(l.getDueDate()))
                .column("Estado", l -> ConsoleUI.status(l.isOverdue(today) ? "Vencido" : l.getStatus().getDbValue()))
                .print(loans);
    }

    private String describe(Loan loan, Map<Integer, Resource> resources, IntFunction<String> names) {
        Resource resource = resources.get(loan.getResourceId());
        return resource.getCode() + " " + resource.getName() + " · " + names.apply(loan.getBorrowerId())
                + " · vence " + Formats.date(loan.getDueDate());
    }

    private void printResources(List<Resource> resources) {
        Map<Integer, ResourceCategory> categories = require(app.resources().categoriesById());
        BaseView.<Resource>table()
                .column("Código", Resource::getCode)
                .column("Nombre", Resource::getName)
                .column("Categoría", r -> categories.get(r.getCategoryId()).getName())
                .column("Ubicación", Resource::getLocation)
                .column("Estado", r -> ConsoleUI.status(r.getStatus().getDbValue()))
                .print(resources);
    }

    private Map<Integer, Resource> resourcesById() {
        return require(app.resources().findAll()).stream().collect(Collectors.toMap(Resource::getId, r -> r));
    }

    private Map<Integer, String> studentNames() {
        return require(app.students().findAll()).stream().collect(Collectors.toMap(Student::getId, Student::getFullName));
    }

    private Map<Integer, String> teacherNames() {
        return require(app.teachers().findAll()).stream().collect(Collectors.toMap(Teacher::getId, Teacher::getFullName));
    }
}
