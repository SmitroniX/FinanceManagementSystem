package com.finvantage.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Encapsulates runtime database connection configuration for Oracle Database.
 * Allows reading from db.properties, environment variables, or GUI overrides.
 */
public class DatabaseConfig {

    private String mode = "ORACLE"; // ORACLE or MOCK
    private String driver = "oracle.jdbc.OracleDriver";
    private String url = "jdbc:oracle:thin:@localhost:1521:xe";
    private String user = "system";
    private String password = "oracle";
    private int timeoutSeconds = 5;

    private static final DatabaseConfig INSTANCE = new DatabaseConfig();

    private DatabaseConfig() {
        loadFromProperties();
    }

    public static DatabaseConfig getInstance() {
        return INSTANCE;
    }

    public void loadFromProperties() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                this.mode = props.getProperty("db.mode", this.mode);
                this.driver = props.getProperty("db.driver", this.driver);
                this.url = props.getProperty("db.url", this.url);
                this.user = props.getProperty("db.user", this.user);
                this.password = props.getProperty("db.password", this.password);
                this.timeoutSeconds = Integer.parseInt(props.getProperty("db.timeout.seconds", "5"));
            }
        } catch (Exception e) {
            System.err.println("[DatabaseConfig] Using default configuration: " + e.getMessage());
        }
    }

    public boolean isMockMode() {
        return "MOCK".equalsIgnoreCase(mode);
    }

    // Getters and Setters
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
