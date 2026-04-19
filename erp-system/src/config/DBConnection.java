package config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL = resolve("ERP_DB_URL", "jdbc:mysql://localhost:3306/erp_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = resolve("ERP_DB_USER", "root");
    private static final String PASSWORD = resolve("ERP_DB_PASSWORD", "Yash@2004");

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String resolve(String key, String fallback) {
        String propValue = System.getProperty(key);
        if (propValue != null && !propValue.isBlank()) {
            return propValue;
        }
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return fallback;
    }
}
