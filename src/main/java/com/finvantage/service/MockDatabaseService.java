package com.finvantage.service;

import com.finvantage.factory.AccountFactory;
import com.finvantage.factory.TransactionFactory;
import com.finvantage.model.Account;
import com.finvantage.model.AuditLog;
import com.finvantage.model.Budget;
import com.finvantage.model.Category;
import com.finvantage.model.FinancialSummary;
import com.finvantage.model.Transaction;
import com.finvantage.model.User;
import com.finvantage.util.PasswordUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MockDatabaseService provides a high-fidelity in-memory persistence layer.
 * Enables instant offline presentations, unit testing, and fallback if an external
 * Oracle Database instance is temporarily unreachable during evaluation.
 */
public class MockDatabaseService {

    private static final MockDatabaseService INSTANCE = new MockDatabaseService();

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();
    private final Map<Long, Category> categories = new ConcurrentHashMap<>();
    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<Long, Budget> budgets = new ConcurrentHashMap<>();
    private final List<AuditLog> auditLogs = Collections.synchronizedList(new ArrayList<>());

    private final AtomicLong userSeq = new AtomicLong(10);
    private final AtomicLong accountSeq = new AtomicLong(10);
    private final AtomicLong categorySeq = new AtomicLong(20);
    private final AtomicLong txSeq = new AtomicLong(100);
    private final AtomicLong budgetSeq = new AtomicLong(10);
    private final AtomicLong logSeq = new AtomicLong(10);

    private MockDatabaseService() {
        seedInitialMockData();
    }

    public static MockDatabaseService getInstance() {
        return INSTANCE;
    }

    private void seedInitialMockData() {
        // Default Demo User
        User demoUser = new User(1L, "Alexander Vance", "alex.vance@finvantage.com", 
            PasswordUtil.hashPassword("Admin@123"), User.Role.ADMIN);
        demoUser.setPhoneNumber("+1-555-0199");
        users.put(demoUser.getId(), demoUser);

        // Default Categories
        Category catSalary = new Category(1L, 1L, "Salary & Compensation", Category.Type.INCOME, "#2ECC71", "briefcase");
        Category catDiv = new Category(2L, 1L, "Investment Dividends", Category.Type.INCOME, "#1ABC9C", "trending-up");
        Category catHousing = new Category(3L, 1L, "Housing & Rent", Category.Type.EXPENSE, "#E74C3C", "home");
        Category catGroceries = new Category(4L, 1L, "Groceries & Dining", Category.Type.EXPENSE, "#E67E22", "shopping-cart");
        Category catTech = new Category(5L, 1L, "Cloud & Software", Category.Type.EXPENSE, "#9B59B6", "server");
        Category catTravel = new Category(6L, 1L, "Travel & Transport", Category.Type.EXPENSE, "#34495E", "plane");

        categories.put(1L, catSalary);
        categories.put(2L, catDiv);
        categories.put(3L, catHousing);
        categories.put(4L, catGroceries);
        categories.put(5L, catTech);
        categories.put(6L, catTravel);

        // Default Accounts (Savings, Checking, Investment)
        Account accSavings = AccountFactory.createAccount(
            "SAVINGS", 1L, 1L, "ACC-SAV-1001", "Premier High-Yield Savings", 
            BigDecimal.valueOf(18500.00), "USD", BigDecimal.valueOf(4.75), 
            BigDecimal.ZERO, BigDecimal.valueOf(500.00), null
        );
        Account accChecking = AccountFactory.createAccount(
            "CHECKING", 2L, 1L, "ACC-CHK-2002", "Apex Operating Checking", 
            BigDecimal.valueOf(4250.75), "USD", BigDecimal.ZERO, 
            BigDecimal.valueOf(1500.00), BigDecimal.ZERO, null
        );
        Account accInvestment = AccountFactory.createAccount(
            "INVESTMENT", 3L, 1L, "ACC-INV-3003", "Vanguard Growth Portfolio", 
            BigDecimal.valueOf(34200.50), "USD", BigDecimal.valueOf(7.80), 
            BigDecimal.ZERO, BigDecimal.valueOf(1000.00), "MODERATE"
        );

        accounts.put(1L, accSavings);
        accounts.put(2L, accChecking);
        accounts.put(3L, accInvestment);

        // Default Transactions
        addMockTransaction("INCOME", "TX-VNT-1001", null, 2L, 1L, BigDecimal.valueOf(7500.00), "Monthly Executive Payroll Deposit", LocalDateTime.now().minusDays(18));
        addMockTransaction("INCOME", "TX-VNT-1002", null, 3L, 2L, BigDecimal.valueOf(850.50), "Q3 Index Fund Dividend Reinvestment", LocalDateTime.now().minusDays(14));
        addMockTransaction("EXPENSE", "TX-VNT-1003", 2L, null, 3L, BigDecimal.valueOf(1850.00), "Metropolitan Tower Apartment Lease", LocalDateTime.now().minusDays(12));
        addMockTransaction("EXPENSE", "TX-VNT-1004", 2L, null, 4L, BigDecimal.valueOf(234.60), "Whole Foods Bi-Weekly Grocery Restock", LocalDateTime.now().minusDays(8));
        addMockTransaction("EXPENSE", "TX-VNT-1005", 2L, null, 5L, BigDecimal.valueOf(178.40), "Oracle Cloud & GitHub Enterprise Billing", LocalDateTime.now().minusDays(5));
        addMockTransaction("EXPENSE", "TX-VNT-1006", 2L, null, 4L, BigDecimal.valueOf(86.25), "Bistro Artisan Family Dinner", LocalDateTime.now().minusDays(2));
        addMockTransaction("TRANSFER", "TX-VNT-1007", 2L, 1L, null, BigDecimal.valueOf(2000.00), "Automated Emergency Fund Transfer", LocalDateTime.now().minusDays(1));

        // Default Budgets for current month
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Budget b1 = new Budget(1L, 1L, 3L, currentMonth, BigDecimal.valueOf(2200.00), BigDecimal.valueOf(85.00));
        b1.setCategoryName("Housing & Rent");
        b1.setCategoryColor("#E74C3C");
        b1.setActualSpent(BigDecimal.valueOf(1850.00));

        Budget b2 = new Budget(2L, 1L, 4L, currentMonth, BigDecimal.valueOf(600.00), BigDecimal.valueOf(80.00));
        b2.setCategoryName("Groceries & Dining");
        b2.setCategoryColor("#E67E22");
        b2.setActualSpent(BigDecimal.valueOf(320.85));

        Budget b3 = new Budget(3L, 1L, 5L, currentMonth, BigDecimal.valueOf(300.00), BigDecimal.valueOf(75.00));
        b3.setCategoryName("Cloud & Software");
        b3.setCategoryColor("#9B59B6");
        b3.setActualSpent(BigDecimal.valueOf(178.40));

        budgets.put(1L, b1);
        budgets.put(2L, b2);
        budgets.put(3L, b3);

        auditLogs.add(new AuditLog(1L, "SYSTEM_INIT", "MOCK_DB", 1L, 1L, "Mock in-memory financial repository provisioned."));
    }

