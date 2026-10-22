package com.littlehorizons.erp;

import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.ErrorLogger;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.OperationCancelledException;
import com.littlehorizons.erp.view.MainView;
import com.littlehorizons.erp.view.components.ConsoleUI;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Punto de entrada de Little Horizons ERP. Verifica la base de datos, arma la aplicación
 * y abre el menú principal. Aquí NO hay lógica de negocio.
 */
public class Main {

    /**
     * Método con el que Java inicia el programa. Todo se hace dentro de run() y el finally garantiza
     * que siempre se apague el hilo del driver, incluso si ocurre un error.
     */
    public static void main(String[] args) {
        try {
            run();
        } finally {
            // Detiene el hilo de limpieza del driver de MySQL para que el programa (y mvn exec:java) termine limpio
            AbandonedConnectionCleanupThread.uncheckedShutdown();
        }
    }

    /** Flujo de la aplicación: conectar a MySQL, construir las dependencias y mostrar el menú principal. */
    private static void run() {
        // Salida en UTF-8 para que las tildes y los bordes se vean bien en cualquier consola (VS Code, NetBeans, terminal)
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        // Logger único (Singleton) que escribe en error_log.txt
        ErrorLogger logger = ErrorLogger.getInstance();
        logger.info("Main", "Application started");

        // Pantalla de bienvenida con el logo
        ConsoleUI.clear();
        ConsoleUI.banner();
        System.out.println("  " + Ansi.GRAY.apply("Conectando con la base de datos erp_little_horizons..."));
        // Si MySQL no responde, se explica qué revisar y se termina (no tiene sentido abrir el menú)
        if (!DatabaseConnection.getInstance().testConnection()) {
            ConsoleUI.error("No fue posible conectarse a MySQL.");
            ConsoleUI.info("Verifique que MySQL esté encendido y revise usuario y contraseña en "
                    + "src/main/resources/database.properties. El detalle quedó en error_log.txt.");
            return;
        }
        ConsoleUI.success("Conexión establecida.");

        // try-with-resources: el Scanner del teclado se cierra solo al terminar
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            // AppContext arma todas las dependencias; MainView muestra el menú hasta que el usuario elige salir
            new MainView(new AppContext(), new InputReader(scanner)).show();
        } catch (OperationCancelledException e) {
            // El teclado se cerró (por ejemplo Ctrl+D): se sale sin mostrar errores
        // Cualquier error inesperado se registra con su traza y se avisa al usuario
        } catch (RuntimeException e) {
            logger.error("Main", "Fatal error", e);
            ConsoleUI.error("Error inesperado. Revise error_log.txt.");
        }
        ConsoleUI.blank();
        ConsoleUI.success("Gracias por usar Little Horizons ERP. ¡Hasta pronto!");
        logger.info("Main", "Application closed");
    }
}
