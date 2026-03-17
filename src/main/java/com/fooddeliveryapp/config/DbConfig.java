package com.fooddeliveryapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbConfig {

    private static final String PROPS_FILE = "db.properties";

    public static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = DbConfig.class.getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (is == null) {
                System.out.println("[WARN] " + PROPS_FILE + " not found on classpath. Using defaults.");
            } else {
                props.load(is);
            }
        } catch (IOException e) {
            System.out.println("[WARN] Could not load db.properties: " + e.getMessage());
        }
        return props;
    }

    private final String url;
    private final String username;
    private final String password;

    public DbConfig() {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(PROPS_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Could not find " + PROPS_FILE + " on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + PROPS_FILE, e);
        }

        this.url = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

