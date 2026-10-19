package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.util.BusinessException;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.ErrorLogger;
import java.util.function.Supplier;

/**
 * Common error handling for every controller.
 * <p>Each controller method wraps its service call in {@link #execute}, so the try-catch
 * and the logging live in ONE place instead of being repeated in every method.</p>
 */
public abstract class BaseController {

    private final ErrorLogger logger = ErrorLogger.getInstance();

    /**
     * Runs an action and converts any exception into a failed {@link OperationResult}.
     *
     * @param successMessage message shown when the action works (may be null)
     * @param action         call to the service
     */
    protected <T> OperationResult<T> execute(String successMessage, Supplier<T> action) {
        try {
            return OperationResult.ok(action.get(), successMessage);
        } catch (BusinessException e) {
            // Rule violated by the user: not a bug, logged as warning
            logger.warning(getClass().getSimpleName(), e.getMessage());
            return OperationResult.fail(e.getMessage());
        } catch (DataAccessException e) {
            // Already logged with stack trace by the DAO
            return OperationResult.fail(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName(), "Unexpected error", e);
            return OperationResult.fail("Ocurrió un error inesperado. El detalle quedó en error_log.txt.");
        }
    }

    /** Same as {@link #execute} for actions without a return value. */
    protected OperationResult<Void> run(String successMessage, Runnable action) {
        return execute(successMessage, () -> {
            action.run();
            return null;
        });
    }
}
