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
 * Módulo 1: estudiantes, acudientes y matrículas.
 * <p>Esta vista solo pide datos al usuario, llama al controlador y muestra el resultado; las reglas están en el servicio.</p>
 */
public class StudentView extends BaseView {

    // Título que aparece en el encabezado del menú
    private static final String MODULE = "Gestión de Estudiantes";

    public StudentView(AppContext app, InputReader input) {
        super(app, input);
    }

    /**
     * Arma el menú del módulo: cada opción es un texto y un método (referencia con ::).
     */
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

    /**
     * Registro guiado de un estudiante: primero el acudiente, luego sus datos, y al final se ofrece matricularlo.
     */
    private void registerStudent() {
        // PASO 1: el acudiente (regla: todo estudiante tiene al menos uno)
        ConsoleUI.section("1. Acudiente responsable");
        ConsoleUI.info("Todo estudiante debe tener al menos un acudiente.");
        // Si ya existe se busca; si no, se registra uno nuevo
        Guardian guardian = input.readYesNo("¿El acudiente ya está registrado?") ? selectGuardian() : createGuardian();

        // PASO 2: datos del estudiante; cada pregunta se repite hasta que el dato sea válido
        ConsoleUI.section("2. Datos del estudiante");
        Student student = new Student();
        // chooseEnum muestra las constantes del enum numeradas para elegir
        student.setDocumentType(chooseEnum("Tipo de documento", DocumentType.class));
        student.setDocumentNumber(input.readValidated("Número de documento", Validator::isValidDocument,
                "Use de 5 a 20 letras o números, sin espacios ni puntos."));
        student.setFirstName(readName("Primer nombre"));
        // readOptionalText: si el usuario solo presiona Enter, el dato queda en null
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
        // La fecha de ingreso es hoy
        student.setAdmissionDate(LocalDate.now());

        // El controlador valida las reglas y guarda; require() cancela la pantalla si algo falla
        Integer studentId = require(app.students().registerStudent(student, guardian.getId()));
        ConsoleUI.success("Estudiante registrado con id " + studentId + " y vinculado a " + guardian.getFullName() + ".");

        // Opcional: matricularlo de una vez en un grado para el año actual
        if (input.readYesNo("¿Desea matricularlo ahora en un grado para " + currentYear() + "?")) {
            GradeLevel grade = chooseGrade();
            ConsoleUI.show(app.students().enroll(studentId, grade.getId(), currentYear()));
        }
    }

    /**
     * Registra un acudiente nuevo y muestra su ficha.
     */
    private void registerGuardian() {
        Guardian guardian = createGuardian();
        ConsoleUI.info("Ahora puede vincularlo a un estudiante desde la opción 3.");
        // Ficha con los datos principales del acudiente recién creado
        ConsoleUI.card("Acudiente " + guardian.getFullName(), List.of(
                new String[]{"Documento", guardian.getDocumentType() + " " + guardian.getDocumentNumber()},
                new String[]{"Teléfono", guardian.getPhone()}));
    }

