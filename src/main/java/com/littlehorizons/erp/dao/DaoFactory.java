package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.dao.jdbc.AcademicRecordDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.AttendanceDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.ChargeDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.ClubDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.ClubMembershipDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.EnrollmentDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.EventParticipationDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.GradeLevelDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.GuardianDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.InvoiceDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.JdbcTransactionManager;
import com.littlehorizons.erp.dao.jdbc.PaymentConceptDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.PaymentDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.ResourceCategoryDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.ResourceDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.ScheduleDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.SchoolEventDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.StudentDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.StudentDiscountDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.StudentLoanDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.SubjectDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.SubjectEnrollmentDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.TeacherAssignmentDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.TeacherDaoJdbc;
import com.littlehorizons.erp.dao.jdbc.TeacherLoanDaoJdbc;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.TeacherLoan;

/**
 * Fábrica (patrón Factory) que crea las implementaciones de los DAO.
 * <p>
 * Los servicios solo conocen las INTERFACES (StudentDao...). Si mañana los datos vinieran de otra
 * fuente (otra base de datos, un archivo), solo cambia esta clase y no las demás
 * (Inversión de Dependencias + patrón Factory).
 * </p>
 */
public final class DaoFactory {

    // Constructor privado: es una clase de utilidad con métodos estáticos, no se instancia
    private DaoFactory() {
    }

    /** Crea el DAO de estudiantes. */
    public static StudentDao students() {
        return new StudentDaoJdbc();
    }

    /** Crea el DAO de acudientes. */
    public static GuardianDao guardians() {
        return new GuardianDaoJdbc();
    }

    /** Crea el DAO de profesores. */
    public static TeacherDao teachers() {
        return new TeacherDaoJdbc();
    }

    /** Crea el DAO de grados. */
    public static GradeLevelDao gradeLevels() {
        return new GradeLevelDaoJdbc();
    }

    /** Crea el DAO de matrículas. */
    public static EnrollmentDao enrollments() {
        return new EnrollmentDaoJdbc();
    }

    /** Crea el DAO de materias. */
    public static SubjectDao subjects() {
        return new SubjectDaoJdbc();
    }

    /** Crea el DAO de asignaciones profesor-materia. */
    public static TeacherAssignmentDao teacherAssignments() {
        return new TeacherAssignmentDaoJdbc();
    }

    /** Crea el DAO de horarios. */
    public static ScheduleDao schedules() {
        return new ScheduleDaoJdbc();
    }

    /** Crea el DAO de inscripciones en materias. */
    public static SubjectEnrollmentDao subjectEnrollments() {
        return new SubjectEnrollmentDaoJdbc();
    }

    /** Crea el DAO de calificaciones. */
    public static AcademicRecordDao academicRecords() {
        return new AcademicRecordDaoJdbc();
    }

    /** Crea el DAO de asistencia. */
    public static AttendanceDao attendance() {
        return new AttendanceDaoJdbc();
    }

    /** Crea el DAO de clubes. */
    public static ClubDao clubs() {
        return new ClubDaoJdbc();
    }

    /** Crea el DAO de membresías de clubes. */
    public static ClubMembershipDao clubMemberships() {
        return new ClubMembershipDaoJdbc();
    }

    /** Crea el DAO de eventos. */
    public static SchoolEventDao events() {
        return new SchoolEventDaoJdbc();
    }

    /** Crea el DAO de participantes de eventos. */
    public static EventParticipationDao eventParticipations() {
        return new EventParticipationDaoJdbc();
    }

    /** Crea el DAO de conceptos de pago. */
    public static PaymentConceptDao paymentConcepts() {
        return new PaymentConceptDaoJdbc();
    }

    /** Crea el DAO de descuentos por estudiante. */
    public static StudentDiscountDao studentDiscounts() {
        return new StudentDiscountDaoJdbc();
    }

    /** Crea el DAO de pagos. */
    public static PaymentDao payments() {
        return new PaymentDaoJdbc();
    }

    /** Crea el DAO de cargos. */
    public static ChargeDao charges() {
        return new ChargeDaoJdbc();
    }

    /** Crea el DAO de facturas. */
    public static InvoiceDao invoices() {
        return new InvoiceDaoJdbc();
    }

    /** Crea el DAO de recursos. */
    public static ResourceDao resources() {
        return new ResourceDaoJdbc();
    }

    /** Crea el DAO de categorías de recursos. */
    public static ResourceCategoryDao resourceCategories() {
        return new ResourceCategoryDaoJdbc();
    }

    /** Crea el DAO de préstamos a estudiantes. */
    public static LoanDao<StudentLoan> studentLoans() {
        return new StudentLoanDaoJdbc();
    }

    /** Crea el DAO de préstamos a profesores. */
    public static LoanDao<TeacherLoan> teacherLoans() {
        return new TeacherLoanDaoJdbc();
    }

    /** Crea el DAO de transacciones (commit/rollback). */
    public static TransactionManager transactions() {
        return new JdbcTransactionManager();
    }
}