    private void addMockTransaction(String type, String ref, Long src, Long dst, Long cat, BigDecimal amount, String desc, LocalDateTime date) {
        Transaction tx = TransactionFactory.createTransaction(type, txSeq.incrementAndGet(), ref, src, dst, cat, amount, desc);
        tx.setTransactionDate(date);
        if (cat != null && categories.containsKey(cat)) {
            tx.setCategoryName(categories.get(cat).getName());
        }
        transactions.put(tx.getId(), tx);
    }

    // Accessors
    public User getMockUser() {
        return users.get(1L);
    }

    public List<Account> getAccounts(Long userId) {
        return new ArrayList<>(accounts.values());
    }

    public Account getAccount(Long id) {
        return accounts.get(id);
    }

    public Account addAccount(Account acc) {
        acc.setId(accountSeq.incrementAndGet());
        accounts.put(acc.getId(), acc);
        return acc;
    }

    public List<Transaction> getTransactions(Long userId, int limit) {
        List<Transaction> list = new ArrayList<>(transactions.values());
        list.sort((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()));
        if (limit > 0 && list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }

    public Transaction addTransaction(Transaction tx) {
        tx.setId(txSeq.incrementAndGet());
        if (tx.getCategoryId() != null && categories.containsKey(tx.getCategoryId())) {
            tx.setCategoryName(categories.get(tx.getCategoryId()).getName());
        }
        transactions.put(tx.getId(), tx);
        return tx;
    }

    public List<Category> getCategories(Long userId) {
        return new ArrayList<>(categories.values());
    }

    public Category addCategory(Category cat) {
        cat.setId(categorySeq.incrementAndGet());
        categories.put(cat.getId(), cat);
        return cat;
    }

    public List<Budget> getBudgets(Long userId, String monthYear) {
        return new ArrayList<>(budgets.values());
    }

    public Budget addBudget(Budget b) {
        b.setId(budgetSeq.incrementAndGet());
        if (categories.containsKey(b.getCategoryId())) {
            b.setCategoryName(categories.get(b.getCategoryId()).getName());
            b.setCategoryColor(categories.get(b.getCategoryId()).getColorHex());
        }
        budgets.put(b.getId(), b);
        return b;
    }

    public List<AuditLog> getAuditLogs() {
        return new ArrayList<>(auditLogs);
    }

    public void addAuditLog(String action, String table, Long recordId, Long userId, String details) {
        auditLogs.add(0, new AuditLog(logSeq.incrementAndGet(), action, table, recordId, userId, details));
    }
}
