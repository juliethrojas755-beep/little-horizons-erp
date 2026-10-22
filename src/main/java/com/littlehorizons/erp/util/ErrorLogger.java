package com.littlehorizons.erp.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger Singleton que agrega los mensajes al archivo {@code error_log.txt} de la carpeta del proyecto.
 * <p>Es Singleton porque todas las clases deben escribir en el mismo archivo usando el mismo candado.</p>
 */
public final class ErrorLogger {

    // Nombre del archivo de registro
    public static final String LOG_FILE = "error_log.txt";
    // Formato de fecha y hora de cada línea del log
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Única instancia (Singleton)
    private static ErrorLogger instance;

    // Constructor privado: no se puede crear con "new"
    private ErrorLogger() {
    }

    /** @return la única instancia del logger (se crea la primera vez; synchronized lo hace seguro entre hilos) */
    public static synchronized ErrorLogger getInstance() {
        if (instance == null) {
            instance = new ErrorLogger();
        }
        return instance;
    }

    /** Registra un error con su traza completa (stack trace). */
    public void error(String source, String message, Throwable cause) {
        write("ERROR", source, message, cause);
    }

    /** Registra una advertencia sin traza (por ejemplo, una operación de negocio inválida). */
    public void warning(String source, String message) {
        write("WARN", source, message, null);
    }

    /** Registra un evento informativo (inicio de la aplicación, factura generada...). */
    public void info(String source, String message) {
        write("INFO", source, message, null);
    }

    /** Escribe una línea en el archivo; synchronized evita que dos escrituras se mezclen. */
    private synchronized void write(String level, String source, String message, Throwable cause) {
        // try-with-resources cierra el archivo aunque falle la escritura.
        // FileWriter(..., true) significa "append": agrega al final sin borrar lo anterior
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            // Formato: [fecha] [NIVEL] [clase] mensaje
            writer.write(String.format("[%s] [%-5s] [%s] %s", LocalDateTime.now().format(TIMESTAMP), level, source, message));
            writer.newLine();
            if (cause != null) {
                writer.write(stackTraceOf(cause));
                writer.newLine();
            }
        } catch (IOException e) {
            // Último recurso: si falla el propio archivo de log, se avisa por la salida de errores
            System.err.println("Could not write to " + LOG_FILE + ": " + e.getMessage());
        }
    }

    /** Convierte la traza del error en texto para poder guardarla en el archivo. */
    private String stackTraceOf(Throwable cause) {
        StringWriter buffer = new StringWriter();
        cause.printStackTrace(new PrintWriter(buffer));   // la traza se escribe en memoria, no en pantalla
        return buffer.toString().trim();
    }
}
