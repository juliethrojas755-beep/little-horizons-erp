package com.littlehorizons.erp.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton that owns the database configuration and hands out JDBC connections.
 * <p>
 * It returns a NEW connection on every call so each DAO method can close it with
 * try-with-resources. The singleton guarantees the configuration is loaded only once.
 * </p>
 */
public final class DatabaseConnection {

    private static final String CONFIG_FILE = "database.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/erp_little_horizons";

    private static DatabaseConnection instance;

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        Properties properties = loadProperties();
        // A JVM argument (-Ddb.password=...) wins over the file, useful to avoid storing passwords
        this.url = System.getProperty("db.url", properties.getProperty("db.url", DEFAULT_URL));
        this.user = System.getProperty("db.user", properties.getProperty("db.user", "root"));
        this.password = System.getProperty("db.password", properties.getProperty("db.password", ""));
    }

    /** @return the single instance (lazy and thread safe) */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Opens a connection. The caller must close it (always inside try-with-resources).
     *
     * @return an open connection to {@code erp_little_horizons}
     * @throws SQLException when MySQL is not reachable or credentials are wrong
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** @return {@code true} when a connection can be opened right now */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            ErrorLogger.getInstance().error("DatabaseConnection", "Connection test failed", e);
            return false;
        }
    }

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
