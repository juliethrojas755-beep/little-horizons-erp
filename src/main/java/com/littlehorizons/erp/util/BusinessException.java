package com.littlehorizons.erp.util;

/**
 * Thrown when a business rule is violated (full club, schedule conflict, invalid grade...).
 * Its message is meant to be shown to the user as is.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
