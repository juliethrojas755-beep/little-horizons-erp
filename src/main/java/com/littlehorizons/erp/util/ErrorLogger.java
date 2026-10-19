package com.littlehorizons.erp.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton logger that appends errors to {@code error_log.txt} in the working directory.
 * <p>Singleton because every class must write to the same file through the same lock.</p>
 */
public final class ErrorLogger {

    public static final String LOG_FILE = "error_log.txt";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static ErrorLogger instance;

    private ErrorLogger() {
    }

    /** @return the single logger instance (lazy and thread safe) */
    public static synchronized ErrorLogger getInstance() {
        if (instance == null) {
            instance = new ErrorLogger();
        }
        return instance;
    }

    /** Logs an error with its full stack trace. */
    public void error(String source, String message, Throwable cause) {
        write("ERROR", source, message, cause);
    }

    /** Logs a warning without stack trace (for example, an invalid business operation). */
    public void warning(String source, String message) {
        write("WARN", source, message, null);
    }

    /** Logs an informative event (application start, invoice generated...). */
    public void info(String source, String message) {
        write("INFO", source, message, null);
    }

    private synchronized void write(String level, String source, String message, Throwable cause) {
        // try-with-resources closes the writer even if writing fails
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(String.format("[%s] [%-5s] [%s] %s", LocalDateTime.now().format(TIMESTAMP), level, source, message));
            writer.newLine();
            if (cause != null) {
                writer.write(stackTraceOf(cause));
                writer.newLine();
            }
        } catch (IOException e) {
            // Last resort: the log file itself failed, so report on the error stream
            System.err.println("Could not write to " + LOG_FILE + ": " + e.getMessage());
        }
    }

    private String stackTraceOf(Throwable cause) {
        StringWriter buffer = new StringWriter();
        cause.printStackTrace(new PrintWriter(buffer));
        return buffer.toString().trim();
    }
}
