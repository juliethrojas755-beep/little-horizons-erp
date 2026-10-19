package com.littlehorizons.erp.util;

/**
 * Thrown by {@link InputReader} when the user types the cancel keyword.
 * Menus catch it to go back to the previous screen from any prompt.
 */
public class OperationCancelledException extends RuntimeException {

    public OperationCancelledException() {
        super("Operation cancelled by the user");
    }
}
