package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.util.BusinessException;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.ErrorLogger;
import java.util.function.Supplier;

/**
 * Manejo de errores común a todos los controladores.
 * <p>Cada método de un controlador envuelve la llamada al servicio con {@link #execute}, así el try-catch
 * y el registro en el log viven en UN solo lugar en vez de repetirse en cada método.</p>
 */
public abstract class BaseController {

    // Logger único (Singleton) para dejar constancia de los problemas
    private final ErrorLogger logger = ErrorLogger.getInstance();

    /**
     * Ejecuta una acción y convierte cualquier excepción en un {@link OperationResult} fallido.
     *
     * @param successMessage mensaje que se muestra si la acción funciona (puede ser null)
     * @param action         llamada al servicio (se pasa como lambda)
     */
    protected <T> OperationResult<T> execute(String successMessage, Supplier<T> action) {
        try {
            // Todo bien: se devuelve el resultado con el mensaje de éxito
            return OperationResult.ok(action.get(), successMessage);
        } catch (BusinessException e) {
            // El usuario rompió una regla (cupo lleno, nota inválida...): no es un error del programa, se avisa con WARN
            logger.warning(getClass().getSimpleName(), e.getMessage());
            return OperationResult.fail(e.getMessage());
        } catch (DataAccessException e) {
            // Falla de base de datos: el DAO ya la registró con su traza completa, aquí solo se informa
            return OperationResult.fail(e.getMessage());
        } catch (RuntimeException e) {
            // Cualquier otro error inesperado: se guarda con su traza para poder revisarlo después
            logger.error(getClass().getSimpleName(), "Unexpected error", e);
            return OperationResult.fail("Ocurrió un error inesperado. El detalle quedó en error_log.txt.");
        }
    }

    /** Igual que {@link #execute} pero para acciones que no devuelven ningún valor. */
    protected OperationResult<Void> run(String successMessage, Runnable action) {
        return execute(successMessage, () -> {
            action.run();
            return null;
        });
    }
}
