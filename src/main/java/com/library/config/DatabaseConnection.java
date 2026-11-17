package com.library.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = value("LIBRARY_DB_URL", "db.url",
                defaultUrl());
        String user = value("LIBRARY_DB_USER", "db.user", environmentOr("MYSQLUSER", "root"));
        String password = value("LIBRARY_DB_PASSWORD", "db.password", environmentOr("MYSQLPASSWORD", ""));
        return DriverManager.getConnection(url, user, password);
    }

    private static String defaultUrl() {
        String host = System.getenv("MYSQLHOST");
        if (host == null || host.isBlank()) {
            return "jdbc:mysql://localhost:3306/library_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }
        String port = environmentOr("MYSQLPORT", "3306");
        String database = environmentOr("MYSQLDATABASE", "library_management");
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static String environmentOr(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String value(String environmentKey, String propertyKey, String fallback) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        return PROPERTIES.getProperty(propertyKey, fallback);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/library.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException exception) {
            throw new ExceptionInInitializerError("Unable to read database configuration: " + exception.getMessage());
        }
        Path external = Path.of("library.properties");
        if (Files.isRegularFile(external)) {
            try (InputStream input = Files.newInputStream(external)) {
                properties.load(input);
            } catch (IOException exception) {
                throw new ExceptionInInitializerError("Unable to read library.properties: " + exception.getMessage());
            }
        }
        return properties;
    }
}
