package com.littlehorizons.erp.util;

/**
 * Se lanza cuando el usuario escribe la palabra de cancelar ("0") en cualquier pregunta.
 * Los menús la capturan para volver a la pantalla anterior desde cualquier punto.
 */
public class OperationCancelledException extends RuntimeException {

    public OperationCancelledException() {
        super("Operation cancelled by the user");
    }
}
