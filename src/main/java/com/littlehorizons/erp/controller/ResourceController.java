package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.model.dto.OverdueLoan;
import com.littlehorizons.erp.model.enums.ResourceStatus;
import com.littlehorizons.erp.service.ResourceService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Actions of the resources and loans screens.
 */
public class ResourceController extends BaseController {

    private final ResourceService service;

    public ResourceController(ResourceService service) {
        this.service = service;
    }

    public OperationResult<Integer> registerResource(Resource resource) {
        return execute("Recurso registrado en el inventario.", () -> service.registerResource(resource));
    }

    public OperationResult<Void> changeStatus(String code, ResourceStatus status) {
        return run("Estado del recurso actualizado a " + status + ".", () -> service.changeStatus(code, status));
    }

    public OperationResult<List<Resource>> findAll() {
        return execute(null, service::findAll);
    }

    public OperationResult<List<Resource>> findByStatus(ResourceStatus status) {
        return execute(null, () -> service.findByStatus(status));
    }

    public OperationResult<List<ResourceCategory>> getCategories() {
        return execute(null, service::getCategories);
    }

    public OperationResult<Map<Integer, ResourceCategory>> categoriesById() {
        return execute(null, service::categoriesById);
    }

    public OperationResult<Integer> loanToStudent(String code, int studentId, int days) {
        return execute("Préstamo registrado. Devolución en " + days + " días.", () -> service.loanToStudent(code, studentId, days));
    }

    public OperationResult<Integer> loanToTeacher(String code, int teacherId, int days) {
        return execute("Préstamo registrado. Devolución en " + days + " días.", () -> service.loanToTeacher(code, teacherId, days));
    }

    public OperationResult<Void> returnStudentLoan(int loanId) {
        return run("Devolución registrada. El recurso quedó Disponible.", () -> service.returnStudentLoan(loanId));
    }

    public OperationResult<Void> returnTeacherLoan(int loanId) {
        return run("Devolución registrada. El recurso quedó Disponible.", () -> service.returnTeacherLoan(loanId));
    }

    public OperationResult<List<StudentLoan>> openStudentLoans() {
        return execute(null, service::openStudentLoans);
    }

    public OperationResult<List<TeacherLoan>> openTeacherLoans() {
        return execute(null, service::openTeacherLoans);
    }

    public OperationResult<List<OverdueLoan>> overdueLoans() {
        return execute(null, () -> service.overdueLoans(LocalDate.now()));
    }
}
