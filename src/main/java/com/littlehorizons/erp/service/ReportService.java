package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.dao.ChargeDao;
import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.EventParticipationDao;
import com.littlehorizons.erp.dao.GradeLevelDao;
import com.littlehorizons.erp.dao.LoanDao;
import com.littlehorizons.erp.dao.PaymentConceptDao;
import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.ScheduleDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.TeacherAssignmentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.model.dto.DashboardStats;
import com.littlehorizons.erp.model.dto.MedalCount;
import com.littlehorizons.erp.model.dto.StudentRanking;
import com.littlehorizons.erp.model.dto.SubjectAverage;
import com.littlehorizons.erp.model.dto.TeacherLoad;
import com.littlehorizons.erp.model.enums.EventPosition;
import com.littlehorizons.erp.model.enums.PaymentMethod;
import com.littlehorizons.erp.model.enums.ResourceStatus;
import com.littlehorizons.erp.model.enums.StudentStatus;
import com.littlehorizons.erp.model.enums.TeacherStatus;
import com.littlehorizons.erp.model.enums.Term;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Statistics and reports. Every method is a Stream pipeline: filter, map, group, average, sort.
 */
public class ReportService {

    /** Colombian performance scale (Decreto 1290 de 2009). */
    public static final String LOW = "Bajo (0.0 - 2.9)";
    public static final String BASIC = "Básico (3.0 - 3.9)";
    public static final String HIGH = "Alto (4.0 - 4.5)";
    public static final String SUPERIOR = "Superior (4.6 - 5.0)";

    private final StudentDao studentDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeLevelDao gradeLevelDao;
    private final AcademicRecordDao recordDao;
    private final SubjectDao subjectDao;
    private final PaymentDao paymentDao;
    private final PaymentConceptDao conceptDao;
    private final ChargeDao chargeDao;
    private final TeacherDao teacherDao;
    private final TeacherAssignmentDao assignmentDao;
    private final ScheduleDao scheduleDao;
    private final EventParticipationDao participationDao;
    private final ResourceDao resourceDao;
    private final ClubDao clubDao;
    private final LoanDao<StudentLoan> studentLoanDao;
    private final LoanDao<TeacherLoan> teacherLoanDao;

    public ReportService(StudentDao studentDao, EnrollmentDao enrollmentDao, GradeLevelDao gradeLevelDao,
                         AcademicRecordDao recordDao, SubjectDao subjectDao, PaymentDao paymentDao,
                         PaymentConceptDao conceptDao, ChargeDao chargeDao, TeacherDao teacherDao,
                         TeacherAssignmentDao assignmentDao, ScheduleDao scheduleDao,
                         EventParticipationDao participationDao, ResourceDao resourceDao, ClubDao clubDao,
                         LoanDao<StudentLoan> studentLoanDao, LoanDao<TeacherLoan> teacherLoanDao) {
        this.studentDao = studentDao;
        this.enrollmentDao = enrollmentDao;
        this.gradeLevelDao = gradeLevelDao;
        this.recordDao = recordDao;
        this.subjectDao = subjectDao;
        this.paymentDao = paymentDao;
        this.conceptDao = conceptDao;
        this.chargeDao = chargeDao;
        this.teacherDao = teacherDao;
        this.assignmentDao = assignmentDao;
        this.scheduleDao = scheduleDao;
        this.participationDao = participationDao;
        this.resourceDao = resourceDao;
        this.clubDao = clubDao;
        this.studentLoanDao = studentLoanDao;
        this.teacherLoanDao = teacherLoanDao;
    }

    /** Numbers for the dashboard of the main menu. */
    public DashboardStats dashboard(LocalDate today) {
        long students = studentDao.findAll().stream().filter(s -> s.getStatus() == StudentStatus.ACTIVE).count();
        long teachers = teacherDao.findAll().stream().filter(t -> t.getStatus() == TeacherStatus.ACTIVE).count();
        long clubs = clubDao.findAll().stream().filter(c -> c.isActive()).count();
        BigDecimal income = paymentDao.findAll().stream()
                .filter(p -> p.getPaymentDate().getYear() == today.getYear() && p.getPaymentDate().getMonth() == today.getMonth())
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pending = chargeDao.findPending().size();
        long loans = studentLoanDao.findOpen().size() + teacherLoanDao.findOpen().size();
        return new DashboardStats(students, teachers, clubs, income, pending, loans);
    }

