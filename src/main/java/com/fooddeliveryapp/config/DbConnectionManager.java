package com.fooddeliveryapp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionManager {

    private final DbConfig config;

    public DbConnectionManager() {
        this.config = new DbConfig();
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC driver not found", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getUrl(),
                config.getUsername(),
                config.getPassword()
        );
    }
}

