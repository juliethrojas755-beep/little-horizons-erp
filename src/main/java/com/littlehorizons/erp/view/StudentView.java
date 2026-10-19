package com.littlehorizons.erp.view;

import com.littlehorizons.erp.AppContext;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.enums.DocumentType;
import com.littlehorizons.erp.model.enums.Gender;
import com.littlehorizons.erp.model.enums.StudentStatus;
import com.littlehorizons.erp.util.Formats;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.OperationCancelledException;
import com.littlehorizons.erp.util.Validator;
import com.littlehorizons.erp.view.components.ConsoleUI;
import com.littlehorizons.erp.view.components.Menu;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Module 1: students, guardians and enrollments.
 */
public class StudentView extends BaseView {

    private static final String MODULE = "Gestión de Estudiantes";

    public StudentView(AppContext app, InputReader input) {
        super(app, input);
    }

    @Override
    public void show() {
        new Menu(MODULE, input)
                .option("✚", "Registrar estudiante", this::registerStudent)
                .option("✚", "Registrar acudiente", this::registerGuardian)
                .option("⇄", "Vincular otro acudiente a un estudiante", this::linkGuardian)
                .separator()
                .option("⌕", "Buscar estudiante (ficha completa)", this::studentProfile)
                .option("☰", "Listar estudiantes por grado", this::listByGrade)
                .option("☰", "Listar todos los estudiantes", this::listAll)
                .option("⌕", "Estudiantes de un acudiente (hermanos)", this::studentsOfGuardian)
                .separator()
                .option("✎", "Matricular estudiante en un grado", this::enroll)
                .option("✎", "Cambiar estado (graduado, retirado, suspendido)", this::changeStatus)
                .show();
    }

    private void registerStudent() {
        ConsoleUI.section("1. Acudiente responsable");
        ConsoleUI.info("Todo estudiante debe tener al menos un acudiente.");
        Guardian guardian = input.readYesNo("¿El acudiente ya está registrado?") ? selectGuardian() : createGuardian();

        ConsoleUI.section("2. Datos del estudiante");
        Student student = new Student();
        student.setDocumentType(chooseEnum("Tipo de documento", DocumentType.class));
        student.setDocumentNumber(input.readValidated("Número de documento", Validator::isValidDocument,
                "Use de 5 a 20 letras o números, sin espacios ni puntos."));
        student.setFirstName(readName("Primer nombre"));
        student.setMiddleName(input.readOptionalText("Segundo nombre"));
        student.setLastName(readName("Primer apellido"));
        student.setSecondLastName(input.readOptionalText("Segundo apellido"));
        student.setBirthDate(input.readDate("Fecha de nacimiento"));
        student.setGender(chooseEnum("Género", Gender.class));
        student.setAddress(input.readText("Dirección"));
        student.setPhone(input.readOptionalText("Teléfono"));
        student.setEmail(input.readOptionalText("Correo electrónico"));
        student.setBloodType(input.readOptionalText("Tipo de sangre (ej: O+)"));
        student.setAllergies(input.readOptionalText("Alergias"));
        student.setMedicalConditions(input.readOptionalText("Condiciones médicas"));
        student.setAdmissionDate(LocalDate.now());

        Integer studentId = require(app.students().registerStudent(student, guardian.getId()));
        ConsoleUI.success("Estudiante registrado con id " + studentId + " y vinculado a " + guardian.getFullName() + ".");

        if (input.readYesNo("¿Desea matricularlo ahora en un grado para " + currentYear() + "?")) {
            GradeLevel grade = chooseGrade();
            ConsoleUI.show(app.students().enroll(studentId, grade.getId(), currentYear()));
        }
    }

    private void registerGuardian() {
        Guardian guardian = createGuardian();
        ConsoleUI.info("Ahora puede vincularlo a un estudiante desde la opción 3.");
        ConsoleUI.card("Acudiente " + guardian.getFullName(), List.of(
                new String[]{"Documento", guardian.getDocumentType() + " " + guardian.getDocumentNumber()},
                new String[]{"Teléfono", guardian.getPhone()}));
    }

    /** Asks the guardian data and saves it. @return the saved guardian */
    private Guardian createGuardian() {
        Guardian guardian = new Guardian();
        // Adults cannot use TI (tarjeta de identidad), so only CC, CE and passport are offered
        guardian.setDocumentType(input.readChoice("Tipo de documento del acudiente",
                List.of(DocumentType.CC, DocumentType.CE, DocumentType.PASSPORT), DocumentType::getDbValue));
        guardian.setDocumentNumber(input.readValidated("Número de documento", Validator::isValidDocument,
                "Use de 5 a 20 letras o números, sin espacios ni puntos."));
        guardian.setFirstName(readName("Primer nombre"));
        guardian.setMiddleName(input.readOptionalText("Segundo nombre"));
        guardian.setLastName(readName("Primer apellido"));
        guardian.setSecondLastName(input.readOptionalText("Segundo apellido"));
        guardian.setRelationship(input.readChoice("Parentesco",
                List.of("Padre", "Madre", "Abuelo", "Abuela", "Tío", "Tía", "Hermano mayor", "Tutor Legal"), s -> s));
        guardian.setPhone(input.readValidated("Teléfono principal", Validator::isValidPhone, "Debe tener 7 o 10 dígitos."));
        guardian.setSecondaryPhone(input.readOptionalText("Teléfono secundario"));
        guardian.setEmail(input.readValidated("Correo electrónico", Validator::isValidEmail, "Correo inválido (ej: nombre@correo.com)."));
        guardian.setAddress(input.readText("Dirección"));
        guardian.setOccupation(input.readOptionalText("Ocupación"));
        guardian.setFinanciallyResponsible(input.readYesNo("¿Es responsable de los pagos?"));
        Integer id = require(app.students().registerGuardian(guardian));
        guardian.setId(id);
        ConsoleUI.success("Acudiente registrado con id " + id + ".");
        return guardian;
    }

