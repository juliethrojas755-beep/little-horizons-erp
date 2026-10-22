package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.dao.TeacherAssignmentDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.model.dto.SubjectAverage;
import com.littlehorizons.erp.model.enums.EnrollmentStatus;
import com.littlehorizons.erp.model.enums.Term;
import com.littlehorizons.erp.util.BusinessException;
import com.littlehorizons.erp.util.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Materias, inscripciones en materias, calificaciones y promedios.
 * <p>Todos los promedios se calculan con la API de Streams sobre las notas guardadas en la base de datos.</p>
 */
public class AcademicService {

    // DAO que usa el servicio (interfaces, entregadas por el constructor)
    private final SubjectDao subjectDao;
    private final SubjectEnrollmentDao subjectEnrollmentDao;
    private final AcademicRecordDao recordDao;
    private final TeacherAssignmentDao assignmentDao;
    private final EnrollmentDao enrollmentDao;

    public AcademicService(SubjectDao subjectDao, SubjectEnrollmentDao subjectEnrollmentDao, AcademicRecordDao recordDao,
                           TeacherAssignmentDao assignmentDao, EnrollmentDao enrollmentDao) {
        this.subjectDao = subjectDao;
        this.subjectEnrollmentDao = subjectEnrollmentDao;
        this.recordDao = recordDao;
        this.assignmentDao = assignmentDao;
        this.enrollmentDao = enrollmentDao;
    }

    // ====================== MATERIAS ======================

    /** Crea una materia nueva; el código debe ser único. */
    public int createSubject(Subject subject) {
        if (!Validator.isNotBlank(subject.getCode()) || !Validator.isNotBlank(subject.getName())) {
            throw new BusinessException("El código y el nombre de la materia son obligatorios.");
        }
        // El código (por ejemplo MAT-5) no puede repetirse
        if (subjectDao.findByCode(subject.getCode()).isPresent()) {
            throw new BusinessException("Ya existe una materia con el código " + subject.getCode() + ".");
        }
        subject.setActive(true);
        return subjectDao.insert(subject);
    }

    public List<Subject> findAllSubjects() {
        return subjectDao.findAll();
    }

    public List<Subject> findSubjectsByGrade(int gradeLevelId) {
        return subjectDao.findByGrade(gradeLevelId);
    }

    public Subject getSubject(int subjectId) {
        return subjectDao.findById(subjectId)
                .orElseThrow(() -> new BusinessException("No existe la materia con id " + subjectId + "."));
    }

    // ====================== INSCRIPCIÓN EN MATERIAS (muchos a muchos por período) ======================

    /** Inscribe a un estudiante en una materia de SU grado para un período. */
    public int enrollInSubject(int studentId, int subjectId, int academicYear, Term term) {
        // La nota FINAL no es un período de cursada: solo se inscribe en I, II, III o IV
        if (term == Term.FINAL) {
            throw new BusinessException("La inscripción se hace por período I, II, III o IV.");
        }
        Enrollment enrollment = activeEnrollment(studentId, academicYear);   // debe tener matrícula activa
        Subject subject = getSubject(subjectId);
        // La materia tiene que ser del mismo grado en el que está matriculado
        if (subject.getGradeLevelId() != enrollment.getGradeLevelId()) {
            throw new BusinessException("La materia " + subject.getName() + " no pertenece al grado del estudiante.");
        }
        // No puede inscribirse dos veces en la misma materia y período
        boolean duplicated = subjectEnrollmentDao.findByStudent(studentId).stream()
                .anyMatch(e -> e.getSubjectId() == subjectId && e.getAcademicYear() == academicYear && e.getTerm() == term);
        if (duplicated) {
            throw new BusinessException("El estudiante ya está inscrito en esa materia para el período " + term + ".");
        }
        // Se arma y se guarda la inscripción
        SubjectEnrollment subjectEnrollment = new SubjectEnrollment();
        subjectEnrollment.setStudentId(studentId);
        subjectEnrollment.setSubjectId(subjectId);
        subjectEnrollment.setAcademicYear(academicYear);
        subjectEnrollment.setTerm(term);
        subjectEnrollment.setSection("A");
        subjectEnrollment.setEnrollmentDate(LocalDate.now());
        subjectEnrollment.setStatus(EnrollmentStatus.ACTIVE);
        return subjectEnrollmentDao.insert(subjectEnrollment);
    }

