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
 * Acciones de las pantallas de recursos y préstamos.
 * <p>Un controlador NO tiene reglas de negocio ni SQL: llama al servicio y envuelve el resultado en un
 * {@link OperationResult} para que la vista nunca reciba excepciones.</p>
 */
public class ResourceController extends BaseController {

    // Servicio al que se delegan las reglas de negocio
    private final ResourceService service;

    // El servicio llega por el constructor (inyección de dependencias): lo entrega AppContext
    public ResourceController(ResourceService service) {
        this.service = service;
    }

    /** Registra un recurso en el inventario. */
    public OperationResult<Integer> registerResource(Resource resource) {
        // execute(mensaje, acción): ejecuta la acción y convierte errores en un resultado fallido; null = sin mensaje de éxito
        return execute("Recurso registrado en el inventario.", () -> service.registerResource(resource));
    }

    /** Cambia el estado. */
    public OperationResult<Void> changeStatus(String code, ResourceStatus status) {
        return run("Estado del recurso actualizado a " + status + ".", () -> service.changeStatus(code, status));
    }

    /** Lista todos los registros. */
    public OperationResult<List<Resource>> findAll() {
        return execute(null, service::findAll);
    }

    /** Recursos en un estado. */
    public OperationResult<List<Resource>> findByStatus(ResourceStatus status) {
        return execute(null, () -> service.findByStatus(status));
    }

    /** Categorías de recursos. */
    public OperationResult<List<ResourceCategory>> getCategories() {
        return execute(null, service::getCategories);
    }

    /** Tabla id -> categoría para mostrar nombres. */
    public OperationResult<Map<Integer, ResourceCategory>> categoriesById() {
        return execute(null, service::categoriesById);
    }

    /** Presta un recurso a un estudiante. */
    public OperationResult<Integer> loanToStudent(String code, int studentId, int days) {
        return execute("Préstamo registrado. Devolución en " + days + " días.", () -> service.loanToStudent(code, studentId, days));
    }

    /** Presta un recurso a un profesor. */
    public OperationResult<Integer> loanToTeacher(String code, int teacherId, int days) {
        return execute("Préstamo registrado. Devolución en " + days + " días.", () -> service.loanToTeacher(code, teacherId, days));
    }

    /** Registra la devolución de un préstamo de estudiante. */
    public OperationResult<Void> returnStudentLoan(int loanId) {
        return run("Devolución registrada. El recurso quedó Disponible.", () -> service.returnStudentLoan(loanId));
    }

    /** Registra la devolución de un préstamo de profesor. */
    public OperationResult<Void> returnTeacherLoan(int loanId) {
        return run("Devolución registrada. El recurso quedó Disponible.", () -> service.returnTeacherLoan(loanId));
    }

    /** Préstamos de estudiantes que no se han devuelto. */
    public OperationResult<List<StudentLoan>> openStudentLoans() {
        return execute(null, service::openStudentLoans);
    }

    /** Préstamos de profesores que no se han devuelto. */
    public OperationResult<List<TeacherLoan>> openTeacherLoans() {
        return execute(null, service::openTeacherLoans);
    }

    /** Préstamos vencidos de estudiantes y profesores. */
    public OperationResult<List<OverdueLoan>> overdueLoans() {
        return execute(null, () -> service.overdueLoans(LocalDate.now()));
    }
}
