package com.finvantage.dao;

import com.finvantage.model.Budget;
import com.finvantage.service.DatabaseConnectionService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for BUDGETS table in Oracle Database.
 * Joins with transactions to compute real-time spend utilization.
 */
public class BudgetDAO implements GenericDAO<Budget, Long> {

    private final DatabaseConnectionService dbService = DatabaseConnectionService.getInstance();

    @Override
    public Budget save(Budget budget) throws SQLException {
        String sql = "INSERT INTO BUDGETS (USER_ID, CATEGORY_ID, MONTH_YEAR, BUDGET_LIMIT, ALERT_THRESHOLD) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"BUDGET_ID"})) {
            ps.setLong(1, budget.getUserId());
            ps.setLong(2, budget.getCategoryId());
            ps.setString(3, budget.getMonthYear());
            ps.setBigDecimal(4, budget.getBudgetLimit());
            ps.setBigDecimal(5, budget.getAlertThreshold());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        budget.setId(rs.getLong(1));
                    }
                }
            }
            return budget;
        }
    }

    public List<Budget> findByUserIdAndMonth(Long userId, String monthYear) throws SQLException {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT b.*, c.NAME AS CAT_NAME, c.COLOR_HEX, " +
                     "       NVL((SELECT SUM(t.AMOUNT) FROM TRANSACTIONS t " +
                     "            WHERE t.CATEGORY_ID = b.CATEGORY_ID " +
                     "              AND t.TX_TYPE = 'EXPENSE' " +
                     "              AND t.STATUS = 'COMPLETED' " +
                     "              AND TO_CHAR(t.TX_DATE, 'YYYY-MM') = b.MONTH_YEAR), 0) AS SPENT_AMOUNT " +
                     "FROM BUDGETS b " +
                     "JOIN CATEGORIES c ON b.CATEGORY_ID = c.CATEGORY_ID " +
                     "WHERE b.USER_ID = ? AND b.MONTH_YEAR = ? " +
                     "ORDER BY b.BUDGET_ID ASC";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, monthYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBudget(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Optional<Budget> findById(Long id) throws SQLException {
        String sql = "SELECT b.*, c.NAME AS CAT_NAME, c.COLOR_HEX, 0 AS SPENT_AMOUNT " +
                     "FROM BUDGETS b JOIN CATEGORIES c ON b.CATEGORY_ID = c.CATEGORY_ID WHERE b.BUDGET_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBudget(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Budget> findAll() throws SQLException {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT b.*, c.NAME AS CAT_NAME, c.COLOR_HEX, 0 AS SPENT_AMOUNT " +
                     "FROM BUDGETS b JOIN CATEGORIES c ON b.CATEGORY_ID = c.CATEGORY_ID";
        try (Connection conn = dbService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToBudget(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(Budget budget) throws SQLException {
        String sql = "UPDATE BUDGETS SET BUDGET_LIMIT = ?, ALERT_THRESHOLD = ? WHERE BUDGET_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, budget.getBudgetLimit());
            ps.setBigDecimal(2, budget.getAlertThreshold());
            ps.setLong(3, budget.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM BUDGETS WHERE BUDGET_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Long id = rs.getLong("BUDGET_ID");
        Long userId = rs.getLong("USER_ID");
        Long catId = rs.getLong("CATEGORY_ID");
        String monthYear = rs.getString("MONTH_YEAR");
        BigDecimal limit = rs.getBigDecimal("BUDGET_LIMIT");
        BigDecimal threshold = rs.getBigDecimal("ALERT_THRESHOLD");
        BigDecimal spent = rs.getBigDecimal("SPENT_AMOUNT");

        Budget b = new Budget(id, userId, catId, monthYear, limit, threshold);
        b.setCategoryName(rs.getString("CAT_NAME"));
        b.setCategoryColor(rs.getString("COLOR_HEX"));
        b.setActualSpent(spent != null ? spent : BigDecimal.ZERO);
        Timestamp created = rs.getTimestamp("CREATED_AT");
        if (created != null) b.setCreatedAt(created.toLocalDateTime());
        return b;
    }
}