    /**
     * Inscribe al estudiante en todas las materias de su grado para el período.
     *
     * @return cuántas inscripciones se crearon
     */
    public long enrollInAllGradeSubjects(int studentId, int academicYear, Term term) {
        Enrollment enrollment = activeEnrollment(studentId, academicYear);
        List<SubjectEnrollment> current = subjectEnrollmentDao.findByStudent(studentId);
        // Materias del grado en las que TODAVÍA no está inscrito en ese año y período
        List<Subject> pending = subjectDao.findByGrade(enrollment.getGradeLevelId()).stream()
                .filter(subject -> current.stream().noneMatch(e -> e.getSubjectId() == subject.getId()
                        && e.getAcademicYear() == academicYear && e.getTerm() == term))
                .toList();
        // Se inscribe en cada una de las pendientes
        pending.forEach(subject -> enrollInSubject(studentId, subject.getId(), academicYear, term));
        return pending.size();
    }

    public List<SubjectEnrollment> getSubjectEnrollments(int studentId) {
        return subjectEnrollmentDao.findByStudent(studentId);
    }

    // ====================== CALIFICACIONES ======================

    /**
     * Registra o reemplaza la nota de un estudiante. El profesor se toma de la asignación de la materia.
     *
     * @return el id del registro
     */
    public int recordGrade(int studentId, int subjectId, int academicYear, Term term, BigDecimal grade, String notes) {
        // 1) la nota debe estar entre 0.0 y 5.0 con un decimal
        if (!Validator.isValidGrade(grade)) {
            throw new BusinessException("La nota debe estar entre 0.0 y 5.0 con un decimal.");
        }
        activeEnrollment(studentId, academicYear);        // 2) debe tener matrícula activa
        // 3) para los períodos I a IV debe estar inscrito en la materia (la nota FINAL no lo exige)
        if (term != Term.FINAL) {
            boolean enrolled = subjectEnrollmentDao.findByStudent(studentId).stream()
                    .anyMatch(e -> e.getSubjectId() == subjectId && e.getAcademicYear() == academicYear && e.getTerm() == term);
            if (!enrolled) {
                throw new BusinessException("El estudiante no está inscrito en esa materia para el período " + term + ".");
            }
        }
        // 4) la materia debe tener un profesor asignado (él queda registrado como quien calificó)
        TeacherAssignment assignment = assignmentDao.findBySubjectAndYear(subjectId, academicYear)
                .orElseThrow(() -> new BusinessException("La materia no tiene profesor asignado en " + academicYear + "."));

        // 5) si ya existía una nota para ese estudiante, materia y período, se actualiza en lugar de duplicarla
        Optional<AcademicRecord> existing = recordDao.findByStudent(studentId).stream()
                .filter(r -> r.getSubjectId() == subjectId && r.getAcademicYear() == academicYear && r.getTerm() == term)
                .findFirst();
        AcademicRecord record = existing.orElseGet(AcademicRecord::new);   // nueva si no existía
        record.setStudentId(studentId);
        record.setSubjectId(subjectId);
        record.setTeacherId(assignment.getTeacherId());
        record.setAcademicYear(academicYear);
        record.setTerm(term);
        record.setGrade(grade.setScale(1, RoundingMode.HALF_UP));         // exactamente un decimal, como DECIMAL(2,1)
        record.setNotes(notes);
        record.setRecordDate(LocalDate.now());
        if (existing.isPresent()) {
            recordDao.update(record);          // ya existía: UPDATE
            return record.getId();
        }
        return recordDao.insert(record);       // no existía: INSERT
    }

    /**
     * Calcula la nota FINAL de una materia como el promedio de los períodos I a IV y la guarda.
     *
     * @return la nota final
     */
    public BigDecimal closeFinalGrade(int studentId, int subjectId, int academicYear) {
        // Notas de los períodos de esa materia (sin contar una nota final anterior)
        List<BigDecimal> termGrades = recordDao.findByStudent(studentId).stream()
                .filter(r -> r.getSubjectId() == subjectId && r.getAcademicYear() == academicYear && r.getTerm() != Term.FINAL)
                .map(AcademicRecord::getGrade)
                .toList();
        if (termGrades.isEmpty()) {
            throw new BusinessException("No hay notas de períodos para calcular la nota final.");
        }
        BigDecimal finalGrade = average(termGrades);
        // Se guarda como una nota más, con el período FINAL
        recordGrade(studentId, subjectId, academicYear, Term.FINAL, finalGrade, "Promedio de " + termGrades.size() + " períodos");
        return finalGrade;
    }

