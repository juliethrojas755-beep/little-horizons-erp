package com.littlehorizons.erp.controller;

import java.util.Optional;

/**
 * Answer of a controller to the view: success flag, message for the user and optional data.
 * The view never receives exceptions, only this object.
 */
public record OperationResult<T>(boolean success, String message, T data) {

    public static <T> OperationResult<T> ok(T data, String message) {
        return new OperationResult<>(true, message, data);
    }

    public static <T> OperationResult<T> ok(T data) {
        return new OperationResult<>(true, null, data);
    }

    public static <T> OperationResult<T> fail(String message) {
        return new OperationResult<>(false, message, null);
    }

    public Optional<T> dataOptional() {
        return Optional.ofNullable(data);
    }
}
