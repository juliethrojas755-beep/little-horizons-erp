package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.GradeLevelDao;
import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentGuardian;
import com.littlehorizons.erp.model.enums.EnrollmentStatus;
import com.littlehorizons.erp.model.enums.StudentStatus;
import com.littlehorizons.erp.util.BusinessException;
import com.littlehorizons.erp.util.Validator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Reglas de negocio de estudiantes, acudientes y matrículas anuales.
 * <p>Los DAO llegan por el constructor (inyección de dependencias), así que esta clase depende de
 * interfaces y nunca de clases JDBC. Aquí NO hay SQL: eso vive en la capa dao.</p>
 */
public class StudentService {

    // Edades permitidas para un estudiante de preescolar a grado 11
    private static final int MIN_AGE = 3;
    private static final int MAX_AGE = 20;

    // DAO que necesita este servicio (son interfaces)
    private final StudentDao studentDao;
    private final GuardianDao guardianDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeLevelDao gradeLevelDao;

    // Inyección por constructor: AppContext entrega las implementaciones concretas
    public StudentService(StudentDao studentDao, GuardianDao guardianDao, EnrollmentDao enrollmentDao,
                          GradeLevelDao gradeLevelDao) {
        this.studentDao = studentDao;
        this.guardianDao = guardianDao;
        this.enrollmentDao = enrollmentDao;
        this.gradeLevelDao = gradeLevelDao;
    }

    // ====================== REGISTRO ======================

    /**
     * Registra un estudiante vinculado a un acudiente existente (todo estudiante necesita al menos uno).
     *
     * @return el id del estudiante nuevo
     */
    public int registerStudent(Student student, int guardianId) {
        validateStudent(student);                                   // 1) datos válidos (documento, nombre, edad, correo)
        // 2) el documento no puede estar repetido
        if (studentDao.findByDocument(student.getDocumentNumber()).isPresent()) {
            throw new BusinessException("Ya existe un estudiante con el documento " + student.getDocumentNumber() + ".");
        }
        // 3) el acudiente debe existir (regla: cada estudiante tiene al menos un acudiente)
        guardianDao.findById(guardianId)
                .orElseThrow(() -> new BusinessException("El acudiente seleccionado no existe."));
        student.setStatus(StudentStatus.ACTIVE);                    // un estudiante nuevo siempre empieza Activo
        int id = studentDao.insert(student);                        // 4) se guarda y MySQL devuelve el id
        linkGuardian(id, guardianId, true);                         // 5) se vincula como acudiente principal
        return id;
    }

    /** @return el id del acudiente nuevo */
    public int registerGuardian(Guardian guardian) {
        if (!Validator.isValidName(guardian.getFirstName()) || !Validator.isValidName(guardian.getLastName())) {
            throw new BusinessException("El nombre y el apellido del acudiente son obligatorios y solo admiten letras.");
        }
        if (!Validator.isValidPhone(guardian.getPhone())) {
            throw new BusinessException("El teléfono principal debe tener 7 o 10 dígitos.");
        }
        if (guardianDao.findByDocument(guardian.getDocumentNumber()).isPresent()) {
            throw new BusinessException("Ya existe un acudiente con el documento " + guardian.getDocumentNumber() + ".");
        }
        return guardianDao.insert(guardian);
    }

    /** Vincula otro acudiente a un estudiante (los hermanos comparten acudientes). */
    public void linkGuardian(int studentId, int guardianId, boolean primary) {
        // Se revisa que el vínculo no exista ya para no duplicarlo
        boolean alreadyLinked = guardianDao.findByStudent(studentId).stream()
                .anyMatch(guardian -> guardian.getId() == guardianId);
        if (alreadyLinked) {
            throw new BusinessException("Ese acudiente ya está vinculado al estudiante.");
        }
        // Se arma el vínculo (fila de la tabla intermedia estudiante_acudiente)
        StudentGuardian link = new StudentGuardian();
        link.setStudentId(studentId);
        link.setGuardianId(guardianId);
        link.setPrimary(primary);
        link.setEmergencyContact(true);
        guardianDao.linkStudent(link);
    }

    /** Actualiza los datos de un estudiante después de validarlos. */
    public boolean updateStudent(Student student) {
        validateStudent(student);
        return studentDao.update(student);
    }

    // ====================== ESTADO Y MATRÍCULA ======================

    /** Cambia el estado del estudiante y mantiene coherente su matrícula del año. */
    public void changeStatus(int studentId, StudentStatus status, int academicYear) {
        Student student = getStudent(studentId);
        student.setStatus(status);
        studentDao.update(student);
        // Si el estudiante tiene matrícula ese año, su estado se ajusta según el nuevo estado
        enrollmentDao.findByStudentAndYear(studentId, academicYear).ifPresent(enrollment -> {
            if (status == StudentStatus.WITHDRAWN) {
                enrollmentDao.updateStatus(enrollment.getId(), EnrollmentStatus.WITHDRAWN);   // Retirado -> matrícula Retirada
            } else if (status == StudentStatus.GRADUATED) {
                enrollmentDao.updateStatus(enrollment.getId(), EnrollmentStatus.COMPLETED);   // Graduado -> matrícula Completada
            }
        });
    }

