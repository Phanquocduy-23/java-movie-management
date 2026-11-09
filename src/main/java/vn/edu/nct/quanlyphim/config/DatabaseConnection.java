package vn.edu.nct.quanlyphim.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection getConnection()
            throws IOException, SQLException {

        Path path = Path.of("config", "database.properties");
        Properties config = new Properties();

        try (Reader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            config.load(reader);
        }

        String url = config.getProperty("db.url");
        String username = config.getProperty("db.username");
        String password = config.getProperty("db.password");

        if (url == null || username == null || password == null) {
            throw new IOException(
                "File cấu hình thiếu db.url, db.username hoặc db.password."
            );
        }

        return DriverManager.getConnection(url, username, password);
    }
}