package com.finvantage.service;

import com.finvantage.config.DatabaseConfig;
import com.finvantage.dao.AccountDAO;
import com.finvantage.dao.AuditDAO;
import com.finvantage.dao.BudgetDAO;
import com.finvantage.dao.CategoryDAO;
import com.finvantage.dao.TransactionDAO;
import com.finvantage.event.FinanceEventManager;
import com.finvantage.factory.TransactionFactory;
import com.finvantage.model.Account;
import com.finvantage.model.AuditLog;
import com.finvantage.model.Budget;
import com.finvantage.model.Category;
import com.finvantage.model.FinancialSummary;
import com.finvantage.model.InsufficientFundsException;
import com.finvantage.model.Transaction;
import com.finvantage.util.ValidationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * FinanceService orchestrates business workflows, ACID transaction boundaries,
 * and data synchronization between Oracle Database and the Swing frontend.
 *
 * Demonstrates:
 * 1. ACID Transactions (Atomicity, Consistency, Isolation, Durability)
 * 2. Row-level locking with SELECT FOR UPDATE
 * 3. Observer pattern notifications
 */
public class FinanceService {

    private static final FinanceService INSTANCE = new FinanceService();

    private final DatabaseConnectionService dbService = DatabaseConnectionService.getInstance();
    private final DatabaseConfig config = DatabaseConfig.getInstance();
    private final MockDatabaseService mockDb = MockDatabaseService.getInstance();

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    private FinanceService() {}

    public static FinanceService getInstance() {
        return INSTANCE;
    }

