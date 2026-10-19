package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.LoanStatus;
import java.time.LocalDate;

/**
 * Common data of a resource loan. Subclasses define who the borrower is.
 */
public abstract class Loan {

    private int id;
    private int resourceId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;  // null while not returned
    private LoanStatus status;
    private String notes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /** @return the id of the student or teacher who has the resource */
    public abstract int getBorrowerId();

    /**
     * A loan is overdue when it has not been returned and its due date already passed.
     *
     * @param today reference date
     * @return {@code true} when the loan must appear in the overdue report
     */
    public boolean isOverdue(LocalDate today) {
        return returnDate == null && dueDate.isBefore(today);
    }
}
