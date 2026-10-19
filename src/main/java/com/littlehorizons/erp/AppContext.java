package com.littlehorizons.erp;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.controller.ExtracurricularController;
import com.littlehorizons.erp.controller.FinanceController;
import com.littlehorizons.erp.controller.ReportController;
import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.dao.DaoFactory;
import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.enums.StudentStatus;
import com.littlehorizons.erp.service.AcademicService;
import com.littlehorizons.erp.service.AttendanceService;
import com.littlehorizons.erp.service.ExtracurricularService;
import com.littlehorizons.erp.service.PaymentService;
import com.littlehorizons.erp.service.ReportService;
import com.littlehorizons.erp.service.ResourceService;
import com.littlehorizons.erp.service.StudentService;
import com.littlehorizons.erp.service.TeacherService;
import com.littlehorizons.erp.service.discount.DiscountStrategyFactory;
import com.littlehorizons.erp.service.invoice.OpenPdfInvoiceGenerator;

/**
 * Composition root: the ONLY place where concrete classes are created and connected.
 * <p>DAOs go into services and services into controllers through constructors
 * (manual Dependency Injection). The rest of the code only knows interfaces.</p>
 */
public class AppContext {

    private final StudentController studentController;
    private final AcademicController academicController;
    private final TeacherController teacherController;
    private final ExtracurricularController extracurricularController;
    private final FinanceController financeController;
    private final ResourceController resourceController;
    private final ReportController reportController;

    public AppContext() {
        StudentDao studentDao = DaoFactory.students();
        GuardianDao guardianDao = DaoFactory.guardians();

        StudentService studentService = new StudentService(studentDao, guardianDao, DaoFactory.enrollments(),
                DaoFactory.gradeLevels());
        AcademicService academicService = new AcademicService(DaoFactory.subjects(), DaoFactory.subjectEnrollments(),
                DaoFactory.academicRecords(), DaoFactory.teacherAssignments(), DaoFactory.enrollments());
        AttendanceService attendanceService = new AttendanceService(DaoFactory.attendance(), studentDao);
        TeacherService teacherService = new TeacherService(DaoFactory.teachers(), DaoFactory.teacherAssignments(),
                DaoFactory.schedules(), DaoFactory.subjects());
        ExtracurricularService extracurricularService = new ExtracurricularService(DaoFactory.clubs(),
                DaoFactory.clubMemberships(), DaoFactory.events(), DaoFactory.eventParticipations(), studentDao);

        // The sibling strategy asks the student service whether an ACTIVE sibling exists
        DiscountStrategyFactory discounts = new DiscountStrategyFactory(studentId -> studentService.getSiblings(studentId)
                .stream().anyMatch(sibling -> sibling.getStatus() == StudentStatus.ACTIVE));
        PaymentService paymentService = new PaymentService(DaoFactory.paymentConcepts(), DaoFactory.studentDiscounts(),
                DaoFactory.charges(), DaoFactory.payments(), DaoFactory.invoices(), studentDao, guardianDao,
                DaoFactory.transactions(), new OpenPdfInvoiceGenerator(), discounts);
        ResourceService resourceService = new ResourceService(DaoFactory.resources(), DaoFactory.resourceCategories(),
                DaoFactory.studentLoans(), DaoFactory.teacherLoans(), studentDao, DaoFactory.teachers(),
                DaoFactory.transactions());
        ReportService reportService = new ReportService(studentDao, DaoFactory.enrollments(), DaoFactory.gradeLevels(),
                DaoFactory.academicRecords(), DaoFactory.subjects(), DaoFactory.payments(), DaoFactory.paymentConcepts(),
                DaoFactory.charges(), DaoFactory.teachers(), DaoFactory.teacherAssignments(), DaoFactory.schedules(),
                DaoFactory.eventParticipations(), DaoFactory.resources(), DaoFactory.clubs(), DaoFactory.studentLoans(),
                DaoFactory.teacherLoans());

        studentController = new StudentController(studentService);
        academicController = new AcademicController(academicService, attendanceService);
        teacherController = new TeacherController(teacherService);
        extracurricularController = new ExtracurricularController(extracurricularService);
        financeController = new FinanceController(paymentService);
        resourceController = new ResourceController(resourceService);
        reportController = new ReportController(reportService);
    }

    public StudentController students() {
        return studentController;
    }

    public AcademicController academic() {
        return academicController;
    }

    public TeacherController teachers() {
        return teacherController;
    }

    public ExtracurricularController extracurricular() {
        return extracurricularController;
    }

    public FinanceController finance() {
        return financeController;
    }

    public ResourceController resources() {
        return resourceController;
    }

    public ReportController reports() {
        return reportController;
    }
}
