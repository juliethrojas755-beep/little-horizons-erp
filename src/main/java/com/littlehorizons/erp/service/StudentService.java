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
 * Business rules for students, guardians and yearly enrollments.
 * <p>DAOs arrive through the constructor (Dependency Injection), so this class depends on
 * interfaces, never on JDBC classes.</p>
 */
public class StudentService {

    private static final int MIN_AGE = 3;
    private static final int MAX_AGE = 20;

    private final StudentDao studentDao;
    private final GuardianDao guardianDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeLevelDao gradeLevelDao;

    public StudentService(StudentDao studentDao, GuardianDao guardianDao, EnrollmentDao enrollmentDao,
                          GradeLevelDao gradeLevelDao) {
        this.studentDao = studentDao;
        this.guardianDao = guardianDao;
        this.enrollmentDao = enrollmentDao;
        this.gradeLevelDao = gradeLevelDao;
    }

    /**
     * Registers a student linked to an existing guardian (every student needs at least one).
     *
     * @return the new student id
     */
    public int registerStudent(Student student, int guardianId) {
        validateStudent(student);
        if (studentDao.findByDocument(student.getDocumentNumber()).isPresent()) {
            throw new BusinessException("Ya existe un estudiante con el documento " + student.getDocumentNumber() + ".");
        }
        guardianDao.findById(guardianId)
                .orElseThrow(() -> new BusinessException("El acudiente seleccionado no existe."));
        student.setStatus(StudentStatus.ACTIVE);
        int id = studentDao.insert(student);
        linkGuardian(id, guardianId, true);
        return id;
    }

    /** @return the new guardian id */
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

    /** Links one more guardian to a student (siblings share guardians). */
    public void linkGuardian(int studentId, int guardianId, boolean primary) {
        boolean alreadyLinked = guardianDao.findByStudent(studentId).stream()
                .anyMatch(guardian -> guardian.getId() == guardianId);
        if (alreadyLinked) {
            throw new BusinessException("Ese acudiente ya está vinculado al estudiante.");
        }
        StudentGuardian link = new StudentGuardian();
        link.setStudentId(studentId);
        link.setGuardianId(guardianId);
        link.setPrimary(primary);
        link.setEmergencyContact(true);
        guardianDao.linkStudent(link);
    }

    public boolean updateStudent(Student student) {
        validateStudent(student);
        return studentDao.update(student);
    }

    /** Changes the student status and keeps the enrollment of the year consistent. */
    public void changeStatus(int studentId, StudentStatus status, int academicYear) {
        Student student = getStudent(studentId);
        student.setStatus(status);
        studentDao.update(student);
        enrollmentDao.findByStudentAndYear(studentId, academicYear).ifPresent(enrollment -> {
            if (status == StudentStatus.WITHDRAWN) {
                enrollmentDao.updateStatus(enrollment.getId(), EnrollmentStatus.WITHDRAWN);
            } else if (status == StudentStatus.GRADUATED) {
                enrollmentDao.updateStatus(enrollment.getId(), EnrollmentStatus.COMPLETED);
            }
        });
    }

    /** Enrolls the student in a grade for a year (only one enrollment per year). */
    public int enroll(int studentId, int gradeLevelId, int academicYear) {
        Student student = getStudent(studentId);
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException("Solo se pueden matricular estudiantes activos.");
        }
        if (enrollmentDao.findByStudentAndYear(studentId, academicYear).isPresent()) {
            throw new BusinessException("El estudiante ya tiene matrícula para el año " + academicYear + ".");
        }
        gradeLevelDao.findById(gradeLevelId).orElseThrow(() -> new BusinessException("El grado no existe."));
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setGradeLevelId(gradeLevelId);
        enrollment.setAcademicYear(academicYear);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrollmentDate(LocalDate.now());
        return enrollmentDao.insert(enrollment);
    }

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

    /** Siblings = other students that share at least one guardian. */
    public List<Student> getSiblings(int studentId) {
        return guardianDao.findByStudent(studentId).stream()
                .flatMap(guardian -> studentDao.findByGuardian(guardian.getId()).stream())
                .filter(student -> student.getId() != studentId)
                .filter(distinctById())
                .toList();
    }

    public List<Enrollment> getEnrollments(int studentId) {
        return enrollmentDao.findByStudent(studentId);
    }

    public Optional<Enrollment> getEnrollment(int studentId, int academicYear) {
        return enrollmentDao.findByStudentAndYear(studentId, academicYear);
    }

    public List<GradeLevel> getGradeLevels() {
        return gradeLevelDao.findAll().stream()
                .sorted(Comparator.comparingInt(GradeLevel::getNumericLevel))
                .toList();
    }

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
        int age = student.getAgeAt(LocalDate.now());
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new BusinessException("La edad del estudiante debe estar entre " + MIN_AGE + " y " + MAX_AGE + " años.");
        }
        if (student.getEmail() != null && !Validator.isValidEmail(student.getEmail())) {
            throw new BusinessException("El correo electrónico no es válido.");
        }
    }

    /** Stateful filter that keeps the first student of each id (used to remove duplicates). */
    private static Predicate<Student> distinctById() {
        Set<Integer> seen = new HashSet<>();
        return student -> seen.add(student.getId());
    }
}
