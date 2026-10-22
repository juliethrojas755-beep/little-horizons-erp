package com.littlehorizons.erp.controller;

import java.util.Optional;

/**
 * Respuesta de un controlador a la vista: indica si salió bien, un mensaje para el usuario y los datos opcionales.
 * La vista nunca recibe excepciones, solo este objeto (por eso no necesita try-catch).
 *
 * @param success {@code true} si la operación funcionó
 * @param message mensaje para mostrar (de éxito o de error); puede ser null
 * @param data    datos devueltos por la operación; null si falló o no devuelve nada
 * @param <T>     tipo de los datos
 */
public record OperationResult<T>(boolean success, String message, T data) {

    /** Operación exitosa con datos y un mensaje de confirmación. */
    public static <T> OperationResult<T> ok(T data, String message) {
        return new OperationResult<>(true, message, data);
    }

    /** Operación exitosa con datos y sin mensaje (consultas). */
    public static <T> OperationResult<T> ok(T data) {
        return new OperationResult<>(true, null, data);
    }

    /** Operación fallida con el mensaje de error para el usuario. */
    public static <T> OperationResult<T> fail(String message) {
        return new OperationResult<>(false, message, null);
    }

    /** Los datos dentro de un Optional (vacío cuando son null). */
    public Optional<T> dataOptional() {
        return Optional.ofNullable(data);
    }
}
