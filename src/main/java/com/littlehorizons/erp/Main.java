package com.littlehorizons.erp;

import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.ErrorLogger;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.OperationCancelledException;
import com.littlehorizons.erp.view.MainView;
import com.littlehorizons.erp.view.components.ConsoleUI;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Entry point of Little Horizons ERP. It checks the database, builds the application
 * and opens the main menu. No business logic lives here.
 */
public class Main {

    public static void main(String[] args) {
        // UTF-8 output so accents and box characters look right in any console (VS Code, NetBeans, terminal)
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        ErrorLogger logger = ErrorLogger.getInstance();
        logger.info("Main", "Application started");

        ConsoleUI.clear();
        ConsoleUI.banner();
        System.out.println("  " + Ansi.GRAY.apply("Conectando con la base de datos erp_little_horizons..."));
        if (!DatabaseConnection.getInstance().testConnection()) {
            ConsoleUI.error("No fue posible conectarse a MySQL.");
            ConsoleUI.info("Verifique que MySQL esté encendido y revise usuario y contraseña en "
                    + "src/main/resources/database.properties. El detalle quedó en error_log.txt.");
            return;
        }
        ConsoleUI.success("Conexión establecida.");

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            new MainView(new AppContext(), new InputReader(scanner)).show();
        } catch (OperationCancelledException e) {
            // input stream closed: leave quietly
        } catch (RuntimeException e) {
            logger.error("Main", "Fatal error", e);
            ConsoleUI.error("Error inesperado. Revise error_log.txt.");
        }
        ConsoleUI.blank();
        ConsoleUI.success("Gracias por usar Little Horizons ERP. ¡Hasta pronto!");
        logger.info("Main", "Application closed");
    }
}
