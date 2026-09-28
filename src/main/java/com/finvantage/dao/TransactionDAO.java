package com.finvantage.dao;

import com.finvantage.factory.TransactionFactory;
import com.finvantage.model.Transaction;
import com.finvantage.service.DatabaseConnectionService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for TRANSACTIONS table in Oracle Database.
 * Polymorphically instantiates Income, Expense, or Transfer transaction objects.
 */
public class TransactionDAO implements GenericDAO<Transaction, Long> {

    private final DatabaseConnectionService dbService = DatabaseConnectionService.getInstance();

    @Override
    public Transaction save(Transaction tx) throws SQLException {
        try (Connection conn = dbService.getConnection()) {
            return save(tx, conn);
        }
    }

    public Transaction save(Transaction tx, Connection conn) throws SQLException {
        String sql = "INSERT INTO TRANSACTIONS (TX_REF, SOURCE_ACCOUNT_ID, DEST_ACCOUNT_ID, CATEGORY_ID, " +
                     "AMOUNT, TX_TYPE, DESCRIPTION, STATUS, TX_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"TX_ID"})) {
            ps.setString(1, tx.getTransactionRef());
            if (tx.getSourceAccountId() != null) ps.setLong(2, tx.getSourceAccountId()); else ps.setNull(2, java.sql.Types.NUMERIC);
            if (tx.getDestinationAccountId() != null) ps.setLong(3, tx.getDestinationAccountId()); else ps.setNull(3, java.sql.Types.NUMERIC);
            if (tx.getCategoryId() != null) ps.setLong(4, tx.getCategoryId()); else ps.setNull(4, java.sql.Types.NUMERIC);
            ps.setBigDecimal(5, tx.getAmount());
            ps.setString(6, tx.getTransactionType());
            ps.setString(7, tx.getDescription());
            ps.setString(8, tx.getStatus());
            ps.setTimestamp(9, Timestamp.valueOf(tx.getTransactionDate()));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        tx.setId(rs.getLong(1));
                    }
                }
            }
            return tx;
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) throws SQLException {
        String sql = "SELECT t.*, c.NAME AS CAT_NAME FROM TRANSACTIONS t " +
                     "LEFT JOIN CATEGORIES c ON t.CATEGORY_ID = c.CATEGORY_ID WHERE t.TX_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Transaction> findByUserId(Long userId, int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, c.NAME AS CAT_NAME FROM TRANSACTIONS t " +
                     "LEFT JOIN CATEGORIES c ON t.CATEGORY_ID = c.CATEGORY_ID " +
                     "WHERE (t.SOURCE_ACCOUNT_ID IN (SELECT ACCOUNT_ID FROM ACCOUNTS WHERE USER_ID = ?) " +
                     "    OR t.DEST_ACCOUNT_ID IN (SELECT ACCOUNT_ID FROM ACCOUNTS WHERE USER_ID = ?)) " +
                     "ORDER BY t.TX_DATE DESC FETCH FIRST ? ROWS ONLY";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, userId);
            ps.setInt(3, limit > 0 ? limit : 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return list;
    }

    public BigDecimal getMonthlyTotal(Long userId, String txType, String monthYear) throws SQLException {
        String sql = "SELECT NVL(SUM(t.AMOUNT), 0) FROM TRANSACTIONS t " +
                     "WHERE t.TX_TYPE = ? AND t.STATUS = 'COMPLETED' " +
                     "  AND TO_CHAR(t.TX_DATE, 'YYYY-MM') = ? " +
                     "  AND (t.SOURCE_ACCOUNT_ID IN (SELECT ACCOUNT_ID FROM ACCOUNTS WHERE USER_ID = ?) " +
                     "    OR t.DEST_ACCOUNT_ID IN (SELECT ACCOUNT_ID FROM ACCOUNTS WHERE USER_ID = ?))";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txType);
            ps.setString(2, monthYear);
            ps.setLong(3, userId);
            ps.setLong(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<Transaction> findAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, c.NAME AS CAT_NAME FROM TRANSACTIONS t " +
                     "LEFT JOIN CATEGORIES c ON t.CATEGORY_ID = c.CATEGORY_ID ORDER BY t.TX_DATE DESC";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTransaction(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(Transaction entity) {
        throw new UnsupportedOperationException("Financial ledger transactions are immutable by design.");
    }

    @Override
    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM TRANSACTIONS WHERE TX_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Long id = rs.getLong("TX_ID");
        String ref = rs.getString("TX_REF");
        Long src = rs.getLong("SOURCE_ACCOUNT_ID");
        if (rs.wasNull()) src = null;
        Long dst = rs.getLong("DEST_ACCOUNT_ID");
        if (rs.wasNull()) dst = null;
        Long cat = rs.getLong("CATEGORY_ID");
        if (rs.wasNull()) cat = null;
        BigDecimal amount = rs.getBigDecimal("AMOUNT");
        String type = rs.getString("TX_TYPE");
        String desc = rs.getString("DESCRIPTION");

        Transaction tx = TransactionFactory.createTransaction(type, id, ref, src, dst, cat, amount, desc);
        tx.setStatus(rs.getString("STATUS"));
        tx.setCategoryName(rs.getString("CAT_NAME"));
        Timestamp txDate = rs.getTimestamp("TX_DATE");
        if (txDate != null) tx.setTransactionDate(txDate.toLocalDateTime());
        Timestamp created = rs.getTimestamp("CREATED_AT");
        if (created != null) tx.setCreatedAt(created.toLocalDateTime());
        return tx;
    }
}
