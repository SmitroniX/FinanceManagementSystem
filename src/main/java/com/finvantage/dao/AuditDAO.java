package com.finvantage.dao;

import com.finvantage.model.AuditLog;
import com.finvantage.service.DatabaseConnectionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for AUDIT_LOGS table in Oracle Database.
 */
public class AuditDAO {

    private final DatabaseConnectionService dbService = DatabaseConnectionService.getInstance();

    public void insertLog(AuditLog log) {
        String sql = "INSERT INTO AUDIT_LOGS (ACTION_TYPE, TABLE_NAME, RECORD_ID, USER_ID, DETAILS, LOG_TIMESTAMP) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getActionType());
            ps.setString(2, log.getTableName());
            if (log.getRecordId() != null) ps.setLong(3, log.getRecordId()); else ps.setNull(3, java.sql.Types.NUMERIC);
            if (log.getUserId() != null) ps.setLong(4, log.getUserId()); else ps.setNull(4, java.sql.Types.NUMERIC);
            ps.setString(5, log.getDetails());
            ps.setTimestamp(6, Timestamp.valueOf(log.getTimestamp()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AuditDAO] Warning: Failed to write audit record: " + e.getMessage());
        }
    }

    public List<AuditLog> getRecentLogs(int limit) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AUDIT_LOGS ORDER BY LOG_TIMESTAMP DESC FETCH FIRST ? ROWS ONLY";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog(
                        rs.getLong("LOG_ID"),
                        rs.getString("ACTION_TYPE"),
                        rs.getString("TABLE_NAME"),
                        rs.getLong("RECORD_ID"),
                        rs.getLong("USER_ID"),
                        rs.getString("DETAILS")
                    );
                    Timestamp ts = rs.getTimestamp("LOG_TIMESTAMP");
                    if (ts != null) log.setTimestamp(ts.toLocalDateTime());
                    list.add(log);
                }
            }
        }
        return list;
    }
}
