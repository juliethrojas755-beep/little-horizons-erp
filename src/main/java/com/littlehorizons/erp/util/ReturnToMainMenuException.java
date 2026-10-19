package com.littlehorizons.erp.util;

/**
 * Thrown when the user types {@value InputReader#MAIN_MENU_KEYWORD} in any prompt.
 * Every submenu lets it pass, and only the main menu catches it.
 */
public class ReturnToMainMenuException extends RuntimeException {

    public ReturnToMainMenuException() {
        super("Return to main menu requested");
    }
}