    /** Matricula al estudiante en un grado para un año (solo se permite una matrícula por año). */
    public int enroll(int studentId, int gradeLevelId, int academicYear) {
        Student student = getStudent(studentId);
        // Solo estudiantes activos pueden matricularse
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException("Solo se pueden matricular estudiantes activos.");
        }
        // No puede tener dos matrículas en el mismo año
        if (enrollmentDao.findByStudentAndYear(studentId, academicYear).isPresent()) {
            throw new BusinessException("El estudiante ya tiene matrícula para el año " + academicYear + ".");
        }
        // El grado debe existir en el catálogo
        gradeLevelDao.findById(gradeLevelId).orElseThrow(() -> new BusinessException("El grado no existe."));
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setGradeLevelId(gradeLevelId);
        enrollment.setAcademicYear(academicYear);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrollmentDate(LocalDate.now());
        return enrollmentDao.insert(enrollment);
    }

    // ====================== CONSULTAS ======================

    /** Busca un estudiante por id; si no existe lanza un error de negocio con mensaje claro. */
    public Student getStudent(int studentId) {
        return studentDao.findById(studentId)
                .orElseThrow(() -> new BusinessException("No existe un estudiante con id " + studentId + "."));
    }

    public Optional<Student> findByDocument(String documentNumber) {
        return studentDao.findByDocument(documentNumber);
    }

    public List<Student> findAll() {
        return studentDao.findAll();
    }

    public List<Student> search(String text) {
        return studentDao.searchByName(text);
    }

    public List<Student> findByGrade(int gradeLevelId, int academicYear) {
        return studentDao.findByGrade(gradeLevelId, academicYear);
    }

    public List<Guardian> getGuardians(int studentId) {
        return guardianDao.findByStudent(studentId);
    }

    public List<Guardian> findAllGuardians() {
        return guardianDao.findAll();
    }

    public Optional<Guardian> findGuardianByDocument(String documentNumber) {
        return guardianDao.findByDocument(documentNumber);
    }

    public Guardian getGuardian(int guardianId) {
        return guardianDao.findById(guardianId)
                .orElseThrow(() -> new BusinessException("No existe un acudiente con id " + guardianId + "."));
    }

    public List<Student> getStudentsOfGuardian(int guardianId) {
        return studentDao.findByGuardian(guardianId);
    }

    /** Hermanos = otros estudiantes que comparten al menos un acudiente con este estudiante. */
    public List<Student> getSiblings(int studentId) {
        return guardianDao.findByStudent(studentId).stream()                                   // acudientes del estudiante
                .flatMap(guardian -> studentDao.findByGuardian(guardian.getId()).stream())     // todos los hijos de cada acudiente
                .filter(student -> student.getId() != studentId)                               // se excluye al propio estudiante
                .filter(distinctById())                                                        // sin repetir si comparten dos acudientes
                .toList();
    }

    public List<Enrollment> getEnrollments(int studentId) {
        return enrollmentDao.findByStudent(studentId);
    }

    public Optional<Enrollment> getEnrollment(int studentId, int academicYear) {
        return enrollmentDao.findByStudentAndYear(studentId, academicYear);
    }

    /** Grados ordenados de Preescolar (0) a Once (11). */
    public List<GradeLevel> getGradeLevels() {
        return gradeLevelDao.findAll().stream()
                .sorted(Comparator.comparingInt(GradeLevel::getNumericLevel))
                .toList();
    }

    // ====================== VALIDACIONES ======================

    /** Reglas que debe cumplir un estudiante antes de guardarse; lanza BusinessException si alguna falla. */
    private void validateStudent(Student student) {
        if (!Validator.isValidDocument(student.getDocumentNumber())) {
            throw new BusinessException("El documento debe tener entre 5 y 20 letras o números.");
        }
        if (!Validator.isValidName(student.getFirstName()) || !Validator.isValidName(student.getLastName())) {
            throw new BusinessException("El primer nombre y el primer apellido son obligatorios y solo admiten letras.");
        }
        if (student.getBirthDate() == null || student.getBirthDate().isAfter(LocalDate.now())) {
            throw new BusinessException("La fecha de nacimiento no puede ser futura.");
        }
        // La edad se calcula con la fecha de nacimiento y debe estar en el rango permitido
        int age = student.getAgeAt(LocalDate.now());
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new BusinessException("La edad del estudiante debe estar entre " + MIN_AGE + " y " + MAX_AGE + " años.");
        }
        // El correo es opcional, pero si se escribe debe tener formato válido
        if (student.getEmail() != null && !Validator.isValidEmail(student.getEmail())) {
            throw new BusinessException("El correo electrónico no es válido.");
        }
    }

    /**
     * Filtro con memoria: deja pasar solo la primera vez que aparece cada id de estudiante
     * (sirve para eliminar duplicados dentro de un Stream).
     */
    private static Predicate<Student> distinctById() {
        Set<Integer> seen = new HashSet<>();
        // Set.add devuelve true solo si el elemento era nuevo, y false si ya estaba
        return student -> seen.add(student.getId());
    }
}