    /**
     * Pide los datos de un acudiente y lo guarda.
     *
     * @return el acudiente guardado, ya con su id
     */
    private Guardian createGuardian() {
        Guardian guardian = new Guardian();
        // Un adulto no puede tener TI (tarjeta de identidad), por eso solo se ofrecen CC, CE y pasaporte
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

    /**
     * Vincula otro acudiente a un estudiante (por ejemplo, el segundo padre).
     */
    private void linkGuardian() {
        Student student = selectStudent();
        Guardian guardian = selectGuardian();
        boolean primary = input.readYesNo("¿Será el acudiente principal?");
        ConsoleUI.show(app.students().linkGuardian(student.getId(), guardian.getId(), primary));
    }

    /** Ficha completa de un estudiante: datos personales, acudientes, hermanos y matrículas. */
    private void studentProfile() {
        Student student = selectStudent();
        // Tabla para convertir el id del grado en su nombre al mostrar las matrículas
        Map<Integer, String> gradeNames = gradeNames();
        // Ficha con los datos personales (cada fila es una pareja etiqueta/valor)
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

        // Tabla de acudientes: cada columna se declara con una lambda que saca el dato de cada fila
        ConsoleUI.section("Acudientes y contactos de emergencia");
        BaseView.<Guardian>table()
                .column("Nombre", Guardian::getFullName)
                .column("Parentesco", Guardian::getRelationship)
                .column("Teléfono", Guardian::getPhone)
                .column("Correo", Guardian::getEmail)
                .column("Paga", g -> g.isFinanciallyResponsible() ? "Sí" : "No")
                .print(require(app.students().getGuardians(student.getId())));

        // Hermanos = otros estudiantes que comparten acudiente
        List<Student> siblings = require(app.students().getSiblings(student.getId()));
        if (!siblings.isEmpty()) {
            ConsoleUI.section("Hermanos en el colegio");
            siblings.forEach(sibling -> ConsoleUI.info(sibling.getFullName() + " (" + sibling.getStatus() + ")"));
        }

        // Tabla con todas las matrículas del estudiante, de la más reciente a la más antigua
        ConsoleUI.section("Historial de matrículas");
        BaseView.<Enrollment>table()
                .column("Año", e -> String.valueOf(e.getAcademicYear()))
                .column("Grado", e -> gradeNames.get(e.getGradeLevelId()))
                .column("Estado", e -> ConsoleUI.status(e.getStatus().getDbValue()))
                .column("Fecha", e -> Formats.date(e.getEnrollmentDate()))
                .print(require(app.students().getEnrollments(student.getId())));
    }

    /**
     * Lista los estudiantes matriculados en un grado y año.
     */
    private void listByGrade() {
        GradeLevel grade = chooseGrade();
        // El año debe estar en un rango razonable
        int year = input.readInt("Año académico", 2000, 2100);
        printStudents(require(app.students().findByGrade(grade.getId(), year)));
    }

    /**
     * Lista todos los estudiantes.
     */
    private void listAll() {
        printStudents(require(app.students().findAll()));
    }

    /**
     * Lista los hijos de un acudiente (los hermanos comparten acudiente).
     */
    private void studentsOfGuardian() {
        Guardian guardian = selectGuardian();
        printStudents(require(app.students().getStudentsOfGuardian(guardian.getId())));
    }

    /**
     * Matricula a un estudiante en un grado para un año.
     */
    private void enroll() {
        Student student = selectStudent();
        GradeLevel grade = chooseGrade();
        // Solo se permite matricular en el año anterior, el actual o el siguiente
        int year = input.readInt("Año académico", currentYear() - 1, currentYear() + 1);
        ConsoleUI.show(app.students().enroll(student.getId(), grade.getId(), year));
    }

    /**
     * Cambia el estado del estudiante (Graduado, Retirado, Suspendido) pidiendo confirmación.
     */
    private void changeStatus() {
        Student student = selectStudent();
        ConsoleUI.info("Estado actual: " + ConsoleUI.status(student.getStatus().getDbValue()));
        StudentStatus status = chooseEnum("Nuevo estado", StudentStatus.class);
        // Cambio de estado: se confirma antes de aplicarlo; si no confirma, se cancela
        if (input.readYesNo("¿Confirma el cambio a " + status + "?")) {
            ConsoleUI.show(app.students().changeStatus(student.getId(), status, currentYear()));
        } else {
            throw new OperationCancelledException();
        }
    }

    // ====================== AYUDAS ======================

    /**
     * Imprime una lista de estudiantes en una tabla.
     */
    private void printStudents(List<Student> students) {
        BaseView.<Student>table()
                .numberColumn("Id", s -> String.valueOf(s.getId()))
                .column("Documento", s -> s.getDocumentType() + " " + s.getDocumentNumber())
                .column("Nombre completo", Student::getFullName)
                .column("Nacimiento", s -> Formats.date(s.getBirthDate()))
                .column("Estado", s -> ConsoleUI.status(s.getStatus().getDbValue()))
                .print(students);
    }

    /**
     * Deja escoger un grado de la lista (de Preescolar a Once).
     */
    private GradeLevel chooseGrade() {
        return input.readChoice("Grado", require(app.students().getGradeLevels()), GradeLevel::getName);
    }

    /**
     * Tabla id de grado -> nombre del grado.
     */
    private Map<Integer, String> gradeNames() {
        return require(app.students().getGradeLevels()).stream()
                .collect(Collectors.toMap(GradeLevel::getId, GradeLevel::getName));
    }

    /**
     * Lee un nombre o apellido y lo valida (solo letras, mínimo 2 caracteres).
     */
    private String readName(String prompt) {
        return input.readValidated(prompt, Validator::isValidName, "Solo letras y espacios, mínimo 2 caracteres.");
    }
}