    /**
     * Best students of the year by general average (terms I to IV).
     *
     * @param limit how many to return
     */
    public List<StudentRanking> topStudents(int academicYear, int limit) {
        Map<Integer, Student> students = studentsById();
        Map<Integer, String> gradeOfStudent = gradeNameByStudent(academicYear);
        AtomicInteger position = new AtomicInteger();
        return recordDao.findByYear(academicYear).stream()
                .filter(r -> r.getTerm() != Term.FINAL)
                .collect(Collectors.groupingBy(AcademicRecord::getStudentId,
                        Collectors.mapping(AcademicRecord::getGrade, Collectors.toList())))
                .entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), AcademicService.average(entry.getValue())))
                .sorted(Map.Entry.<Integer, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new StudentRanking(position.incrementAndGet(), entry.getKey(),
                        students.get(entry.getKey()).getFullName(), gradeOfStudent.getOrDefault(entry.getKey(), "-"),
                        entry.getValue()))
                .toList();
    }

    /** Students with at least one subject average below 3.0, with the failed subjects. */
    public Map<String, List<SubjectAverage>> studentsAtRisk(int academicYear) {
        Map<Integer, Student> students = studentsById();
        Map<Integer, Subject> subjects = subjectsById();
        return recordDao.findByYear(academicYear).stream()
                .filter(r -> r.getTerm() != Term.FINAL)
                .collect(Collectors.groupingBy(AcademicRecord::getStudentId,
                        Collectors.groupingBy(AcademicRecord::getSubjectId,
                                Collectors.mapping(AcademicRecord::getGrade, Collectors.toList()))))
                .entrySet().stream()
                .map(student -> Map.entry(students.get(student.getKey()).getFullName(),
                        student.getValue().entrySet().stream()
                                .map(subject -> new SubjectAverage(subject.getKey(), subjects.get(subject.getKey()).getName(),
                                        AcademicService.average(subject.getValue()), subject.getValue().size()))
                                .filter(average -> average.average().compareTo(new BigDecimal("3.0")) < 0)
                                .toList()))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, TreeMap::new));
    }

    /** How many grades fall in each level of the Colombian scale. */
    public Map<String, Long> performanceDistribution(int academicYear) {
        Map<String, Long> counts = recordDao.findByYear(academicYear).stream()
                .filter(r -> r.getTerm() != Term.FINAL)
                .collect(Collectors.groupingBy(r -> performanceLevel(r.getGrade()), Collectors.counting()));
        Map<String, Long> ordered = new LinkedHashMap<>();
        for (String level : List.of(SUPERIOR, HIGH, BASIC, LOW)) {
            ordered.put(level, counts.getOrDefault(level, 0L));
        }
        return ordered;
    }

    /** Average of every grade level (course) in the year, from Preescolar to Once. */
    public Map<String, BigDecimal> averageByGradeLevel(int academicYear) {
        Map<Integer, Integer> gradeLevelOfStudent = enrollmentDao.findAll().stream()
                .filter(e -> e.getAcademicYear() == academicYear)
                .collect(Collectors.toMap(Enrollment::getStudentId, Enrollment::getGradeLevelId, (a, b) -> a));
        Map<Integer, List<BigDecimal>> gradesByLevel = recordDao.findByYear(academicYear).stream()
                .filter(r -> r.getTerm() != Term.FINAL && gradeLevelOfStudent.containsKey(r.getStudentId()))
                .collect(Collectors.groupingBy(r -> gradeLevelOfStudent.get(r.getStudentId()),
                        Collectors.mapping(AcademicRecord::getGrade, Collectors.toList())));
        return gradeLevelDao.findAll().stream()
                .sorted(Comparator.comparingInt(GradeLevel::getNumericLevel))
                .filter(level -> gradesByLevel.containsKey(level.getId()))
                .collect(Collectors.toMap(GradeLevel::getName, level -> AcademicService.average(gradesByLevel.get(level.getId())),
                        (a, b) -> a, LinkedHashMap::new));
    }

    /** Income of the year grouped by concept, highest first. */
    public Map<String, BigDecimal> incomeByConcept(int year) {
        Map<Integer, String> concepts = conceptDao.findAll().stream()
                .collect(Collectors.toMap(PaymentConcept::getId, PaymentConcept::getName));
        return paymentsOfYear(year).stream()
                .collect(Collectors.groupingBy(p -> concepts.get(p.getConceptId()),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    /** Income of the year per month (EnumMap keeps January to December order). */
    public Map<Month, BigDecimal> incomeByMonth(int year) {
        return paymentsOfYear(year).stream()
                .collect(Collectors.groupingBy(p -> p.getPaymentDate().getMonth(), () -> new EnumMap<>(Month.class),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));
    }

    /** Number of payments per method. */
    public Map<PaymentMethod, Long> paymentsByMethod(int year) {
        return paymentsOfYear(year).stream()
                .collect(Collectors.groupingBy(Payment::getMethod, () -> new EnumMap<>(PaymentMethod.class),
                        Collectors.counting()));
    }

    /** Subjects and weekly blocks of each teacher, busiest first. */
    public List<TeacherLoad> teacherLoad(int academicYear) {
        Map<Integer, List<TeacherAssignment>> byTeacher = assignmentDao.findAll().stream()
                .filter(a -> a.getAcademicYear() == academicYear)
                .collect(Collectors.groupingBy(TeacherAssignment::getTeacherId));
        return teacherDao.findAll().stream()
                .filter(t -> byTeacher.containsKey(t.getId()))
                .map(t -> new TeacherLoad(t.getFullName(), t.getSpecialization(), byTeacher.get(t.getId()).size(),
                        scheduleDao.findByTeacher(t.getId()).size()))
                .sorted(Comparator.comparingLong(TeacherLoad::weeklyBlocks).reversed()
                        .thenComparing(TeacherLoad::teacherName))
                .toList();
    }

    /** Medal table of competitive events (gold first, then silver, then bronze). */
    public List<MedalCount> medalTable() {
        Map<Integer, Student> students = studentsById();
        return participationDao.findAll().stream()
                .filter(p -> p.getPosition() != null && p.getPosition() != EventPosition.PARTICIPANT)
                .collect(Collectors.groupingBy(EventParticipation::getStudentId))
                .entrySet().stream()
                .map(entry -> new MedalCount(students.get(entry.getKey()).getFullName(),
                        countPosition(entry.getValue(), EventPosition.FIRST_PLACE),
                        countPosition(entry.getValue(), EventPosition.SECOND_PLACE),
                        countPosition(entry.getValue(), EventPosition.THIRD_PLACE)))
                .sorted(Comparator.comparingLong(MedalCount::gold)
                        .thenComparingLong(MedalCount::silver)
                        .thenComparingLong(MedalCount::bronze).reversed())
                .toList();
    }

    /** Count of resources in each status. */
    public Map<ResourceStatus, Long> resourcesByStatus() {
        return resourceDao.findAll().stream()
                .collect(Collectors.groupingBy(Resource::getStatus, () -> new EnumMap<>(ResourceStatus.class),
                        Collectors.counting()));
    }

    /** Students per grade level in the year. */
    public Map<String, Long> studentsByGradeLevel(int academicYear) {
        Map<Integer, String> names = gradeLevelDao.findAll().stream()
                .collect(Collectors.toMap(GradeLevel::getId, GradeLevel::getName));
        Map<Integer, Long> counts = enrollmentDao.findAll().stream()
                .filter(e -> e.getAcademicYear() == academicYear)
                .collect(Collectors.groupingBy(Enrollment::getGradeLevelId, Collectors.counting()));
        return gradeLevelDao.findAll().stream()
                .sorted(Comparator.comparingInt(GradeLevel::getNumericLevel))
                .collect(Collectors.toMap(level -> names.get(level.getId()), level -> counts.getOrDefault(level.getId(), 0L),
                        (a, b) -> a, LinkedHashMap::new));
    }

    // ---------- helpers ----------

    public static String performanceLevel(BigDecimal grade) {
        if (grade.compareTo(new BigDecimal("3.0")) < 0) {
            return LOW;
        } else if (grade.compareTo(new BigDecimal("4.0")) < 0) {
            return BASIC;
        } else if (grade.compareTo(new BigDecimal("4.6")) < 0) {
            return HIGH;
        }
        return SUPERIOR;
    }

    private long countPosition(List<EventParticipation> participations, EventPosition position) {
        return participations.stream().filter(p -> p.getPosition() == position).count();
    }

    private List<Payment> paymentsOfYear(int year) {
        return paymentDao.findAll().stream().filter(p -> p.getPaymentDate().getYear() == year).toList();
    }

    private Map<Integer, Student> studentsById() {
        return studentDao.findAll().stream().collect(Collectors.toMap(Student::getId, Function.identity()));
    }

    private Map<Integer, Subject> subjectsById() {
        return subjectDao.findAll().stream().collect(Collectors.toMap(Subject::getId, Function.identity()));
    }

    private Map<Integer, String> gradeNameByStudent(int academicYear) {
        Map<Integer, String> gradeNames = gradeLevelDao.findAll().stream()
                .collect(Collectors.toMap(GradeLevel::getId, GradeLevel::getName));
        return enrollmentDao.findAll().stream()
                .filter(e -> e.getAcademicYear() == academicYear)
                .collect(Collectors.toMap(Enrollment::getStudentId, e -> gradeNames.get(e.getGradeLevelId()), (a, b) -> a));
    }

    /** Lookup used by views that need the teacher of each id. */
    public Map<Integer, Teacher> teachersById() {
        return teacherDao.findAll().stream().collect(Collectors.toMap(Teacher::getId, Function.identity()));
    }
}
