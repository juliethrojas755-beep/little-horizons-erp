package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.LoanDao;
import com.littlehorizons.erp.dao.ResourceCategoryDao;
import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.dao.TransactionManager;
import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.model.dto.OverdueLoan;
import com.littlehorizons.erp.model.enums.LoanStatus;
import com.littlehorizons.erp.model.enums.ResourceStatus;
import com.littlehorizons.erp.model.enums.StudentStatus;
import com.littlehorizons.erp.model.enums.TeacherStatus;
import com.littlehorizons.erp.util.BusinessException;
import com.littlehorizons.erp.util.Validator;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Catálogo de recursos (biblioteca y laboratorios) y préstamos a estudiantes y profesores.
 */
public class ResourceService {

    /** Un estudiante no puede tener más de este número de recursos prestados al mismo tiempo. */
    public static final int MAX_OPEN_LOANS_PER_STUDENT = 3;
    /** Máximo de días que puede durar un préstamo. */
    public static final int MAX_LOAN_DAYS = 30;

    private final ResourceDao resourceDao;
    private final ResourceCategoryDao categoryDao;
    // Los dos DAO de préstamos comparten la misma interfaz genérica LoanDao<T>
    private final LoanDao<StudentLoan> studentLoanDao;
    private final LoanDao<TeacherLoan> teacherLoanDao;
    private final StudentDao studentDao;
    private final TeacherDao teacherDao;
    // Permite guardar el préstamo y cambiar el estado del recurso en una sola transacción
    private final TransactionManager transactions;

    public ResourceService(ResourceDao resourceDao, ResourceCategoryDao categoryDao, LoanDao<StudentLoan> studentLoanDao,
                           LoanDao<TeacherLoan> teacherLoanDao, StudentDao studentDao, TeacherDao teacherDao,
                           TransactionManager transactions) {
        this.resourceDao = resourceDao;
        this.categoryDao = categoryDao;
        this.studentLoanDao = studentLoanDao;
        this.teacherLoanDao = teacherLoanDao;
        this.studentDao = studentDao;
        this.teacherDao = teacherDao;
        this.transactions = transactions;
    }

    // ====================== CATÁLOGO ======================

    /** Registra un recurso nuevo en el inventario; el código debe ser único. */
    public int registerResource(Resource resource) {
        if (!Validator.isNotBlank(resource.getCode()) || !Validator.isNotBlank(resource.getName())) {
            throw new BusinessException("El código y el nombre del recurso son obligatorios.");
        }
        if (resourceDao.findByCode(resource.getCode()).isPresent()) {
            throw new BusinessException("Ya existe un recurso con el código " + resource.getCode() + ".");
        }
        resource.setStatus(ResourceStatus.AVAILABLE);      // un recurso nuevo empieza Disponible
        return resourceDao.insert(resource);
    }

    /** Cambio manual de estado (mantenimiento, baja). El estado Prestado solo cambia al prestar o devolver. */
    public void changeStatus(String code, ResourceStatus status) {
        Resource resource = getByCode(code);
        if (resource.getStatus() == ResourceStatus.LOANED || status == ResourceStatus.LOANED) {
            throw new BusinessException("El estado Prestado solo cambia al prestar o devolver el recurso.");
        }
        resource.setStatus(status);
        resourceDao.update(resource);
    }

    public List<Resource> findAll() {
        return resourceDao.findAll();
    }

    public List<Resource> findByStatus(ResourceStatus status) {
        return resourceDao.findByStatus(status);
    }

    public Resource getByCode(String code) {
        return resourceDao.findByCode(code)
                .orElseThrow(() -> new BusinessException("No existe un recurso con el código " + code + "."));
    }

    public List<ResourceCategory> getCategories() {
        return categoryDao.findAll();
    }

    public Map<Integer, ResourceCategory> categoriesById() {
        return categoryDao.findAll().stream().collect(Collectors.toMap(ResourceCategory::getId, Function.identity()));
    }

    // ====================== PRÉSTAMOS ======================

