package com.finvantage.dao;

import com.finvantage.factory.AccountFactory;
import com.finvantage.model.Account;
import com.finvantage.model.CheckingAccount;
import com.finvantage.model.InvestmentAccount;
import com.finvantage.model.SavingsAccount;
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
 * Data Access Object for ACCOUNTS table in Oracle Database.
 * Converts relational rows into polymorphic OOP Account domain objects.
 */
public class AccountDAO implements GenericDAO<Account, Long> {

    private final DatabaseConnectionService dbService = DatabaseConnectionService.getInstance();

    @Override
    public Account save(Account account) throws SQLException {
        String sql = "INSERT INTO ACCOUNTS (USER_ID, ACCOUNT_NUMBER, ACCOUNT_NAME, ACCOUNT_TYPE, BALANCE, CURRENCY, " +
                     "INTEREST_RATE, OVERDRAFT_LIMIT, MINIMUM_BALANCE, PORTFOLIO_RISK, STATUS) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ACCOUNT_ID"})) {
            
            ps.setLong(1, account.getUserId());
            ps.setString(2, account.getAccountNumber());
            ps.setString(3, account.getAccountName());
            ps.setString(4, account.getAccountType());
            ps.setBigDecimal(5, account.getBalance());
            ps.setString(6, account.getCurrency());

            if (account instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) account;
                ps.setBigDecimal(7, sa.getInterestRate());
                ps.setBigDecimal(8, BigDecimal.ZERO);
                ps.setBigDecimal(9, sa.getMinimumBalance());
                ps.setString(10, null);
            } else if (account instanceof CheckingAccount) {
                CheckingAccount ca = (CheckingAccount) account;
                ps.setBigDecimal(7, BigDecimal.ZERO);
                ps.setBigDecimal(8, ca.getOverdraftLimit());
                ps.setBigDecimal(9, BigDecimal.ZERO);
                ps.setString(10, null);
            } else if (account instanceof InvestmentAccount) {
                InvestmentAccount ia = (InvestmentAccount) account;
                ps.setBigDecimal(7, ia.getAnnualDividendYield());
                ps.setBigDecimal(8, BigDecimal.ZERO);
                ps.setBigDecimal(9, BigDecimal.ZERO);
                ps.setString(10, ia.getPortfolioRisk());
            } else {
                ps.setBigDecimal(7, BigDecimal.ZERO);
                ps.setBigDecimal(8, BigDecimal.ZERO);
                ps.setBigDecimal(9, BigDecimal.ZERO);
                ps.setString(10, null);
            }

            ps.setString(11, account.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        account.setId(rs.getLong(1));
                    }
                }
            }
            return account;
        }
    }

    @Override
    public Optional<Account> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM ACCOUNTS WHERE ACCOUNT_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Account> findByUserId(Long userId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM ACCOUNTS WHERE USER_ID = ? ORDER BY ACCOUNT_ID ASC";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        }
        return accounts;
    }

    public boolean updateBalance(Long accountId, BigDecimal newBalance, Connection conn) throws SQLException {
        String sql = "UPDATE ACCOUNTS SET BALANCE = ?, UPDATED_AT = CURRENT_TIMESTAMP WHERE ACCOUNT_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, accountId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Account> findAll() throws SQLException {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM ACCOUNTS ORDER BY ACCOUNT_ID ASC";
        try (Connection conn = dbService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToAccount(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(Account account) throws SQLException {
        String sql = "UPDATE ACCOUNTS SET ACCOUNT_NAME = ?, STATUS = ?, UPDATED_AT = CURRENT_TIMESTAMP WHERE ACCOUNT_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getAccountName());
            ps.setString(2, account.getStatus());
            ps.setLong(3, account.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM ACCOUNTS WHERE ACCOUNT_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Long id = rs.getLong("ACCOUNT_ID");
        Long userId = rs.getLong("USER_ID");
        String accNum = rs.getString("ACCOUNT_NUMBER");
        String accName = rs.getString("ACCOUNT_NAME");
        String accType = rs.getString("ACCOUNT_TYPE");
        BigDecimal balance = rs.getBigDecimal("BALANCE");
        String currency = rs.getString("CURRENCY");
        BigDecimal interest = rs.getBigDecimal("INTEREST_RATE");
        BigDecimal overdraft = rs.getBigDecimal("OVERDRAFT_LIMIT");
        BigDecimal minBal = rs.getBigDecimal("MINIMUM_BALANCE");
        String risk = rs.getString("PORTFOLIO_RISK");

        Account acc = AccountFactory.createAccount(
            accType, id, userId, accNum, accName, balance, currency,
            interest, overdraft, minBal, risk
        );
        acc.setStatus(rs.getString("STATUS"));
        Timestamp created = rs.getTimestamp("CREATED_AT");
        if (created != null) acc.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("UPDATED_AT");
        if (updated != null) acc.setUpdatedAt(updated.toLocalDateTime());
        return acc;
    }
}
