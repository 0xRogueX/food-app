package com.fooddeliveryapp;

import com.fooddeliveryapp.config.DbConnectionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Abstract base class for service tests that exercise JDBC repositories backed
 * by an H2 in-memory database (PostgreSQL compatibility mode).
 *
 * <p>The schema is created once for the entire JVM run ({@code @BeforeAll}).
 * Every table is truncated before each individual test ({@code @BeforeEach})
 * so tests remain fully isolated from each other.
 *
 * <p>Subclasses share the single {@link #connectionManager} instance and must
 * use it when constructing JDBC repositories inside their own {@code @BeforeEach}
 * setup methods.
 */
public abstract class JdbcTestBase {

    protected static DbConnectionManager connectionManager;

    private static volatile boolean schemaInitialized = false;

    @BeforeAll
    static void ensureSchemaExists() throws IOException, SQLException {
        if (connectionManager == null) {
            connectionManager = new DbConnectionManager();
        }
        if (!schemaInitialized) {
            initializeSchema();
            schemaInitialized = true;
        }
    }

    private static void initializeSchema() throws IOException, SQLException {
        String schema;
        try (InputStream is = JdbcTestBase.class.getClassLoader()
                .getResourceAsStream("test-schema.sql")) {
            if (is == null) {
                throw new IllegalStateException("test-schema.sql not found on classpath");
            }
            schema = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : schema.split(";")) {
                String trimmed = sql.strip();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("DELETE FROM cart_items");
            stmt.execute("DELETE FROM carts");
            stmt.execute("DELETE FROM order_items");
            stmt.execute("DELETE FROM payments");
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM menu_items");
            stmt.execute("DELETE FROM categories");
            stmt.execute("DELETE FROM admins");
            stmt.execute("DELETE FROM customers");
            stmt.execute("DELETE FROM delivery_agents");
            stmt.execute("DELETE FROM users");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }
}
