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
 * Estadísticas y reportes. Cada método es un "pipeline" de Streams: filtrar, transformar, agrupar, promediar y ordenar.
 * <p>Este servicio solo LEE datos de los DAO y los procesa en memoria con lambdas; no guarda nada.</p>
 */
public class ReportService {

    // Escala nacional de desempeño (Decreto 1290 de 2009) usada para clasificar las notas
    public static final String LOW = "Bajo (0.0 - 2.9)";
    public static final String BASIC = "Básico (3.0 - 3.9)";
    public static final String HIGH = "Alto (4.0 - 4.5)";
    public static final String SUPERIOR = "Superior (4.6 - 5.0)";

    // DAO de los que se leen los datos (interfaces entregadas por el constructor)
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

    // ====================== TABLERO DEL MENÚ PRINCIPAL ======================

    /** Cifras que se muestran arriba del menú principal. */
    public DashboardStats dashboard(LocalDate today) {
        long students = studentDao.findAll().stream().filter(s -> s.getStatus() == StudentStatus.ACTIVE).count();
        long teachers = teacherDao.findAll().stream().filter(t -> t.getStatus() == TeacherStatus.ACTIVE).count();
        long clubs = clubDao.findAll().stream().filter(c -> c.isActive()).count();
        // Recaudo del mes: suma de los pagos cuyo año y mes coinciden con hoy
        BigDecimal income = paymentDao.findAll().stream()
                .filter(p -> p.getPaymentDate().getYear() == today.getYear() && p.getPaymentDate().getMonth() == today.getMonth())
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);      // reduce suma todos los valores empezando desde cero
        long pending = chargeDao.findPending().size();
        long loans = studentLoanDao.findOpen().size() + teacherLoanDao.findOpen().size();
        return new DashboardStats(students, teachers, clubs, income, pending, loans);
    }

    // ====================== REPORTES ACADÉMICOS ======================

    /**
     * Mejores estudiantes del año por promedio general (períodos I a IV).
     *
     * @param limit cuántos estudiantes devolver
     */
    public List<StudentRanking> topStudents(int academicYear, int limit) {
        Map<Integer, Student> students = studentsById();
        Map<Integer, String> gradeOfStudent = gradeNameByStudent(academicYear);
        AtomicInteger position = new AtomicInteger();       // contador para numerar el puesto 1, 2, 3...
        return recordDao.findByYear(academicYear).stream()
                .filter(r -> r.getTerm() != Term.FINAL)
                // agrupa todas las notas de cada estudiante: Map<id estudiante, lista de notas>
                .collect(Collectors.groupingBy(AcademicRecord::getStudentId,
                        Collectors.mapping(AcademicRecord::getGrade, Collectors.toList())))
                .entrySet().stream()
                // promedio con 2 decimales para poder desempatar
                .map(entry -> Map.entry(entry.getKey(), AcademicService.average(entry.getValue(), 2)))
                // mayor promedio primero; si empatan, por nombre
                .sorted(Map.Entry.<Integer, BigDecimal>comparingByValue().reversed()
                        .thenComparing(entry -> students.get(entry.getKey()).getFullName()))
                .limit(limit)                                // solo los primeros N
                .map(entry -> new StudentRanking(position.incrementAndGet(), entry.getKey(),
                        students.get(entry.getKey()).getFullName(), gradeOfStudent.getOrDefault(entry.getKey(), "-"),
                        entry.getValue()))
                .toList();
    }

    /** Estudiantes con al menos una materia con promedio inferior a 3.0, junto con esas materias. */
    public Map<String, List<SubjectAverage>> studentsAtRisk(int academicYear) {
        Map<Integer, Student> students = studentsById();
        Map<Integer, Subject> subjects = subjectsById();
        return recordDao.findByYear(academicYear).stream()
                .filter(r -> r.getTerm() != Term.FINAL)
                // agrupación en dos niveles: estudiante -> materia -> lista de notas
                .collect(Collectors.groupingBy(AcademicRecord::getStudentId,
                        Collectors.groupingBy(AcademicRecord::getSubjectId,
                                Collectors.mapping(AcademicRecord::getGrade, Collectors.toList()))))
                .entrySet().stream()
                .map(student -> Map.entry(students.get(student.getKey()).getFullName(),
                        student.getValue().entrySet().stream()
                                // promedio de cada materia del estudiante
                                .map(subject -> new SubjectAverage(subject.getKey(), subjects.get(subject.getKey()).getName(),
                                        AcademicService.average(subject.getValue()), subject.getValue().size()))
                                // se queda solo con las materias reprobadas (promedio menor que 3.0)
                                .filter(average -> average.average().compareTo(new BigDecimal("3.0")) < 0)
                                .toList()))
                .filter(entry -> !entry.getValue().isEmpty())     // descarta a los estudiantes sin materias en riesgo
                // TreeMap ordena el resultado alfabéticamente por nombre de estudiante
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, TreeMap::new));
    }

    /** Cuántas notas caen en cada nivel de la escala nacional de desempeño. */
    public Map<String, Long> performanceDistribution(int academicYear) {
        // groupingBy + counting: cuenta las notas de cada nivel
        Map<String, Long> counts = recordDao.findByYear(academicYear).stream()
                .filter(r -> r.getTerm() != Term.FINAL)
                .collect(Collectors.groupingBy(r -> performanceLevel(r.getGrade()), Collectors.counting()));
        // LinkedHashMap conserva el orden de inserción: de Superior a Bajo, incluyendo los niveles con cero notas
        Map<String, Long> ordered = new LinkedHashMap<>();
        for (String level : List.of(SUPERIOR, HIGH, BASIC, LOW)) {
            ordered.put(level, counts.getOrDefault(level, 0L));
        }
        return ordered;
    }

    /** Promedio de cada grado (curso) en el año, de Preescolar a Once. */
    public Map<String, BigDecimal> averageByGradeLevel(int academicYear) {
        // Tabla estudiante -> grado en el que está matriculado ese año
        Map<Integer, Integer> gradeLevelOfStudent = enrollmentDao.findAll().stream()
                .filter(e -> e.getAcademicYear() == academicYear)
                .collect(Collectors.toMap(Enrollment::getStudentId, Enrollment::getGradeLevelId, (a, b) -> a));
        // Notas agrupadas por grado
        Map<Integer, List<BigDecimal>> gradesByLevel = recordDao.findByYear(academicYear).stream()
                .filter(r -> r.getTerm() != Term.FINAL && gradeLevelOfStudent.containsKey(r.getStudentId()))
                .collect(Collectors.groupingBy(r -> gradeLevelOfStudent.get(r.getStudentId()),
                        Collectors.mapping(AcademicRecord::getGrade, Collectors.toList())));
        return gradeLevelDao.findAll().stream()
                .sorted(Comparator.comparingInt(GradeLevel::getNumericLevel))    // de Preescolar a Once
                .filter(level -> gradesByLevel.containsKey(level.getId()))       // solo grados que tienen notas
                .collect(Collectors.toMap(GradeLevel::getName, level -> AcademicService.average(gradesByLevel.get(level.getId())),
                        (a, b) -> a, LinkedHashMap::new));
    }

    // ====================== REPORTES FINANCIEROS ======================

    /** Ingresos del año agrupados por concepto, de mayor a menor. */
    public Map<String, BigDecimal> incomeByConcept(int year) {
        Map<Integer, String> concepts = conceptDao.findAll().stream()
                .collect(Collectors.toMap(PaymentConcept::getId, PaymentConcept::getName));
        return paymentsOfYear(year).stream()
                // agrupa por nombre de concepto y SUMA los valores de cada grupo con reducing
                .collect(Collectors.groupingBy(p -> concepts.get(p.getConceptId()),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())     // el que más recaudó primero
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    /** Ingresos del año por mes (EnumMap mantiene el orden de enero a diciembre). */
    public Map<Month, BigDecimal> incomeByMonth(int year) {
        return paymentsOfYear(year).stream()
                .collect(Collectors.groupingBy(p -> p.getPaymentDate().getMonth(), () -> new EnumMap<>(Month.class),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));
    }

    /** Número de pagos por método de pago. */
    public Map<PaymentMethod, Long> paymentsByMethod(int year) {
        return paymentsOfYear(year).stream()
                .collect(Collectors.groupingBy(Payment::getMethod, () -> new EnumMap<>(PaymentMethod.class),
                        Collectors.counting()));
    }

    // ====================== OTROS REPORTES ======================

    /** Materias y bloques semanales de cada profesor, del más ocupado al menos ocupado. */
    public List<TeacherLoad> teacherLoad(int academicYear) {
        // Asignaciones del año agrupadas por profesor
        Map<Integer, List<TeacherAssignment>> byTeacher = assignmentDao.findAll().stream()
                .filter(a -> a.getAcademicYear() == academicYear)
                .collect(Collectors.groupingBy(TeacherAssignment::getTeacherId));
        return teacherDao.findAll().stream()
                .filter(t -> byTeacher.containsKey(t.getId()))        // solo profesores con materias asignadas
                .map(t -> new TeacherLoad(t.getFullName(), t.getSpecialization(), byTeacher.get(t.getId()).size(),
                        scheduleDao.findByTeacher(t.getId()).size()))
                .sorted(Comparator.comparingLong(TeacherLoad::weeklyBlocks).reversed()    // más bloques primero
                        .thenComparing(TeacherLoad::teacherName))                         // y a igualdad, por nombre
                .toList();
    }

    /** Medallero de los eventos con competencia (primero oro, luego plata y luego bronce). */
    public List<MedalCount> medalTable() {
        Map<Integer, Student> students = studentsById();
        return participationDao.findAll().stream()
                // solo quienes quedaron en el podio (1°, 2° o 3°)
                .filter(p -> p.getPosition() != null && p.getPosition() != EventPosition.PARTICIPANT)
                .collect(Collectors.groupingBy(EventParticipation::getStudentId))      // participaciones por estudiante
                .entrySet().stream()
                .map(entry -> new MedalCount(students.get(entry.getKey()).getFullName(),
                        countPosition(entry.getValue(), EventPosition.FIRST_PLACE),
                        countPosition(entry.getValue(), EventPosition.SECOND_PLACE),
                        countPosition(entry.getValue(), EventPosition.THIRD_PLACE)))
                // orden: más oros, luego más platas, luego más bronces (reversed invierte todo el orden compuesto)
                .sorted(Comparator.comparingLong(MedalCount::gold)
                        .thenComparingLong(MedalCount::silver)
                        .thenComparingLong(MedalCount::bronze).reversed())
                .toList();
    }

    /** Cantidad de recursos en cada estado (Disponible, Prestado...). */
    public Map<ResourceStatus, Long> resourcesByStatus() {
        return resourceDao.findAll().stream()
                .collect(Collectors.groupingBy(Resource::getStatus, () -> new EnumMap<>(ResourceStatus.class),
                        Collectors.counting()));
    }

    /** Estudiantes matriculados en cada grado durante el año. */
    public Map<String, Long> studentsByGradeLevel(int academicYear) {
        Map<Integer, String> names = gradeLevelDao.findAll().stream()
                .collect(Collectors.toMap(GradeLevel::getId, GradeLevel::getName));
        // Cuenta las matrículas del año por grado
        Map<Integer, Long> counts = enrollmentDao.findAll().stream()
                .filter(e -> e.getAcademicYear() == academicYear)
                .collect(Collectors.groupingBy(Enrollment::getGradeLevelId, Collectors.counting()));
        return gradeLevelDao.findAll().stream()
                .sorted(Comparator.comparingInt(GradeLevel::getNumericLevel))
                // getOrDefault pone 0 en los grados que no tienen matrículas
                .collect(Collectors.toMap(level -> names.get(level.getId()), level -> counts.getOrDefault(level.getId(), 0L),
                        (a, b) -> a, LinkedHashMap::new));
    }

    // ====================== AYUDAS ======================

    /** Clasifica una nota en la escala nacional: Bajo, Básico, Alto o Superior. */
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

    /** Cuenta cuántas participaciones tienen una posición determinada. */
    private long countPosition(List<EventParticipation> participations, EventPosition position) {
        return participations.stream().filter(p -> p.getPosition() == position).count();
    }

    /** Pagos recibidos durante un año. */
    private List<Payment> paymentsOfYear(int year) {
        return paymentDao.findAll().stream().filter(p -> p.getPaymentDate().getYear() == year).toList();
    }

    /** Tabla de búsqueda id -> estudiante. */
    private Map<Integer, Student> studentsById() {
        return studentDao.findAll().stream().collect(Collectors.toMap(Student::getId, Function.identity()));
    }

    /** Tabla de búsqueda id -> materia. */
    private Map<Integer, Subject> subjectsById() {
        return subjectDao.findAll().stream().collect(Collectors.toMap(Subject::getId, Function.identity()));
    }

    /** Tabla estudiante -> nombre del grado en el que está matriculado ese año. */
    private Map<Integer, String> gradeNameByStudent(int academicYear) {
        Map<Integer, String> gradeNames = gradeLevelDao.findAll().stream()
                .collect(Collectors.toMap(GradeLevel::getId, GradeLevel::getName));
        return enrollmentDao.findAll().stream()
                .filter(e -> e.getAcademicYear() == academicYear)
                .collect(Collectors.toMap(Enrollment::getStudentId, e -> gradeNames.get(e.getGradeLevelId()), (a, b) -> a));
    }

    /** Tabla de búsqueda id -> profesor, para las vistas que la necesiten. */
    public Map<Integer, Teacher> teachersById() {
        return teacherDao.findAll().stream().collect(Collectors.toMap(Teacher::getId, Function.identity()));
    }
}
