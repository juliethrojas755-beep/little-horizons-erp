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
 * Subjects, subject enrollments, grades and averages.
 * <p>All averages are computed with the Stream API over the grades of the database.</p>
 */
public class AcademicService {

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

    // ---------- subjects ----------

    public int createSubject(Subject subject) {
        if (!Validator.isNotBlank(subject.getCode()) || !Validator.isNotBlank(subject.getName())) {
            throw new BusinessException("El código y el nombre de la materia son obligatorios.");
        }
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

    // ---------- subject enrollment (many to many per term) ----------

    /** Enrolls a student in a subject of HIS grade for one term. */
    public int enrollInSubject(int studentId, int subjectId, int academicYear, Term term) {
        if (term == Term.FINAL) {
            throw new BusinessException("La inscripción se hace por período I, II, III o IV.");
        }
        Enrollment enrollment = activeEnrollment(studentId, academicYear);
        Subject subject = getSubject(subjectId);
        if (subject.getGradeLevelId() != enrollment.getGradeLevelId()) {
            throw new BusinessException("La materia " + subject.getName() + " no pertenece al grado del estudiante.");
        }
        boolean duplicated = subjectEnrollmentDao.findByStudent(studentId).stream()
                .anyMatch(e -> e.getSubjectId() == subjectId && e.getAcademicYear() == academicYear && e.getTerm() == term);
        if (duplicated) {
            throw new BusinessException("El estudiante ya está inscrito en esa materia para el período " + term + ".");
        }
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

    /** Enrolls the student in every subject of his grade for the term. @return how many were created */
    public long enrollInAllGradeSubjects(int studentId, int academicYear, Term term) {
        Enrollment enrollment = activeEnrollment(studentId, academicYear);
        List<SubjectEnrollment> current = subjectEnrollmentDao.findByStudent(studentId);
        List<Subject> pending = subjectDao.findByGrade(enrollment.getGradeLevelId()).stream()
                .filter(subject -> current.stream().noneMatch(e -> e.getSubjectId() == subject.getId()
                        && e.getAcademicYear() == academicYear && e.getTerm() == term))
                .toList();
        pending.forEach(subject -> enrollInSubject(studentId, subject.getId(), academicYear, term));
        return pending.size();
    }

    public List<SubjectEnrollment> getSubjectEnrollments(int studentId) {
        return subjectEnrollmentDao.findByStudent(studentId);
    }

    // ---------- grades ----------

    /**
     * Records or replaces the grade of a student. The teacher is taken from the subject assignment.
     *
     * @return the record id
     */
    public int recordGrade(int studentId, int subjectId, int academicYear, Term term, BigDecimal grade, String notes) {
        if (!Validator.isValidGrade(grade)) {
            throw new BusinessException("La nota debe estar entre 0.0 y 5.0 con un decimal.");
        }
        activeEnrollment(studentId, academicYear);
        if (term != Term.FINAL) {
            boolean enrolled = subjectEnrollmentDao.findByStudent(studentId).stream()
                    .anyMatch(e -> e.getSubjectId() == subjectId && e.getAcademicYear() == academicYear && e.getTerm() == term);
            if (!enrolled) {
                throw new BusinessException("El estudiante no está inscrito en esa materia para el período " + term + ".");
            }
        }
        TeacherAssignment assignment = assignmentDao.findBySubjectAndYear(subjectId, academicYear)
                .orElseThrow(() -> new BusinessException("La materia no tiene profesor asignado en " + academicYear + "."));

        Optional<AcademicRecord> existing = recordDao.findByStudent(studentId).stream()
                .filter(r -> r.getSubjectId() == subjectId && r.getAcademicYear() == academicYear && r.getTerm() == term)
                .findFirst();
        AcademicRecord record = existing.orElseGet(AcademicRecord::new);
        record.setStudentId(studentId);
        record.setSubjectId(subjectId);
        record.setTeacherId(assignment.getTeacherId());
        record.setAcademicYear(academicYear);
        record.setTerm(term);
        record.setGrade(grade.setScale(1, RoundingMode.HALF_UP));
        record.setNotes(notes);
        record.setRecordDate(LocalDate.now());
        if (existing.isPresent()) {
            recordDao.update(record);
            return record.getId();
        }
        return recordDao.insert(record);
    }

    /**
     * Computes the FINAL grade of a subject as the average of terms I to IV and stores it.
     *
     * @return the final grade
     */
    public BigDecimal closeFinalGrade(int studentId, int subjectId, int academicYear) {
        List<BigDecimal> termGrades = recordDao.findByStudent(studentId).stream()
                .filter(r -> r.getSubjectId() == subjectId && r.getAcademicYear() == academicYear && r.getTerm() != Term.FINAL)
                .map(AcademicRecord::getGrade)
                .toList();
        if (termGrades.isEmpty()) {
            throw new BusinessException("No hay notas de períodos para calcular la nota final.");
        }
        BigDecimal finalGrade = average(termGrades);
        recordGrade(studentId, subjectId, academicYear, Term.FINAL, finalGrade, "Promedio de " + termGrades.size() + " períodos");
        return finalGrade;
    }

    /** Average of each subject for one student in a year (terms I-IV, FINAL excluded). */
    public List<SubjectAverage> averageBySubject(int studentId, int academicYear) {
        Map<Integer, List<BigDecimal>> gradesBySubject = recordDao.findByStudent(studentId).stream()
                .filter(r -> r.getAcademicYear() == academicYear && r.getTerm() != Term.FINAL)
                .collect(Collectors.groupingBy(AcademicRecord::getSubjectId,
                        Collectors.mapping(AcademicRecord::getGrade, Collectors.toList())));
        return toSubjectAverages(gradesBySubject);
    }

    /** General average of the student in one term (all subjects). Empty when he has no grades. */
    public Optional<BigDecimal> termAverage(int studentId, int academicYear, Term term) {
        List<BigDecimal> grades = recordDao.findByStudent(studentId).stream()
                .filter(r -> r.getAcademicYear() == academicYear && r.getTerm() == term)
                .map(AcademicRecord::getGrade)
                .toList();
        return grades.isEmpty() ? Optional.empty() : Optional.of(average(grades));
    }

    /** Average of the whole course (grade level) per subject in a year. */
    public List<SubjectAverage> courseAverageBySubject(int gradeLevelId, int academicYear) {
        Map<Integer, List<BigDecimal>> gradesBySubject = subjectDao.findByGrade(gradeLevelId).stream()
                .flatMap(subject -> recordDao.findBySubject(subject.getId(), academicYear).stream())
                .filter(r -> r.getTerm() != Term.FINAL)
                .collect(Collectors.groupingBy(AcademicRecord::getSubjectId,
                        Collectors.mapping(AcademicRecord::getGrade, Collectors.toList())));
        return toSubjectAverages(gradesBySubject);
    }

    /**
     * Complete academic history: year, then term, then the grades of that term.
     * TreeMap keeps years and terms ordered.
     */
    public Map<Integer, Map<Term, List<AcademicRecord>>> academicHistory(int studentId) {
        return recordDao.findByStudent(studentId).stream()
                .collect(Collectors.groupingBy(AcademicRecord::getAcademicYear, TreeMap::new,
                        Collectors.groupingBy(AcademicRecord::getTerm, TreeMap::new, Collectors.toList())));
    }

    /** Lookup table id -> subject, used by views to show names instead of ids. */
    public Map<Integer, Subject> subjectsById() {
        return subjectDao.findAll().stream().collect(Collectors.toMap(Subject::getId, Function.identity()));
    }

    // ---------- helpers ----------

    /** Average rounded to one decimal, like the DECIMAL(2,1) column. */
    public static BigDecimal average(List<BigDecimal> values) {
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 1, RoundingMode.HALF_UP);
    }

    private List<SubjectAverage> toSubjectAverages(Map<Integer, List<BigDecimal>> gradesBySubject) {
        Map<Integer, Subject> subjects = subjectsById();
        return gradesBySubject.entrySet().stream()
                .map(entry -> new SubjectAverage(entry.getKey(),
                        Optional.ofNullable(subjects.get(entry.getKey())).map(Subject::getName).orElse("?"),
                        average(entry.getValue()), entry.getValue().size()))
                .sorted(Comparator.comparing(SubjectAverage::subjectName))
                .toList();
    }

    private Enrollment activeEnrollment(int studentId, int academicYear) {
        return enrollmentDao.findByStudentAndYear(studentId, academicYear)
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("El estudiante no tiene matrícula activa en " + academicYear + "."));
    }
}
