package com.littlehorizons.erp.util;

/**
 * Se lanza cuando el usuario escribe {@value InputReader#MAIN_MENU_KEYWORD} en cualquier pregunta.
 * Todos los submenús la dejan pasar y solo el menú principal la captura.
 */
public class ReturnToMainMenuException extends RuntimeException {

    public ReturnToMainMenuException() {
        super("Return to main menu requested");
    }
}
