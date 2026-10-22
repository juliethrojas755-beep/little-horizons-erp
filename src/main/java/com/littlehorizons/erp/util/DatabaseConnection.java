package com.littlehorizons.erp.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton que guarda la configuración de la base de datos y entrega las conexiones JDBC.
 * <p>
 * Entrega una conexión NUEVA en cada llamada para que cada método de un DAO la cierre con
 * try-with-resources. El Singleton garantiza que la configuración se lea una sola vez.
 * </p>
 */
public final class DatabaseConnection {

    // Archivo de configuración dentro de src/main/resources (usuario, clave y URL de MySQL)
    private static final String CONFIG_FILE = "database.properties";
    // URL por defecto si el archivo no existe
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/erp_little_horizons";

    // Única instancia de la clase (patrón Singleton)
    private static DatabaseConnection instance;

    // Datos de conexión leídos una sola vez
    private final String url;
    private final String user;
    private final String password;

    // Constructor privado: nadie puede crear otra instancia con "new"
    private DatabaseConnection() {
        Properties properties = loadProperties();
        // Un argumento de la JVM (-Ddb.password=...) tiene prioridad sobre el archivo,
        // útil para no guardar la clave en el archivo
        this.url = System.getProperty("db.url", properties.getProperty("db.url", DEFAULT_URL));
        this.user = System.getProperty("db.user", properties.getProperty("db.user", "root"));
        this.password = System.getProperty("db.password", properties.getProperty("db.password", ""));
    }

    /** @return la única instancia (se crea la primera vez que se pide; synchronized evita que dos hilos creen dos) */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Abre una conexión. Quien la pida debe cerrarla (siempre dentro de un try-with-resources).
     *
     * @return una conexión abierta a erp_little_horizons
     * @throws SQLException si MySQL no responde o las credenciales son incorrectas
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** @return {@code true} si en este momento se puede abrir una conexión */
    public boolean testConnection() {
        // La conexión de prueba se cierra sola al salir del try
        try (Connection connection = getConnection()) {
            return connection.isValid(2);     // espera máximo 2 segundos la respuesta del servidor
        } catch (SQLException e) {
            ErrorLogger.getInstance().error("DatabaseConnection", "Connection test failed", e);
            return false;
        }
    }

    /** Lee database.properties desde el classpath; si no existe usa valores por defecto. */
    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                ErrorLogger.getInstance().warning("DatabaseConnection", CONFIG_FILE + " not found, using defaults");
            }
        } catch (IOException e) {
            ErrorLogger.getInstance().error("DatabaseConnection", "Could not read " + CONFIG_FILE, e);
        }
        return properties;
    }
}
