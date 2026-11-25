package pe.edu.upeu.sysventas.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** PostgreSQL settings are read from environment variables, never committed. */
public final class DatabaseConfig {
    private DatabaseConfig() { }

    public static Connection getConnection() throws SQLException {
        String url = required("SYSVENTAS_DB_URL");
        String user = required("SYSVENTAS_DB_USER");
        String password = required("SYSVENTAS_DB_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configura la variable de entorno " + name);
        }
        return value;
    }
}