    /**
     * Executes an ACID-compliant Inter-Account Transfer.
     * Guarantees Atomicity through explicit transaction boundaries (commit/rollback).
     */
    public synchronized void executeAtomicTransfer(Long sourceAccId, Long destAccId, BigDecimal amount, String description) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be strictly greater than zero.");
        }
        if (sourceAccId.equals(destAccId)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be identical.");
        }

        String txRef = ValidationUtil.generateTransactionRef();

        if (config.isMockMode()) {
            Account src = mockDb.getAccount(sourceAccId);
            Account dst = mockDb.getAccount(destAccId);
            if (src == null || dst == null) throw new IllegalArgumentException("Invalid account selection.");
            
            Transaction tx = TransactionFactory.createTransaction("TRANSFER", null, txRef, sourceAccId, destAccId, null, amount, description);
            tx.execute(src, dst);
            mockDb.addTransaction(tx);
            mockDb.addAuditLog("INTER_ACCOUNT_TRANSFER", "TRANSACTIONS", tx.getId(), src.getUserId(),
                "Transferred $" + amount + " from " + src.getAccountNumber() + " to " + dst.getAccountNumber());

            FinanceEventManager.getInstance().notifyChange("TRANSFER_COMPLETED", tx);
            return;
        }

        // Oracle Database ACID Transaction Execution
        try (Connection conn = dbService.getConnection()) {
            conn.setAutoCommit(false); // Begin ACID Transaction Boundary
            try {
                // 1. Lock and fetch source account
                Account sourceAccount = lockAndFetchAccount(sourceAccId, conn);
                Account destAccount = lockAndFetchAccount(destAccId, conn);

                if (!"ACTIVE".equalsIgnoreCase(sourceAccount.getStatus())) {
                    throw new IllegalStateException("Source account is not active.");
                }
                if (!"ACTIVE".equalsIgnoreCase(destAccount.getStatus())) {
                    throw new IllegalStateException("Destination account is not active.");
                }

                // 2. Perform polymorphic withdrawal on source account
                sourceAccount.withdraw(amount);
                accountDAO.updateBalance(sourceAccId, sourceAccount.getBalance(), conn);

                // 3. Deposit into destination account
                destAccount.deposit(amount);
                accountDAO.updateBalance(destAccId, destAccount.getBalance(), conn);

                // 4. Record Transaction
                Transaction tx = TransactionFactory.createTransaction(
                    "TRANSFER", null, txRef, sourceAccId, destAccId, null, amount, description
                );
                transactionDAO.save(tx, conn);

                // 5. Commit Transaction
                conn.commit();

                // 6. Audit logging for compliance
                auditDAO.insertLog(new AuditLog(null, "TRANSFER_COMPLETED", "TRANSACTIONS", tx.getId(),
                    sourceAccount.getUserId(), "Transferred $" + amount + " from " + sourceAccount.getAccountNumber() + " to " + destAccount.getAccountNumber()));

                FinanceEventManager.getInstance().notifyChange("TRANSFER_COMPLETED", tx);
            } catch (Exception ex) {
                conn.rollback(); // Rollback to preserve consistency
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Records an incoming financial transaction (Salary, Dividends, Client Payments).
     */
    public synchronized void recordIncome(Long destAccId, Long categoryId, BigDecimal amount, String description) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Income amount must be greater than zero.");
        }
        String txRef = ValidationUtil.generateTransactionRef();

        if (config.isMockMode()) {
            Account dst = mockDb.getAccount(destAccId);
            if (dst == null) throw new IllegalArgumentException("Destination account not found.");
            dst.deposit(amount);
            Transaction tx = TransactionFactory.createTransaction("INCOME", null, txRef, null, destAccId, categoryId, amount, description);
            mockDb.addTransaction(tx);
            FinanceEventManager.getInstance().notifyChange("INCOME_RECORDED", tx);
            return;
        }

        try (Connection conn = dbService.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Account destAccount = lockAndFetchAccount(destAccId, conn);
                destAccount.deposit(amount);
                accountDAO.updateBalance(destAccId, destAccount.getBalance(), conn);

                Transaction tx = TransactionFactory.createTransaction("INCOME", null, txRef, null, destAccId, categoryId, amount, description);
                transactionDAO.save(tx, conn);

                conn.commit();
                FinanceEventManager.getInstance().notifyChange("INCOME_RECORDED", tx);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Records an expense transaction with source account debit.
     */
    public synchronized void recordExpense(Long sourceAccId, Long categoryId, BigDecimal amount, String description) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero.");
        }
        String txRef = ValidationUtil.generateTransactionRef();

        if (config.isMockMode()) {
            Account src = mockDb.getAccount(sourceAccId);
            if (src == null) throw new IllegalArgumentException("Source account not found.");
            src.withdraw(amount);
            Transaction tx = TransactionFactory.createTransaction("EXPENSE", null, txRef, sourceAccId, null, categoryId, amount, description);
            mockDb.addTransaction(tx);
            FinanceEventManager.getInstance().notifyChange("EXPENSE_RECORDED", tx);
            return;
        }

        try (Connection conn = dbService.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Account sourceAccount = lockAndFetchAccount(sourceAccId, conn);
                sourceAccount.withdraw(amount);
                accountDAO.updateBalance(sourceAccId, sourceAccount.getBalance(), conn);

                Transaction tx = TransactionFactory.createTransaction("EXPENSE", null, txRef, sourceAccId, null, categoryId, amount, description);
                transactionDAO.save(tx, conn);

                conn.commit();
                FinanceEventManager.getInstance().notifyChange("EXPENSE_RECORDED", tx);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Account lockAndFetchAccount(Long accountId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM ACCOUNTS WHERE ACCOUNT_ID = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountDAO dao = new AccountDAO();
                    return dao.findById(accountId).orElseThrow(() -> new SQLException("Account not found."));
                } else {
                    throw new SQLException("Account with ID " + accountId + " does not exist.");
                }
            }
        }
    }

    public List<Account> getAccounts(Long userId) throws SQLException {
        if (config.isMockMode()) {
            return mockDb.getAccounts(userId);
        }
        return accountDAO.findByUserId(userId);
    }

    public Account createAccount(Account account) throws SQLException {
        if (config.isMockMode()) {
            Account created = mockDb.addAccount(account);
            FinanceEventManager.getInstance().notifyChange("ACCOUNT_CREATED", created);
            return created;
        }
        Account saved = accountDAO.save(account);
        FinanceEventManager.getInstance().notifyChange("ACCOUNT_CREATED", saved);
        return saved;
    }

    public List<Transaction> getRecentTransactions(Long userId, int limit) throws SQLException {
        if (config.isMockMode()) {
            return mockDb.getTransactions(userId, limit);
        }
        return transactionDAO.findByUserId(userId, limit);
    }

    public List<Category> getCategories(Long userId) throws SQLException {
        if (config.isMockMode()) {
            return mockDb.getCategories(userId);
        }
        return categoryDAO.findByUserId(userId);
    }

    public Category createCategory(Category category) throws SQLException {
        if (config.isMockMode()) {
            return mockDb.addCategory(category);
        }
        return categoryDAO.save(category);
    }

    public List<Budget> getBudgets(Long userId, String monthYear) throws SQLException {
        if (config.isMockMode()) {
            return mockDb.getBudgets(userId, monthYear);
        }
        return budgetDAO.findByUserIdAndMonth(userId, monthYear);
    }

    public Budget createBudget(Budget budget) throws SQLException {
        if (config.isMockMode()) {
            Budget saved = mockDb.addBudget(budget);
            FinanceEventManager.getInstance().notifyChange("BUDGET_SAVED", saved);
            return saved;
        }
        Budget saved = budgetDAO.save(budget);
        FinanceEventManager.getInstance().notifyChange("BUDGET_SAVED", saved);
        return saved;
    }

    public List<AuditLog> getRecentAuditLogs(int limit) throws SQLException {
        if (config.isMockMode()) {
            return mockDb.getAuditLogs();
        }
        return auditDAO.getRecentLogs(limit);
    }

    /**
     * Calculates high-level financial summary KPIs for the current billing month.
     */
    public FinancialSummary getFinancialSummary(Long userId) throws SQLException {
        List<Account> accounts = getAccounts(userId);
        BigDecimal netWorth = BigDecimal.ZERO;
        for (Account a : accounts) {
            netWorth = netWorth.add(a.getBalance());
        }

        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        BigDecimal monthlyIncome = BigDecimal.ZERO;
        BigDecimal monthlyExpense = BigDecimal.ZERO;

        if (config.isMockMode()) {
            List<Transaction> txs = mockDb.getTransactions(userId, 500);
            for (Transaction tx : txs) {
                String txMonth = tx.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                if (currentMonth.equals(txMonth)) {
                    if ("INCOME".equalsIgnoreCase(tx.getTransactionType())) {
                        monthlyIncome = monthlyIncome.add(tx.getAmount());
                    } else if ("EXPENSE".equalsIgnoreCase(tx.getTransactionType())) {
                        monthlyExpense = monthlyExpense.add(tx.getAmount());
                    }
                }
            }
            return new FinancialSummary(netWorth, monthlyIncome, monthlyExpense, accounts.size(), txs.size());
        }

        monthlyIncome = transactionDAO.getMonthlyTotal(userId, "INCOME", currentMonth);
        monthlyExpense = transactionDAO.getMonthlyTotal(userId, "EXPENSE", currentMonth);
        int totalTx = transactionDAO.findByUserId(userId, 1000).size();

        return new FinancialSummary(netWorth, monthlyIncome, monthlyExpense, accounts.size(), totalTx);
    }
}