    // ====================== PROMEDIOS (Streams) ======================

    /** Promedio de cada materia de un estudiante en un año (períodos I a IV, sin la nota FINAL). */
    public List<SubjectAverage> averageBySubject(int studentId, int academicYear) {
        // groupingBy agrupa las notas por materia: Map<id de materia, lista de notas>
        Map<Integer, List<BigDecimal>> gradesBySubject = recordDao.findByStudent(studentId).stream()
                .filter(r -> r.getAcademicYear() == academicYear && r.getTerm() != Term.FINAL)
                .collect(Collectors.groupingBy(AcademicRecord::getSubjectId,
                        Collectors.mapping(AcademicRecord::getGrade, Collectors.toList())));   // mapping deja solo el valor de la nota
        return toSubjectAverages(gradesBySubject);
    }

    /** Promedio general del estudiante en un período (todas sus materias). Vacío si no tiene notas. */
    public Optional<BigDecimal> termAverage(int studentId, int academicYear, Term term) {
        List<BigDecimal> grades = recordDao.findByStudent(studentId).stream()
                .filter(r -> r.getAcademicYear() == academicYear && r.getTerm() == term)
                .map(AcademicRecord::getGrade)
                .toList();
        return grades.isEmpty() ? Optional.empty() : Optional.of(average(grades));
    }

    /** Promedio de todo el curso (grado) en cada materia durante un año. */
    public List<SubjectAverage> courseAverageBySubject(int gradeLevelId, int academicYear) {
        Map<Integer, List<BigDecimal>> gradesBySubject = subjectDao.findByGrade(gradeLevelId).stream()
                // flatMap junta en un solo flujo las notas de todas las materias del grado
                .flatMap(subject -> recordDao.findBySubject(subject.getId(), academicYear).stream())
                .filter(r -> r.getTerm() != Term.FINAL)
                .collect(Collectors.groupingBy(AcademicRecord::getSubjectId,
                        Collectors.mapping(AcademicRecord::getGrade, Collectors.toList())));
        return toSubjectAverages(gradesBySubject);
    }

    /**
     * Historial académico completo: primero el año, luego el período y luego las notas de ese período.
     * TreeMap mantiene los años y los períodos ordenados.
     */
    public Map<Integer, Map<Term, List<AcademicRecord>>> academicHistory(int studentId) {
        return recordDao.findByStudent(studentId).stream()
                // agrupación en dos niveles: año -> período -> lista de notas
                .collect(Collectors.groupingBy(AcademicRecord::getAcademicYear, TreeMap::new,
                        Collectors.groupingBy(AcademicRecord::getTerm, TreeMap::new, Collectors.toList())));
    }

    /** Tabla de búsqueda id -> materia, que usan las vistas para mostrar nombres en lugar de ids. */
    public Map<Integer, Subject> subjectsById() {
        return subjectDao.findAll().stream().collect(Collectors.toMap(Subject::getId, Function.identity()));
    }

    // ====================== AYUDAS ======================

    /** Promedio con un decimal, igual que la columna DECIMAL(2,1). */
    public static BigDecimal average(List<BigDecimal> values) {
        return average(values, 1);
    }

    /** Promedio con los decimales indicados (los rankings usan 2 decimales para desempatar). */
    public static BigDecimal average(List<BigDecimal> values, int scale) {
        // Se suman todas las notas empezando desde cero y luego se divide entre la cantidad
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), scale, RoundingMode.HALF_UP);
    }

    /** Convierte el mapa materia -> notas en una lista de promedios ordenada por nombre de materia. */
    private List<SubjectAverage> toSubjectAverages(Map<Integer, List<BigDecimal>> gradesBySubject) {
        Map<Integer, Subject> subjects = subjectsById();
        return gradesBySubject.entrySet().stream()
                .map(entry -> new SubjectAverage(entry.getKey(),
                        Optional.ofNullable(subjects.get(entry.getKey())).map(Subject::getName).orElse("?"),
                        average(entry.getValue()), entry.getValue().size()))
                .sorted(Comparator.comparing(SubjectAverage::subjectName))
                .toList();
    }

    /** Matrícula ACTIVA del estudiante en el año; si no tiene, lanza un error de negocio. */
    private Enrollment activeEnrollment(int studentId, int academicYear) {
        return enrollmentDao.findByStudentAndYear(studentId, academicYear)
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("El estudiante no tiene matrícula activa en " + academicYear + "."));
    }
}