    private void linkGuardian() {
        Student student = selectStudent();
        Guardian guardian = selectGuardian();
        boolean primary = input.readYesNo("¿Será el acudiente principal?");
        ConsoleUI.show(app.students().linkGuardian(student.getId(), guardian.getId(), primary));
    }

    /** Full record of a student: personal data, guardians, siblings and enrollments. */
    private void studentProfile() {
        Student student = selectStudent();
        Map<Integer, String> gradeNames = gradeNames();
        ConsoleUI.card(student.getFullName(), List.of(
                new String[]{"Documento", student.getDocumentType() + " " + student.getDocumentNumber()},
                new String[]{"Nacimiento", Formats.date(student.getBirthDate()) + "  (" + student.getAgeAt(LocalDate.now()) + " años)"},
                new String[]{"Género", student.getGender().getDbValue()},
                new String[]{"Dirección", student.getAddress()},
                new String[]{"Teléfono", student.getPhone()},
                new String[]{"Correo", student.getEmail()},
                new String[]{"Tipo de sangre", student.getBloodType()},
                new String[]{"Alergias", student.getAllergies()},
                new String[]{"Ingreso", Formats.date(student.getAdmissionDate())},
                new String[]{"Estado", ConsoleUI.status(student.getStatus().getDbValue())}));

        ConsoleUI.section("Acudientes y contactos de emergencia");
        BaseView.<Guardian>table()
                .column("Nombre", Guardian::getFullName)
                .column("Parentesco", Guardian::getRelationship)
                .column("Teléfono", Guardian::getPhone)
                .column("Correo", Guardian::getEmail)
                .column("Paga", g -> g.isFinanciallyResponsible() ? "Sí" : "No")
                .print(require(app.students().getGuardians(student.getId())));

        List<Student> siblings = require(app.students().getSiblings(student.getId()));
        if (!siblings.isEmpty()) {
            ConsoleUI.section("Hermanos en el colegio");
            siblings.forEach(sibling -> ConsoleUI.info(sibling.getFullName() + " (" + sibling.getStatus() + ")"));
        }

        ConsoleUI.section("Historial de matrículas");
        BaseView.<Enrollment>table()
                .column("Año", e -> String.valueOf(e.getAcademicYear()))
                .column("Grado", e -> gradeNames.get(e.getGradeLevelId()))
                .column("Estado", e -> ConsoleUI.status(e.getStatus().getDbValue()))
                .column("Fecha", e -> Formats.date(e.getEnrollmentDate()))
                .print(require(app.students().getEnrollments(student.getId())));
    }

    private void listByGrade() {
        GradeLevel grade = chooseGrade();
        int year = input.readInt("Año académico", 2000, 2100);
        printStudents(require(app.students().findByGrade(grade.getId(), year)));
    }

    private void listAll() {
        printStudents(require(app.students().findAll()));
    }

    private void studentsOfGuardian() {
        Guardian guardian = selectGuardian();
        printStudents(require(app.students().getStudentsOfGuardian(guardian.getId())));
    }

    private void enroll() {
        Student student = selectStudent();
        GradeLevel grade = chooseGrade();
        int year = input.readInt("Año académico", currentYear() - 1, currentYear() + 1);
        ConsoleUI.show(app.students().enroll(student.getId(), grade.getId(), year));
    }

    private void changeStatus() {
        Student student = selectStudent();
        ConsoleUI.info("Estado actual: " + ConsoleUI.status(student.getStatus().getDbValue()));
        StudentStatus status = chooseEnum("Nuevo estado", StudentStatus.class);
        if (input.readYesNo("¿Confirma el cambio a " + status + "?")) {
            ConsoleUI.show(app.students().changeStatus(student.getId(), status, currentYear()));
        } else {
            throw new OperationCancelledException();
        }
    }

    // ---------- helpers ----------

    private void printStudents(List<Student> students) {
        BaseView.<Student>table()
                .numberColumn("Id", s -> String.valueOf(s.getId()))
                .column("Documento", s -> s.getDocumentType() + " " + s.getDocumentNumber())
                .column("Nombre completo", Student::getFullName)
                .column("Nacimiento", s -> Formats.date(s.getBirthDate()))
                .column("Estado", s -> ConsoleUI.status(s.getStatus().getDbValue()))
                .print(students);
    }

    private GradeLevel chooseGrade() {
        return input.readChoice("Grado", require(app.students().getGradeLevels()), GradeLevel::getName);
    }

    private Map<Integer, String> gradeNames() {
        return require(app.students().getGradeLevels()).stream()
                .collect(Collectors.toMap(GradeLevel::getId, GradeLevel::getName));
    }

    private String readName(String prompt) {
        return input.readValidated(prompt, Validator::isValidName, "Solo letras y espacios, mínimo 2 caracteres.");
    }
}
