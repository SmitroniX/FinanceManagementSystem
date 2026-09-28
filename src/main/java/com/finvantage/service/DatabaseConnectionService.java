package com.finvantage.service;

import com.finvantage.config.DatabaseConfig;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * DatabaseConnectionService implements the Singleton Pattern for managing Oracle Database connections.
 * Handles Oracle JDBC driver registration, connection lifecycle, and stored procedure execution.
 */
public class DatabaseConnectionService {

    private static volatile DatabaseConnectionService instance;
    private final DatabaseConfig config;
    private boolean driverLoaded = false;

    private DatabaseConnectionService() {
        this.config = DatabaseConfig.getInstance();
        initDriver();
    }

    public static DatabaseConnectionService getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionService.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionService();
                }
            }
        }
        return instance;
    }

    private void initDriver() {
        try {
            Class.forName(config.getDriver());
            this.driverLoaded = true;
            System.out.println("[DB] Oracle JDBC Driver successfully registered: " + config.getDriver());
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Warning: Oracle JDBC Driver class not found: " + e.getMessage());
            this.driverLoaded = false;
        }
    }

    /**
     * Establishes and returns a new physical Connection to Oracle Database.
     */
    public Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            initDriver();
        }
        DriverManager.setLoginTimeout(config.getTimeoutSeconds());
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    /**
     * Tests connectivity to the configured Oracle Database instance.
     * Returns true if connection succeeds and executes a ping query.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(config.getTimeoutSeconds());
        } catch (Exception e) {
            System.err.println("[DB] Oracle Connection Test Failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Executes an array of DDL/DML statements against Oracle Database.
     */
    public void executeScript(String scriptContent) throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String[] commands = scriptContent.split(";");
            for (String cmd : commands) {
                String trimmed = cmd.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException ex) {
                        // Ignore drop errors during re-initialization
                        if (!trimmed.toUpperCase().startsWith("DROP")) {
                            System.err.println("[DB Script Warning] " + ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Demonstrates execution of Oracle PL/SQL Stored Procedure (SP_TRANSFER_FUNDS)
     * with IN and OUT parameters.
     */
    public TransferResult callTransferProcedure(Long sourceAccId, Long destAccId, 
                                                double amount, String description, String txRef) throws SQLException {
        String plsql = "{call SP_TRANSFER_FUNDS(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getConnection(); CallableStatement cstmt = conn.prepareCall(plsql)) {
            cstmt.setLong(1, sourceAccId);
            cstmt.setLong(2, destAccId);
            cstmt.setDouble(3, amount);
            cstmt.setString(4, description);
            cstmt.setString(5, txRef);
            
            // Register OUT parameters
            cstmt.registerOutParameter(6, Types.VARCHAR); // p_status_out
            cstmt.registerOutParameter(7, Types.VARCHAR); // p_message_out

            cstmt.execute();

            String status = cstmt.getString(6);
            String message = cstmt.getString(7);
            return new TransferResult("SUCCESS".equalsIgnoreCase(status), message);
        }
    }

    public static class TransferResult {
        private final boolean success;
        private final String message;

        public TransferResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