    /** Presta un recurso a un estudiante por cierto número de días. */
    public int loanToStudent(String resourceCode, int studentId, int days) {
        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new BusinessException("No existe un estudiante con id " + studentId + "."));
        // Regla 1: solo estudiantes activos
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException("Solo los estudiantes activos pueden recibir préstamos.");
        }
        // Regla 2: máximo 3 préstamos abiertos (sin fecha de devolución) al mismo tiempo
        long openLoans = studentLoanDao.findByBorrower(studentId).stream().filter(l -> l.getReturnDate() == null).count();
        if (openLoans >= MAX_OPEN_LOANS_PER_STUDENT) {
            throw new BusinessException(student.getFullName() + " ya tiene " + openLoans + " préstamos abiertos (máximo "
                    + MAX_OPEN_LOANS_PER_STUDENT + ").");
        }
        // Regla 3: no puede pedir otro recurso si tiene alguno vencido
        boolean hasOverdue = studentLoanDao.findByBorrower(studentId).stream().anyMatch(l -> l.isOverdue(LocalDate.now()));
        if (hasOverdue) {
            throw new BusinessException(student.getFullName() + " tiene préstamos vencidos. Debe devolverlos primero.");
        }
        StudentLoan loan = new StudentLoan();
        loan.setStudentId(studentId);
        return createLoan(resourceCode, days, loan, studentLoanDao);
    }

    /** Presta un recurso a un profesor por cierto número de días. */
    public int loanToTeacher(String resourceCode, int teacherId, int days) {
        Teacher teacher = teacherDao.findById(teacherId)
                .orElseThrow(() -> new BusinessException("No existe un profesor con id " + teacherId + "."));
        if (teacher.getStatus() != TeacherStatus.ACTIVE) {
            throw new BusinessException("Solo los profesores activos pueden recibir préstamos.");
        }
        TeacherLoan loan = new TeacherLoan();
        loan.setTeacherId(teacherId);
        return createLoan(resourceCode, days, loan, teacherLoanDao);
    }

    /** Devolución de un préstamo de estudiante: el recurso vuelve a quedar Disponible. */
    public void returnStudentLoan(int loanId) {
        returnLoan(loanId, studentLoanDao);
    }

    /** Devolución de un préstamo de profesor. */
    public void returnTeacherLoan(int loanId) {
        returnLoan(loanId, teacherLoanDao);
    }

    public List<StudentLoan> openStudentLoans() {
        studentLoanDao.refreshOverdue(LocalDate.now());   // antes de listar se marcan como Vencidos los que ya pasaron la fecha
        return studentLoanDao.findOpen();
    }

    public List<TeacherLoan> openTeacherLoans() {
        teacherLoanDao.refreshOverdue(LocalDate.now());
        return teacherLoanDao.findOpen();
    }

    /**
     * Préstamos vencidos de estudiantes Y profesores en una sola lista, del más atrasado al menos atrasado.
     * Antes de leer, el estado Activo pasa a Vencido cuando la fecha límite ya pasó.
     */
    public List<OverdueLoan> overdueLoans(LocalDate today) {
        studentLoanDao.refreshOverdue(today);
        teacherLoanDao.refreshOverdue(today);
        // Tablas de búsqueda id -> dato para mostrar nombres en el reporte
        Map<Integer, Resource> resources = resourceDao.findAll().stream()
                .collect(Collectors.toMap(Resource::getId, Function.identity()));
        Map<Integer, String> students = studentDao.findAll().stream()
                .collect(Collectors.toMap(Student::getId, Student::getFullName));
        Map<Integer, String> teachers = teacherDao.findAll().stream()
                .collect(Collectors.toMap(Teacher::getId, Teacher::getFullName));

        // Se convierte cada lista de préstamos al mismo tipo OverdueLoan y se juntan con Stream.concat
        Stream<OverdueLoan> studentOverdue = toOverdue(studentLoanDao.findOpen(), "Estudiante", students::get, resources, today);
        Stream<OverdueLoan> teacherOverdue = toOverdue(teacherLoanDao.findOpen(), "Profesor", teachers::get, resources, today);
        return Stream.concat(studentOverdue, teacherOverdue)
                .sorted(Comparator.comparingLong(OverdueLoan::daysOverdue).reversed())
                .toList();
    }

    // ====================== LÓGICA COMPARTIDA POR LOS DOS TIPOS DE PRÉSTAMO ======================

    /**
     * Crea un préstamo (de estudiante o de profesor) y marca el recurso como Prestado, todo en una transacción.
     * Es genérico: funciona con cualquier subclase de Loan.
     */
    private <T extends Loan> int createLoan(String resourceCode, int days, T loan, LoanDao<T> dao) {
        // La duración debe estar entre 1 y 30 días
        if (days < 1 || days > MAX_LOAN_DAYS) {
            throw new BusinessException("El préstamo debe ser de 1 a " + MAX_LOAN_DAYS + " días.");
        }
        Resource resource = getByCode(resourceCode);
        // Solo se presta lo que está Disponible
        if (!resource.isAvailable()) {
            throw new BusinessException("El recurso " + resource.getName() + " no está disponible (estado: "
                    + resource.getStatus() + ").");
        }
        loan.setResourceId(resource.getId());
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(days));      // fecha límite = hoy + días del préstamo
        loan.setStatus(LoanStatus.ACTIVE);
        // Transacción: o se guardan las dos cosas (préstamo y cambio de estado) o ninguna
        return transactions.inTransaction(connection -> {
            int id = dao.insert(connection, loan);
            resourceDao.updateStatus(connection, resource.getId(), ResourceStatus.LOANED);
            return id;
        });
    }

    /** Registra la devolución: marca el préstamo como Devuelto y el recurso como Disponible (en una transacción). */
    private <T extends Loan> void returnLoan(int loanId, LoanDao<T> dao) {
        T loan = dao.findById(loanId).orElseThrow(() -> new BusinessException("No existe el préstamo " + loanId + "."));
        // No se puede devolver dos veces
        if (loan.getReturnDate() != null) {
            throw new BusinessException("Ese préstamo ya fue devuelto el " + loan.getReturnDate() + ".");
        }
        transactions.inTransaction(connection -> {
            dao.markReturned(connection, loanId, LocalDate.now());
            resourceDao.updateStatus(connection, loan.getResourceId(), ResourceStatus.AVAILABLE);
            return null;
        });
    }

    /** Convierte los préstamos vencidos de un tipo (estudiante o profesor) en objetos OverdueLoan para el reporte. */
    private <T extends Loan> Stream<OverdueLoan> toOverdue(List<T> loans, String type, IntFunction<String> names,
                                                          Map<Integer, Resource> resources, LocalDate today) {
        return loans.stream()
                .filter(loan -> loan.isOverdue(today))           // solo los que ya pasaron la fecha límite
                .map(loan -> {
                    Resource resource = resources.get(loan.getResourceId());
                    // días de retraso = días entre la fecha límite y hoy
                    return new OverdueLoan(loan.getId(), type, names.apply(loan.getBorrowerId()), resource.getCode(),
                            resource.getName(), loan.getDueDate(), ChronoUnit.DAYS.between(loan.getDueDate(), today));
                });
    }
}
