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
 * Factory that creates the DAO implementations.
 * <p>
 * Services only see the interfaces. If tomorrow the data comes from another source,
 * only this class changes (Dependency Inversion + Factory pattern).
 * </p>
 */
public final class DaoFactory {

    private DaoFactory() {
    }

    public static StudentDao students() {
        return new StudentDaoJdbc();
    }

    public static GuardianDao guardians() {
        return new GuardianDaoJdbc();
    }

    public static TeacherDao teachers() {
        return new TeacherDaoJdbc();
    }

    public static GradeLevelDao gradeLevels() {
        return new GradeLevelDaoJdbc();
    }

    public static EnrollmentDao enrollments() {
        return new EnrollmentDaoJdbc();
    }

    public static SubjectDao subjects() {
        return new SubjectDaoJdbc();
    }

    public static TeacherAssignmentDao teacherAssignments() {
        return new TeacherAssignmentDaoJdbc();
    }

    public static ScheduleDao schedules() {
        return new ScheduleDaoJdbc();
    }

    public static SubjectEnrollmentDao subjectEnrollments() {
        return new SubjectEnrollmentDaoJdbc();
    }

    public static AcademicRecordDao academicRecords() {
        return new AcademicRecordDaoJdbc();
    }

    public static AttendanceDao attendance() {
        return new AttendanceDaoJdbc();
    }

    public static ClubDao clubs() {
        return new ClubDaoJdbc();
    }

    public static ClubMembershipDao clubMemberships() {
        return new ClubMembershipDaoJdbc();
    }

    public static SchoolEventDao events() {
        return new SchoolEventDaoJdbc();
    }

    public static EventParticipationDao eventParticipations() {
        return new EventParticipationDaoJdbc();
    }

    public static PaymentConceptDao paymentConcepts() {
        return new PaymentConceptDaoJdbc();
    }

    public static StudentDiscountDao studentDiscounts() {
        return new StudentDiscountDaoJdbc();
    }

    public static PaymentDao payments() {
        return new PaymentDaoJdbc();
    }

    public static ChargeDao charges() {
        return new ChargeDaoJdbc();
    }

    public static InvoiceDao invoices() {
        return new InvoiceDaoJdbc();
    }

    public static ResourceDao resources() {
        return new ResourceDaoJdbc();
    }

    public static ResourceCategoryDao resourceCategories() {
        return new ResourceCategoryDaoJdbc();
    }

    public static LoanDao<StudentLoan> studentLoans() {
        return new StudentLoanDaoJdbc();
    }

    public static LoanDao<TeacherLoan> teacherLoans() {
        return new TeacherLoanDaoJdbc();
    }

    public static TransactionManager transactions() {
        return new JdbcTransactionManager();
    }
}
